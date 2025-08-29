package com.bristoHQ.securetotp.controllers.auth;

import java.util.Date;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bristoHQ.securetotp.dto.MessageResponseDTO;
import com.bristoHQ.securetotp.dto.auth.BearerToken;
import com.bristoHQ.securetotp.dto.auth.LoginDto;
import com.bristoHQ.securetotp.dto.auth.RegisterDto;
import com.bristoHQ.securetotp.dto.auth.TokenResponse;
import com.bristoHQ.securetotp.dto.otp.ResendOtpRequest;
import com.bristoHQ.securetotp.dto.otp.VerifyOtpRequest;
import com.bristoHQ.securetotp.dto.user.UserDTO;
import com.bristoHQ.securetotp.helper.Validator;
import com.bristoHQ.securetotp.security.jwt.JwtUtilities;
import com.bristoHQ.securetotp.services.email.EmailOtpService;
import com.bristoHQ.securetotp.services.email.EmailService;
import com.bristoHQ.securetotp.services.user.UserService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
public class AuthRestController {

    private final UserService userService;
    private final JwtUtilities jwtUtilities;
    private final EmailOtpService otpService;
    private final EmailService emailService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterDto registerDto) {

        System.out.println("Received registration request: " + registerDto);

        Map<String, Object> passResult = Validator.validatePassword(registerDto.getPassword());
        if (!(boolean) passResult.get("status")) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponseDTO(false, passResult.get("error").toString(), new Date()));
        }

        System.out.println("Registering user: " + registerDto);

        Map<String, Object> emailResult = Validator.validateEmail(registerDto.getEmail());
        if (!(boolean) emailResult.get("status")) {

            System.out.println("Email validation failed: " + emailResult.get("error"));
            return ResponseEntity.badRequest()
                    .body(new MessageResponseDTO(false, emailResult.get("error").toString(), new Date()));
        }

        if (registerDto.getEmail() == null || registerDto.getEmail().isEmpty()) {
            System.out.println("Email is required for registration");
            return ResponseEntity.badRequest()
                    .body(new MessageResponseDTO(false, "Email is required", new Date()));
        }

        if (registerDto.getFullName() == null || registerDto.getFullName().isEmpty()) {
            System.out.println("Full name is required for registration");
            return ResponseEntity.badRequest()
                    .body(new MessageResponseDTO(false, "Full name is required", new Date()));
        }

        // Validate full name length
        if (registerDto.getFullName().trim().length() < 2) {
            System.out.println("Full name validation failed: Full name must be at least 2 characters");
            return ResponseEntity.badRequest()
                    .body(new MessageResponseDTO(false, "Full name must be at least 2 characters", new Date()));
        }

        if (registerDto.getFullName().trim().length() > 12) {
            System.out.println("Full name validation failed: Full name must not exceed 12 characters");
            return ResponseEntity.badRequest()
                    .body(new MessageResponseDTO(false, "Full name must not exceed 12 characters", new Date()));
        }

        // Validate full name format (letters, spaces, hyphens, apostrophes only)
        if (!registerDto.getFullName().trim().matches("^[a-zA-Z\\s'\\-]+$")) {
            System.out.println("Full name validation failed: Full name contains invalid characters");
            return ResponseEntity.badRequest()
                    .body(new MessageResponseDTO(false,
                            "Full name can only contain letters, spaces, hyphens, and apostrophes", new Date()));
        }

        if (registerDto.getPassword() == null || registerDto.getPassword().isEmpty()) {
            System.out.println("Password is required for registration");
            return ResponseEntity.badRequest()
                    .body(new MessageResponseDTO(false, "Password is required", new Date()));
        }
        if (registerDto.getPassword().length() < 8) {

            System.out.println("Password validation failed: Password must be at least 8 characters long");
            return ResponseEntity.badRequest()
                    .body(new MessageResponseDTO(false, "Password must be at least 8 characters long", new Date()));
        }
        if (registerDto.getPassword().length() > 20) {
            System.out.println("Password validation failed: Password must be at most 20 characters long");
            return ResponseEntity.badRequest()
                    .body(new MessageResponseDTO(false, "Password must be at most 20 characters long", new Date()));
        }

        if (registerDto.getUsername() != null && !registerDto.getUsername().isEmpty()) {
            // Validate username if provided
            // Ensure username starts with @ before validation
            if (!registerDto.getUsername().startsWith("@")) {
                registerDto.setUsername("@" + registerDto.getUsername());
            }

            Map<String, Object> usernameResult = Validator.validateUsername(registerDto.getUsername());
            if (!(boolean) usernameResult.get("status")) {
                System.out.println("Username validation failed: " + usernameResult.get("error"));
                return ResponseEntity.badRequest()
                        .body(new MessageResponseDTO(false, usernameResult.get("error").toString(), new Date()));
            }
        }

        if (registerDto.getUsername() == null || registerDto.getUsername().isEmpty()) {
            System.out.println("Username not provided, defaulting to email prefix");
            registerDto.setUsername("@" + registerDto.getEmail().split("@")[0]); // Default username to email prefix
                                                                                 // with @ if not provided
        }
        try {
            if (userService.isUserExist(registerDto.getEmail())
                    || userService.isUserExistByUsername(registerDto.getUsername())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new MessageResponseDTO(false, "User already exists", new Date()));
            }

            registerDto.setProvider("LOCAL");

            // Register the user
            ResponseEntity<?> registrationResponse = userService.register(registerDto);

            if (registrationResponse.getStatusCode() != HttpStatus.OK) {
                // If registration failed, return the error
                return registrationResponse;
            }

            emailService.welcomeEmailToNewUser(registerDto.getEmail());

            // Try to send OTP email but don't fail registration if it fails
            try {
                otpService.generateAndSendOtp(registerDto.getEmail());
            } catch (Exception e) {
                // Log the email error but return success for registration
                System.err.println("Failed to send verification email: " + e.getMessage());
                // Return success but mention the email issue
                BearerToken bearerToken = null;
                if (registrationResponse.getBody() instanceof BearerToken) {
                    bearerToken = (BearerToken) registrationResponse.getBody();
                }
                String accessToken = bearerToken != null ? bearerToken.getAccessToken() : "";
                return ResponseEntity.ok(new BearerToken(
                        accessToken,
                        "Bearer "));
            }

            // Return the original success response with token
            emailService.welcomeEmailToNewUser(registerDto.getEmail());
            return registrationResponse;
        } catch (Exception e) {
            // Handle any other exceptions
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponseDTO(false, "Registration failed: " + e.getMessage(), new Date()));
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<MessageResponseDTO> verifyOtp(@RequestBody VerifyOtpRequest request) {

        Map<String, Object> emailResult = Validator.validateEmail(request.getEmail());
        if (!(boolean) emailResult.get("status")) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponseDTO(false, emailResult.get("error").toString(), new Date()));
        }
        if (request.getOtp() == null || request.getOtp().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponseDTO(false, "OTP is required", new Date()));
        }
        if (request.getOtp().length() != 6) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponseDTO(false, "OTP must be 6 digits", new Date()));
        }
        if (!request.getOtp().matches("\\d{6}")) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponseDTO(false, "OTP must contain only digits", new Date()));
        }
        if (!userService.isUserExist(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponseDTO(false, "User not found", new Date()));
        }

        boolean verified = otpService.verifyOtp(request.getEmail(), request.getOtp());

        if (verified) {
            return ResponseEntity.ok(new MessageResponseDTO(true, "Email verified successfully", new Date()));
        } else {
            return ResponseEntity.badRequest()
                    .body(new MessageResponseDTO(false, "Invalid OTP or OTP expired", new Date()));
        }
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<MessageResponseDTO> resendOtp(@RequestBody ResendOtpRequest request) {

        Map<String, Object> emailResult = Validator.validateEmail(request.getEmail());
        if (!(boolean) emailResult.get("status")) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponseDTO(false, emailResult.get("error").toString(), new Date()));
        }
        if (request.getEmail() == null || request.getEmail().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponseDTO(false, "Email is required", new Date()));
        }
        if (!userService.isUserExist(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponseDTO(false, "User not found", new Date()));
        }

        try {
            UserDTO userDTO = userService.findByEmail(request.getEmail());
            if (userDTO.isVerified()) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponseDTO(false, "Email already verified", new Date()));
            }
            otpService.generateAndSendOtp(request.getEmail());
            return ResponseEntity.ok(new MessageResponseDTO(true, "OTP sent to your email", new Date()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponseDTO(false, e.getMessage(), new Date()));
        }
    }

    @GetMapping("/reset-password")
    public ResponseEntity<MessageResponseDTO> resetPassword(@RequestParam String email) {
        Map<String, Object> emailResult = Validator.validateEmail(email);
        if (!(boolean) emailResult.get("status")) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponseDTO(false, emailResult.get("error").toString(), new Date()));
        }
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponseDTO(false, "Email is required", new Date()));
        }
        if (!userService.isUserExist(email)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponseDTO(false, "User not found", new Date()));
        }
        try {
            UserDTO user = userService.findByEmail(email);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new MessageResponseDTO(false, "User not found", new Date()));
            }
            emailService.sendResetPasswordEmail(email, jwtUtilities.generateResetToken(email));
            return ResponseEntity
                    .ok(new MessageResponseDTO(true, "Reset Link sent to your email for password reset", new Date()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponseDTO(false, e.getMessage(), new Date()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponseDTO> resetPassword(@RequestParam String token,
            @RequestParam String newPassword) {
        if (newPassword == null || newPassword.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponseDTO(false, "New password is required", new Date()));
        }
        Map<String, Object> passResult = Validator.validatePassword(newPassword);
        if (!(boolean) passResult.get("status")) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponseDTO(false, passResult.get("error").toString(), new Date()));
        }

        if (token == null || token.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponseDTO(false, "Token is required", new Date()));
        }

        try {
            if (token == null || token.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponseDTO(false, "Token is required", new Date()));
            }
            if (newPassword == null || newPassword.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponseDTO(false, "New password is required", new Date()));
            }

            String email = jwtUtilities.validateResetToken(token);
            if (email == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new MessageResponseDTO(false, "Invalid or expired token", new Date()));
            }

            userService.updatePassword(email, newPassword);
            return ResponseEntity.ok(new MessageResponseDTO(true, "Password reset successfully", new Date()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponseDTO(false, e.getMessage(), new Date()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam(value = "error", required = false) String error,
            @RequestBody LoginDto loginDto) {

        if (error != null && !error.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponseDTO(false, "Login failed: " + error, new Date()));
        }
        if (loginDto.getEmailOrUsername() == null || loginDto.getEmailOrUsername().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponseDTO(false, "Email or username is required", new Date()));
        }
        if (!loginDto.getEmailOrUsername().startsWith("@")) {
            Map<String, Object> emailResult = Validator.validateEmail(loginDto.getEmailOrUsername());
            if (!(boolean) emailResult.get("status")) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponseDTO(false, emailResult.get("error").toString(), new Date()));
            }
        }
        // Map<String, Object> passResult =
        // Validator.validatePassword(loginDto.getPassword());
        // if (!(boolean) passResult.get("status")) {
        // return ResponseEntity.badRequest()
        // .body(new MessageResponseDTO(false, passResult.get("error").toString(), new
        // Date()));
        // }
        if (loginDto.getEmailOrUsername() == null || loginDto.getEmailOrUsername().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponseDTO(false, "Email or username is required", new Date()));
        }
        if (loginDto.getPassword() == null || loginDto.getPassword().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponseDTO(false, "Password is required", new Date()));
        }

        try {
            if (loginDto.getEmailOrUsername() == null || loginDto.getEmailOrUsername().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponseDTO(false, "Email or username is required", new Date()));
            }

            if (loginDto.getPassword() == null || loginDto.getPassword().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponseDTO(false, "Password is required", new Date()));
            }

            // Check if user exists
            boolean userExists = userService.isUserExist(loginDto.getEmailOrUsername()) ||
                    userService.isUserExistByUsername(loginDto.getEmailOrUsername());

            if (!userExists) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new MessageResponseDTO(false, "Account not found", new Date()));
            }

            // Check if user signed up with OAuth
            boolean isOAuthUser = userService.isOAuthUser(loginDto.getEmailOrUsername());
            if (isOAuthUser) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponseDTO(false, "Please sign in with Google with same email!", new Date()));
            }

            try {
                // Attempt authentication
                System.out.println("Attempting to authenticate user: " + loginDto);
                String token = userService.authenticate(loginDto);
                System.out.println("Authentication successful, token: " + token);
                return ResponseEntity.ok(new BearerToken(token, "Bearer "));

            } catch (Exception e) {
                String errorMessage = e.getMessage().toLowerCase();

                // Provide specific error message based on exception
                if (errorMessage.contains("password") || errorMessage.contains("credentials")) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(new MessageResponseDTO(false, "Incorrect password", new Date()));
                } else {
                    e.printStackTrace();
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(new MessageResponseDTO(false, "Login failed: " + e.getMessage(), new Date()));
                }
            }
        } catch (Exception e) {
            // Log the error for debugging
            e.printStackTrace();

            // Return generic error message to client
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponseDTO(false, "Login failed: " + e.getMessage(), new Date()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        if (request == null) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponseDTO(false, "Request is null", new Date()));
        }
        try {
            String token = jwtUtilities.getToken(request);
            if (token != null) {
                userService.blacklistToken(token);
                return ResponseEntity
                        .ok(new MessageResponseDTO(true, "Logout successful, token blacklisted", new Date()));
            } else {
                return ResponseEntity.badRequest()
                        .body(new MessageResponseDTO(false, "No token provided", new Date()));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponseDTO(false, "Logout failed: " + e.getMessage(), new Date()));
        }
    }

    @GetMapping("/isLogin")
    public ResponseEntity<Boolean> isAuthenticatedByToken(@RequestHeader("Authorization") String token) {
        if (token == null || token.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(false);
        }
        if (!token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(false);
        }
        // Extract token value from "Bearer <token>"
        String actualToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        UserDTO user = userService.getUserDetails(actualToken);
        return ResponseEntity.ok(user != null);
    }

    @PostMapping("/validateToken")
    public ResponseEntity<TokenResponse> validateToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || authHeader.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new TokenResponse(null, HttpStatus.UNAUTHORIZED));
        }
        // Check if the token starts with "Bearer "
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            // Extract token value from "Bearer <token>"
            String actualToken = token.startsWith("Bearer ") ? token.substring(7) : token;
            UserDTO user = userService.getUserDetails(actualToken);
            if (user != null) {
                // userService.isUserExist(user.getEmail());
                return ResponseEntity.ok(new TokenResponse(user.getUsername(), HttpStatus.OK));
            }
            System.out.println("hello");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new TokenResponse(null, HttpStatus.UNAUTHORIZED));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new TokenResponse(null, HttpStatus.UNAUTHORIZED));
    }

    @PostMapping("/validateTokenOrNot")
    public ResponseEntity<Boolean> validateTokenOrNot(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            // Extract token value from "Bearer <token>"
            String actualToken = token.startsWith("Bearer ") ? token.substring(7) : token;
            UserDTO user = userService.getUserDetails(actualToken);
            if (user != null) {
                return ResponseEntity.ok(userService.isUserExist(user.getEmail()));
            }
            System.out.println("/validateTokenOrNot - UNAUTHORIZED");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(false);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(false);
    }
}
