package com.bristoHQ.securetotp.services.user;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.bristoHQ.securetotp.dto.user.UserDTO;
import com.bristoHQ.securetotp.helper.EncryptionService;
import com.bristoHQ.securetotp.helper.Validator;
import com.bristoHQ.securetotp.models.User;
import com.bristoHQ.securetotp.models.role.Role;
import com.bristoHQ.securetotp.repositories.RoleRepository;
import com.bristoHQ.securetotp.repositories.UserRepository;
import com.bristoHQ.securetotp.services.storage.CloudinaryService;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserProfileService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private EncryptionService encryptionService;

    @Autowired
    private RoleRepository roleRepository;

    /**
     * Update user's full name
     */
    public String updateFullName(String usernameOrEmail, String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name cannot be empty");
        }

        User user = findUserByUsernameOrEmail(usernameOrEmail);
        user.setFullName(encryptionService.encrypt(fullName.trim()));
        userRepository.save(user);

        return "Full name updated successfully";
    }

    /**
     * Update user's email
     */
    public String updateEmail(String usernameOrEmail, String newEmail) {
        Map<String, Object> validation = Validator.validateEmail(newEmail);
        if (!validation.get("status").equals(true)) {
            throw new IllegalArgumentException(validation.get("error").toString());
        }

        User currentUser = findUserByUsernameOrEmail(usernameOrEmail);

        // Decrypt current email to compare
        String currentEmailDecrypted = encryptionService.decrypt(currentUser.getEmail());
        if (currentEmailDecrypted.equalsIgnoreCase(newEmail)) {
            throw new IllegalArgumentException("New email is the same as current email");
        }

        // Check if email already exists (search with encrypted email)
        if (userRepository.existsByEmail(encryptionService.encrypt(newEmail))) {
            throw new IllegalArgumentException("Email already exists");
        }

        currentUser.setEmail(encryptionService.encrypt(newEmail));
        userRepository.save(currentUser);

        return "Email updated successfully";
    }

    /**
     * Update user's username
     */
    public String updateUsername(String usernameOrEmail, String newUsername) {
        if (!newUsername.startsWith("@")) {
            throw new IllegalArgumentException("Username must start with '@'");
        }

        Map<String, Object> validation = Validator.validateUsername(newUsername);
        if (!validation.get("status").equals(true)) {
            throw new IllegalArgumentException(validation.get("error").toString());
        }

        User currentUser = findUserByUsernameOrEmail(usernameOrEmail);

        // Decrypt current username to compare
        String currentUsernameDecrypted = encryptionService.decrypt(currentUser.getUsername());
        if (currentUsernameDecrypted.equalsIgnoreCase(newUsername)) {
            throw new IllegalArgumentException("New username is the same as current username");
        }

        // Check if username already exists (search with encrypted username)
        if (userRepository.existsByUsername(encryptionService.encrypt(newUsername))) {
            throw new IllegalArgumentException("Username already exists");
        }

        // Update username in User table (encrypted)
        currentUser.setUsername(encryptionService.encrypt(newUsername));
        userRepository.save(currentUser);

        // Update username in Role table (unencrypted for roles)
        List<Role> userRoles = roleRepository.findByUsername(currentUsernameDecrypted);
        for (Role role : userRoles) {
            role.setUsername(newUsername); // Store unencrypted username in roles
            roleRepository.save(role);
        }

        return "Username updated successfully";
    }

    /**
     * Update user's avatar
     */
    public String updateAvatar(String usernameOrEmail, MultipartFile avatarFile) throws IOException {
        if (avatarFile == null || avatarFile.isEmpty()) {
            throw new IllegalArgumentException("Avatar file is required");
        }

        User user = findUserByUsernameOrEmail(usernameOrEmail);

        // Upload new avatar
        @SuppressWarnings("unchecked")
        Map<String, Object> uploadResult = cloudinaryService.uploadImage(avatarFile);
        String imageUrl = uploadResult.get("url").toString();

        user.setUserAvatar(encryptionService.encrypt(imageUrl)); // Encrypt URLs
        userRepository.save(user);

        return "Avatar updated successfully";
    }

    /**
     * Update user's banner
     */
    public String updateBanner(String usernameOrEmail, MultipartFile bannerFile) throws IOException {
        if (bannerFile == null || bannerFile.isEmpty()) {
            throw new IllegalArgumentException("Banner file is required");
        }

        User user = findUserByUsernameOrEmail(usernameOrEmail);

        // Upload new banner
        @SuppressWarnings("unchecked")
        Map<String, Object> uploadResult = cloudinaryService.uploadImage(bannerFile);
        String imageUrl = uploadResult.get("url").toString();

        user.setUserBanner(encryptionService.encrypt(imageUrl)); // Encrypt URLs
        userRepository.save(user);

        return "Banner updated successfully";
    }

    /**
     * Update user's recovery phone
     */
    public String updateRecoveryPhone(String usernameOrEmail, String recoveryPhone) {
        if (recoveryPhone == null || recoveryPhone.trim().isEmpty()) {
            throw new IllegalArgumentException("Recovery phone cannot be empty");
        }

        // Basic phone validation (you can enhance this)
        String cleanPhone = recoveryPhone.replaceAll("[^+\\d]", "");
        if (cleanPhone.length() < 10) {
            throw new IllegalArgumentException("Invalid phone number format");
        }

        User user = findUserByUsernameOrEmail(usernameOrEmail);
        user.setRecoveryPhone(encryptionService.encrypt(cleanPhone));
        userRepository.save(user);

        return "Recovery phone updated successfully";
    }

    /**
     * Remove user's avatar
     */
    public String removeAvatar(String usernameOrEmail) {
        User user = findUserByUsernameOrEmail(usernameOrEmail);

        user.setUserAvatar(null);
        userRepository.save(user);

        return "Avatar removed successfully";
    }

    /**
     * Remove user's banner
     */
    public String removeBanner(String usernameOrEmail) {
        User user = findUserByUsernameOrEmail(usernameOrEmail);

        user.setUserBanner(null);
        userRepository.save(user);

        return "Banner removed successfully";
    }

    /**
     * Helper method to find user by username or email (with proper encryption)
     */
    private User findUserByUsernameOrEmail(String usernameOrEmail) {
        String encryptedParam = encryptionService.encrypt(usernameOrEmail);
        return userRepository.findByEmailOrUsername(encryptedParam, encryptedParam)
                .orElseThrow(() -> new RuntimeException("User not found: " + usernameOrEmail));
    }

    /**
     * Get user profile information (properly decrypted)
     */
    public UserDTO getUserProfile(String usernameOrEmail) {
        return userService.findByEmailOrUsername(usernameOrEmail, usernameOrEmail);
    }

    public boolean updatePassword(String name, String currentPassword, String newPassword) {

        if (!userService.checkPassword(name, currentPassword)) {
            return false; // Current password is incorrect
        }

        if (newPassword == null || newPassword.length() < 8) {
            return false; // New password must be at least 8 characters long
        }

        userService.updatePassword(name, newPassword);
        return true; // Password updated successfully
    }
}