package com.bristoHQ.securetotp.controllers.auth;

import java.util.Date;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
import com.bristoHQ.securetotp.security.jwt.JwtUtilities;
import com.bristoHQ.securetotp.services.email.EmailOtpService;
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

    @PostMapping("/register")
public ResponseEntity<?> register(@RequestBody RegisterDto registerDto) {
    if(registerDto.getUsername() == null || registerDto.getUsername().isEmpty()) {
        registerDto.setUsername(registerDto.getEmail().split("@")[0]); // Default username to email prefix if not provided
    }
    try {
        if (userService.isUserExist(registerDto.getEmail()) || userService.isUserExistByUsername(registerDto.getUsername())) {
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

        // Try to send OTP email but don't fail registration if it fails
        try {
            otpService.generateAndSendOtp(registerDto.getEmail());
        } catch (Exception e) {
            // Log the email error but return success for registration
            System.err.println("Failed to send verification email: " + e.getMessage());
            // Return success but mention the email issue
            return ResponseEntity.ok(new BearerToken(
                ((BearerToken)registrationResponse.getBody()).getAccessToken(),
                "Bearer "
            ));
        }

        // Return the original success response with token
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
        boolean verified = otpService.verifyOtp(request.getEmail(), request.getOtp());
        
        if (verified) {
            return ResponseEntity.ok(new MessageResponseDTO(true, "Email verified successfully", new Date()));
        } else {
            return ResponseEntity.badRequest().body(new MessageResponseDTO(false, "Invalid OTP or OTP expired", new Date()));
        }
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<MessageResponseDTO> resendOtp(@RequestBody ResendOtpRequest request) {
        try {
            // UserDTO userDTO = userService.findByEmail(request.getEmail());
            // if(userDTO.isVerified()) {
            //     return ResponseEntity.badRequest().body(new MessageResponseDTO(false, "Email already verified", new Date()));
            // }
            otpService.generateAndSendOtp(request.getEmail());
            return ResponseEntity.ok(new MessageResponseDTO(true, "OTP sent to your email", new Date()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponseDTO(false, e.getMessage(), new Date()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam(value = "error", required = false) String error,
            @RequestBody LoginDto loginDto) {
        try {
            // Validate request data
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
                    .body(new MessageResponseDTO(false, "Please sign in with Google", new Date()));
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
                    throw e; // Re-throw for other authentication errors
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
        try {
            String token = jwtUtilities.getToken(request);
            if (token != null) {
                userService.blacklistToken(token);
                return ResponseEntity.ok(new MessageResponseDTO(true, "Logout successful, token blacklisted", new Date()));
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
        // Extract token value from "Bearer <token>"
        String actualToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        UserDTO user = userService.getUserDetails(actualToken);
        return ResponseEntity.ok(user != null);
    }

    @PostMapping("/validateToken")
    public ResponseEntity<TokenResponse> validateToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
             // Extract token value from "Bearer <token>"
        String actualToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        UserDTO user = userService.getUserDetails(actualToken);
        if(user != null){
            // userService.isUserExist(user.getEmail());
            return ResponseEntity.ok(new TokenResponse(user.getUsername(), HttpStatus.OK));
        }
        System.out.println("hello");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new TokenResponse(null, HttpStatus.UNAUTHORIZED));
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
        if(user != null){

            return ResponseEntity.ok(userService.isUserExist(user.getEmail()));
        }
        System.out.println("hello");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(false);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(false);
    }


    @GetMapping("/byEmailUsername/{emailOrUsername}")
    public ResponseEntity<UserDTO> getUserByEmailOrUsername(@PathVariable String emailOrUsername) {
        UserDTO user = userService.findByEmailOrUsername(emailOrUsername, emailOrUsername);
        return ResponseEntity.ok(user);
    }


    

}
