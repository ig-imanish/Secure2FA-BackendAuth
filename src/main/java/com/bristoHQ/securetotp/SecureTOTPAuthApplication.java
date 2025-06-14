package com.bristoHQ.securetotp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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
        // JwtUtilities jwtUtilities = context.getBean(JwtUtilities.class);
        // // EmailService emailService = context.getBean(EmailService.class);

        // // ✅ Generate a test JWT token
        // String token =
        // jwtUtilities.generateTokenWithoutExpiration("manishkukran123@gmail.com",
        // Arrays.asList("ADMIN"));
        // System.out.println("Generated JWT Token: " + token);

        System.out.println("\n\n" +
                "**************************************************\n" +
                "*                                                *\n" +
                "*          Welcome to DevHub Application!        *\n" +
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
}
