package com.bristoHQ.securetotp;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.bristoHQ.securetotp.helper.EncryptionService;
import com.bristoHQ.securetotp.models.User;
import com.bristoHQ.securetotp.models.role.Role;
import com.bristoHQ.securetotp.models.role.RoleName;
import com.bristoHQ.securetotp.repositories.UserRepository;
import com.bristoHQ.securetotp.repositories.RoleRepository;
import com.bristoHQ.securetotp.security.jwt.JwtUtilities;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class SecureTOTPAuthApplication {
    public static void main(String[] args) {
        // Load environment variables from .env file BEFORE Spring starts
        try {
            Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
            dotenv.entries().forEach(e -> {
                System.setProperty(e.getKey(), e.getValue());
                System.out.println("Loaded env var: " + e.getKey());
            });
        } catch (Exception e) {
            System.err.println("Warning: Failed to load .env file. " + e.getMessage());
        }

        var context = SpringApplication.run(SecureTOTPAuthApplication.class, args);

        // ✅ Manually get the JwtUtilities bean from Spring Context
        JwtUtilities jwtUtilities = context.getBean(JwtUtilities.class);
        // EmailService emailService = context.getBean(EmailService.class);

        // Creating Admin User
        createAdminUser(context);

        String token = jwtUtilities.generateTokenWithoutExpiration("admin@bristohq.me",
                Arrays.asList("ADMIN"));
        System.out.println("Generated JWT Token for ADMIN: " + token);

        // ✅ Generate a test JWT token
        // String token =
        // jwtUtilities.generateTokenWithoutExpiration("manishkukran123@gmail.com",
        // Arrays.asList("ADMIN"));
        // System.out.println("Generated JWT Token for ADMIN: " + token);

        // EncryptionService encryptionService =
        // context.getBean(EncryptionService.class);

        // Encrypt a test string
        // String testString = "@ig-imanish";
        // String encryptedString = encryptionService.encrypt(testString);
        // System.out.println("Encrypted String: " + encryptedString);

        System.out.println("\n\n" +
                "**************************************************\n" +
                "*                                                *\n" +
                "*          Welcome to Bristo Application!        *\n" +
                "**************************************************\n" +
                "*                                                *\n" +
                "*      Your application has started successfully!*\n" +
                "*                                                *\n" +
                "**************************************************\n" +
                "\n");

        // System.out.println("✉️ Testing email service...");
        // String testEmail = "manish.business.com@gmail.com";

        // // Send a test email
        // emailService.sendOtpEmail(testEmail, "123456");

        // System.out.println("✅ Test email sent successfully to: " + testEmail);
    }

    /**
     * Create an admin user with ADMIN role
     */
    private static void createAdminUser(org.springframework.context.ConfigurableApplicationContext context) {
        try {
            UserRepository userRepository = context.getBean(UserRepository.class);
            RoleRepository roleRepository = context.getBean(RoleRepository.class);
            PasswordEncoder passwordEncoder = context.getBean(PasswordEncoder.class);
            EncryptionService encryptionService = context.getBean(EncryptionService.class);

            String adminEmail = "admin@bristohq.me";
            String adminUsername = "@admin";

            // Check if admin user already exists
            if (userRepository.existsByEmail(encryptionService.encrypt(adminEmail))) {
                System.out.println("ℹ️  Admin user already exists, skipping creation.");
                return;
            }

            // Create admin user
            User adminUser = new User();
            adminUser.setEmail(adminEmail);
            adminUser.setFullName("Admin User");
            adminUser.setUsername(adminUsername);
            adminUser.setPassword(passwordEncoder.encode("admin@1829b"));
            adminUser.setProvider("LOCAL");
            adminUser.setAccountCreatedAt(new Date());
            adminUser.setVerified(true); // Admin should be verified
            adminUser.setPremium(false);

            // Create ADMIN role
            Role adminRole = new Role(RoleName.ADMIN, adminUsername);
            roleRepository.save(adminRole);

            // Assign ADMIN role to user
            adminUser.setRoles(Collections.singletonList(adminRole));

            // Encrypt and save user
            User encryptedUser = new User();
            encryptedUser.setEmail(encryptionService.encrypt(adminUser.getEmail()));
            encryptedUser.setFullName(encryptionService.encrypt(adminUser.getFullName()));
            encryptedUser.setUsername(encryptionService.encrypt(adminUser.getUsername()));
            encryptedUser.setPassword(adminUser.getPassword()); // Password is already encoded
            encryptedUser.setProvider(adminUser.getProvider());
            encryptedUser.setAccountCreatedAt(adminUser.getAccountCreatedAt());
            encryptedUser.setVerified(adminUser.isVerified());
            encryptedUser.setPremium(adminUser.isPremium());
            encryptedUser.setRoles(adminUser.getRoles());

            userRepository.save(encryptedUser);

            System.out.println("✅ Admin user created successfully!");
            System.out.println("   Email: " + adminEmail);
            System.out.println("   Username: " + adminUsername);
            System.out.println("   Password: admin@1829b");
            System.out.println("   Role: ADMIN");

        } catch (Exception e) {
            System.err.println("❌ Failed to create admin user: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
