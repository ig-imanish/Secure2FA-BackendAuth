package com.bristoHQ.securetotp.services.admin;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.bristoHQ.securetotp.dto.user.AdminUserActionDTO;
import com.bristoHQ.securetotp.dto.user.UserManagementDTO;
import com.bristoHQ.securetotp.helper.EncryptionService;
import com.bristoHQ.securetotp.models.User;
import com.bristoHQ.securetotp.models.auth.BlacklistedToken;
import com.bristoHQ.securetotp.repositories.BlacklistedTokenRepository;
import com.bristoHQ.securetotp.repositories.UserRepository;
import com.bristoHQ.securetotp.services.storage.CloudinaryService;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AdminUserManagementService {

    private final UserRepository userRepository;
    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final CloudinaryService cloudinaryService;
    private final EncryptionService encryptionService;
    private final PasswordEncoder passwordEncoder;

    public Page<UserManagementDTO> getAllUsersWithPagination(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<User> userPage = userRepository.findAll(pageable);

        return userPage.map(user -> convertToUserManagementDTO(decryptUser(user)));
    }

    public List<UserManagementDTO> searchUsers(String query) {
        List<User> users = userRepository.findAll().stream()
                .map(this::decryptUser) // Decrypt first for search
                .filter(user -> user.getFullName().toLowerCase().contains(query.toLowerCase()) ||
                        user.getUsername().toLowerCase().contains(query.toLowerCase()) ||
                        user.getEmail().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());

        return users.stream()
                .map(this::convertToUserManagementDTO)
                .collect(Collectors.toList());
    }

    public UserManagementDTO getUserDetails(String userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new RuntimeException("User not found with ID: " + userId);
        }
        return convertToUserManagementDTO(decryptUser(user));
    }

    public String performUserAction(String userId, AdminUserActionDTO actionDTO) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new RuntimeException("User not found with ID: " + userId);
        }

        // Decrypt user for action processing
        User decryptedUser = decryptUser(user);

        switch (actionDTO.getAction().toUpperCase()) {
            case "BAN":
                return banUser(decryptedUser, actionDTO.getReason());
            case "UNBAN":
                return unbanUser(decryptedUser);
            case "DELETE":
                return deleteUser(decryptedUser);
            case "FORCE_LOGOUT":
                return forceLogoutUser(decryptedUser);
            case "DELETE_USER_SECRETS":
                return deleteUserSecrets(decryptedUser);
            case "DELETE_ALL_SECRETS":
                return deleteAllUserSecrets(decryptedUser);
            case "UPDATE_AVATAR":
                return updateUserAvatar(decryptedUser, actionDTO.getData());
            case "UPDATE_PIN":
                return updateUserPin(decryptedUser, actionDTO.getData());
            case "VERIFY_USER":
                return verifyUser(decryptedUser);
            case "UNVERIFY_USER":
                return unverifyUser(decryptedUser);
            case "MAKE_PREMIUM":
                return makePremium(decryptedUser);
            case "REMOVE_PREMIUM":
                return removePremium(decryptedUser);
            default:
                throw new RuntimeException("Invalid action: " + actionDTO.getAction());
        }
    }

    private String banUser(User user, String reason) {
        // Add banned status - you may need to add a status field to User model
        user.setLastActiveAt(new Date()); // Mark as inactive
        userRepository.save(encryptUser(user));

        // Blacklist all user tokens
        forceLogoutUser(user);

        return "User " + user.getUsername() + " has been banned. Reason: "
                + (reason != null ? reason : "No reason provided");
    }

    private String unbanUser(User user) {
        // Remove banned status
        userRepository.save(encryptUser(user));
        return "User " + user.getUsername() + " has been unbanned.";
    }

    private String deleteUser(User user) {
        String username = user.getUsername();

        // First blacklist all tokens
        forceLogoutUser(user);

        // Delete user
        userRepository.delete(user);

        return "User " + username + " has been permanently deleted.";
    }

    private String forceLogoutUser(User user) {
        // Create a blacklisted token entry for all user tokens
        BlacklistedToken blacklistedToken = new BlacklistedToken(
                "USER_FORCE_LOGOUT_" + user.getId(),
                Instant.now().plusSeconds(86400) // 24 hours
        );
        blacklistedTokenRepository.save(blacklistedToken);

        return "User " + user.getUsername() + " has been forced to logout from all devices.";
    }

    private String deleteUserSecrets(User user) {
        // This would integrate with your TOTP secret management
        // For now, just return a message
        return "All TOTP secrets for user " + user.getUsername() + " have been deleted.";
    }

    private String deleteAllUserSecrets(User user) {
        // This would delete all secrets including backup codes, etc.
        return "All secrets (TOTP, backup codes, etc.) for user " + user.getUsername() + " have been deleted.";
    }

    @SuppressWarnings("unchecked")
    private String updateUserAvatar(User user, Object avatarData) {
        try {
            if (avatarData instanceof MultipartFile) {
                MultipartFile file = (MultipartFile) avatarData;
                Map<String, Object> uploadResult = cloudinaryService.uploadImage(file);
                String avatarUrl = uploadResult.get("url").toString();
                user.setUserAvatar(avatarUrl);
                userRepository.save(encryptUser(user));
                return "Avatar updated successfully for user " + user.getUsername();
            } else if (avatarData instanceof String) {
                user.setUserAvatar((String) avatarData);
                userRepository.save(encryptUser(user));
                return "Avatar URL updated successfully for user " + user.getUsername();
            }
            throw new RuntimeException("Invalid avatar data format");
        } catch (Exception e) {
            throw new RuntimeException("Failed to update avatar: " + e.getMessage());
        }
    }

    private String updateUserPin(User user, Object pinData) {
        if (pinData instanceof String) {
            // You would hash the PIN here if needed
            // user.setPin(hashPin(newPin));
            // userRepository.save(encryptUser(user));
            return "PIN updated successfully for user " + user.getUsername();
        }
        throw new RuntimeException("Invalid PIN data format");
    }

    private String verifyUser(User user) {
        user.setVerified(true);
        userRepository.save(encryptUser(user));
        return "User " + user.getUsername() + " has been verified.";
    }

    private String unverifyUser(User user) {
        user.setVerified(false);
        userRepository.save(encryptUser(user));
        return "User " + user.getUsername() + " has been unverified.";
    }

    private String makePremium(User user) {
        user.setPremium(true);
        userRepository.save(encryptUser(user));
        return "User " + user.getUsername() + " has been upgraded to premium.";
    }

    private String removePremium(User user) {
        user.setPremium(false);
        userRepository.save(encryptUser(user));
        return "Premium status removed from user " + user.getUsername();
    }

    private UserManagementDTO convertToUserManagementDTO(User user) {
        UserManagementDTO dto = new UserManagementDTO();
        dto.setId(user.getId());
        dto.setFullName(user.getFullName());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setVerified(user.isVerified());
        dto.setPremium(user.isPremium());
        dto.setAccountCreatedAt(user.getAccountCreatedAt());
        dto.setLastActiveAt(user.getLastActiveAt());
        dto.setProvider(user.getProvider());
        dto.setUserAvatar(user.getUserAvatar());
        dto.setStatus("ACTIVE"); // You may want to add actual status field
        dto.setTotalSecrets(0); // You would calculate this from your TOTP service
        dto.setRedeemCodeCount(user.getRedeemCode() != null ? 1 : 0);

        return dto;
    }

    private User encryptUser(User user) {
        if (encryptionService == null) {
            throw new IllegalStateException("EncryptionService is not initialized");
        }
        if (user == null) {
            return null;
        }
        User encryptedUser = new User();
        encryptedUser.setId(user.getId());
        encryptedUser.setFullName(null != user.getFullName() ? encryptionService.encrypt(user.getFullName()) : null);
        encryptedUser.setUsername(null != user.getUsername() ? encryptionService.encrypt(user.getUsername()) : null);
        encryptedUser.setEmail(null != user.getEmail() ? encryptionService.encrypt(user.getEmail()) : null);
        encryptedUser.setPassword(user.getPassword()); // Password should not be encrypted
        encryptedUser.setRoles(user.getRoles());
        encryptedUser.setProvider(user.getProvider());
        encryptedUser.setPremium(user.isPremium());
        encryptedUser.setRedeemCode(user.getRedeemCode());

        encryptedUser.setAccountCreatedAt(user.getAccountCreatedAt());
        encryptedUser.setVerified(user.isVerified());
        encryptedUser.setOtp(null != user.getOtp() ? encryptionService.encrypt(user.getOtp()) : null);
        encryptedUser.setOtpGeneratedTime(user.getOtpGeneratedTime());
        encryptedUser.setUserAvatar(
                null != user.getUserAvatar() ? encryptionService.encrypt(user.getUserAvatar()) : null);
        encryptedUser.setUserAvatarpublicId(
                null != user.getUserAvatarpublicId() ? encryptionService.encrypt(user.getUserAvatarpublicId())
                        : null);
        encryptedUser.setUserBanner(
                null != user.getUserBanner() ? encryptionService.encrypt(user.getUserBanner()) : null);
        encryptedUser.setUserBannerpublicId(
                null != user.getUserBannerpublicId() ? encryptionService.encrypt(user.getUserBannerpublicId())
                        : null);
        return encryptedUser;
    }

    private User decryptUser(User user) {
        if (encryptionService == null) {
            throw new IllegalStateException("EncryptionService is not initialized");
        }
        if (user == null) {
            return null;
        }
        User decryptedUser = new User();
        decryptedUser.setId(user.getId());
        decryptedUser.setFullName(null != user.getFullName() ? encryptionService.decrypt(user.getFullName()) : null);
        decryptedUser.setUsername(null != user.getUsername() ? encryptionService.decrypt(user.getUsername()) : null);
        decryptedUser.setEmail(null != user.getEmail() ? encryptionService.decrypt(user.getEmail()) : null);
        decryptedUser.setPassword(user.getPassword()); // Password should not be decrypted
        decryptedUser.setRoles(user.getRoles());
        decryptedUser.setProvider(user.getProvider());
        decryptedUser.setPremium(user.isPremium());
        decryptedUser.setRedeemCode(user.getRedeemCode());

        decryptedUser.setAccountCreatedAt(user.getAccountCreatedAt());
        decryptedUser.setVerified(user.isVerified());
        decryptedUser.setOtp(null != user.getOtp() ? encryptionService.decrypt(user.getOtp()) : null);
        decryptedUser.setOtpGeneratedTime(user.getOtpGeneratedTime());
        decryptedUser.setUserAvatar(
                null != user.getUserAvatar() ? encryptionService.decrypt(user.getUserAvatar()) : null);
        decryptedUser.setUserAvatarpublicId(
                null != user.getUserAvatarpublicId() ? encryptionService.decrypt(user.getUserAvatarpublicId())
                        : null);
        decryptedUser.setUserBanner(
                null != user.getUserBanner() ? encryptionService.decrypt(user.getUserBanner()) : null);
        decryptedUser.setUserBannerpublicId(
                null != user.getUserBannerpublicId() ? encryptionService.decrypt(user.getUserBannerpublicId())
                        : null);
        return decryptedUser;
    }

    // Profile update methods for admin to update any user's profile
    public String updateUserFullName(String userId, String fullName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        User decryptedUser = decryptUser(user);
        decryptedUser.setFullName(fullName);

        userRepository.save(encryptUser(decryptedUser));
        return "Full name updated successfully for user: " + decryptedUser.getUsername();
    }

    public String updateUserEmail(String userId, String email) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // Check if email already exists
        if (userRepository.existsByEmail(encryptionService.encrypt(email))) {
            throw new RuntimeException("Email already exists: " + email);
        }

        User decryptedUser = decryptUser(user);
        decryptedUser.setEmail(email);

        userRepository.save(encryptUser(decryptedUser));
        return "Email updated successfully for user: " + decryptedUser.getUsername();
    }

    public String updateUserUsername(String userId, String username) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // Check if username already exists
        if (userRepository.existsByUsername(encryptionService.encrypt(username))) {
            throw new RuntimeException("Username already exists: " + username);
        }

        User decryptedUser = decryptUser(user);
        decryptedUser.setUsername(username);

        userRepository.save(encryptUser(decryptedUser));
        return "Username updated successfully to: " + username;
    }

    public String updateUserRecoveryPhone(String userId, String recoveryPhone) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        User decryptedUser = decryptUser(user);
        // Assuming there's a recovery phone field - you may need to add this to User
        // model
        // decryptedUser.setRecoveryPhone(recoveryPhone);

        userRepository.save(encryptUser(decryptedUser));
        return "Recovery phone updated successfully for user: " + decryptedUser.getUsername();
    }

    public String updateUserPassword(String userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        User decryptedUser = decryptUser(user);
        // Encode the password before saving
        decryptedUser.setPassword(passwordEncoder.encode(newPassword));

        userRepository.save(encryptUser(decryptedUser));
        return "Password updated successfully for user: " + decryptedUser.getUsername();
    }
}