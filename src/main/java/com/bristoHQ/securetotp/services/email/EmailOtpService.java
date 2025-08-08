package com.bristoHQ.securetotp.services.email;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

import org.springframework.stereotype.Service;

import com.bristoHQ.securetotp.helper.EncryptionService;
import com.bristoHQ.securetotp.models.User;
import com.bristoHQ.securetotp.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailOtpService {

    private final EncryptionService encryptionService;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private static final long OTP_VALID_DURATION = 10; // 10 minutes

    public void generateAndSendOtp(String email) {
        Optional<User> userOptional = userRepository.findByEmail(encryptionService.encrypt(email));

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // Decrypt user for logging
            user = decryptUser(user);

            // Generate OTP
            String otp = generateOTP();

            // Print user before updating
            System.out.println("User before saving OTP: " + user);

            // Save OTP to user
            user.setOtp(otp);
            user.setOtpGeneratedTime(LocalDateTime.now());
            userRepository.save(encryptUser(user));
            System.out.println("User after saving OTP: " + user);

            // Send OTP via email
            emailService.sendOtpEmail(email, otp);
            System.out.println("Sent OTP: " + otp);
        } else {
            throw new RuntimeException("User with email " + email + " not found");
        }
    }

    public void sendResetEmail(String email, String token) {

    }

    public boolean verifyOtp(String email, String otp) {
        Optional<User> userOptional = userRepository.findByEmail(encryptionService.encrypt(email));

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // Decrypt user for verification
            user = decryptUser(user);

            if (user.isVerified()) {
                // User is already verified
                return true;
            }
            if (user.getOtp() == null || user.getOtpGeneratedTime() == null) {
                // OTP not generated or expired
                return false;
            }
            // Check if OTP matches and is not expired
            if (user.getOtp().equals(otp) &&
                    LocalDateTime.now().isBefore(user.getOtpGeneratedTime().plusMinutes(OTP_VALID_DURATION))) {

                // Mark user as verified
                user.setVerified(true);
                user.setOtp(null);
                user.setOtpGeneratedTime(null);
                userRepository.save(encryptUser(user));

                return true;
            }
        }

        return false;
    }

    private String generateOTP() {
        // Generate 6-digit OTP
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
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
}
