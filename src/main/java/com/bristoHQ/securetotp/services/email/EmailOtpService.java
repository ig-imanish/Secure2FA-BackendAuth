package com.bristoHQ.securetotp.services.email;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.bristoHQ.securetotp.models.User;
import com.bristoHQ.securetotp.repositories.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class EmailOtpService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private static final long OTP_VALID_DURATION = 10; // 10 minutes

    public void generateAndSendOtp(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            
            // Generate OTP
            String otp = generateOTP();
            
            // Print user before updating
            System.out.println("User before saving OTP: " + user);
            
            // Save OTP to user
            user.setOtp(otp);
            user.setOtpGeneratedTime(LocalDateTime.now());
            userRepository.save(user);
             System.out.println("User after saving OTP: " + user);
            
            // Send OTP via email
            emailService.sendOtpEmail(email, otp);
            System.out.println("Sent OTP: " + otp);
        } else {
            throw new RuntimeException("User with email " + email + " not found");
        }
    }

    public boolean verifyOtp(String email, String otp) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            
            if(user.isVerified()) {
                // User is already verified
                return true;
            }
            if(user.getOtp() == null || user.getOtpGeneratedTime() == null) {
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
                userRepository.save(user);
                
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
}
