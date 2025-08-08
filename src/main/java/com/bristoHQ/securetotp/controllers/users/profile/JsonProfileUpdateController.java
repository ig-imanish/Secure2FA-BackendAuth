package com.bristoHQ.securetotp.controllers.users.profile;

import java.io.IOException;
import java.security.Principal;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.bristoHQ.securetotp.dto.MessageResponseDTO;
import com.bristoHQ.securetotp.dto.auth.BearerToken;
import com.bristoHQ.securetotp.dto.user.UpdateEmailDTO;
import com.bristoHQ.securetotp.dto.user.UpdateFullNameDTO;
import com.bristoHQ.securetotp.dto.user.UpdateRecoveryPhoneDTO;
import com.bristoHQ.securetotp.dto.user.UpdateUsernameDTO;
import com.bristoHQ.securetotp.dto.user.UserDTO;
import com.bristoHQ.securetotp.services.user.UserProfileService;
import com.bristoHQ.securetotp.services.user.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/users/profile/individual")
@RequiredArgsConstructor
public class JsonProfileUpdateController {

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private UserService userService;

    /**
     * Get current user profile
     */
    @GetMapping
    public ResponseEntity<?> getCurrentUserProfile(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponseDTO(false, "Unauthorized: No user logged in", new Date()));
        }

        try {
            UserDTO userProfile = userProfileService.getUserProfile(principal.getName());
            return ResponseEntity.ok(userProfile);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponseDTO(false, e.getMessage(), new Date()));
        }
    }

    /**
     * Update user's full name only
     * POST body: { "fullName": "New Full Name" }
     */
    @PutMapping("/fullname")
    public ResponseEntity<MessageResponseDTO> updateFullName(
            @RequestBody UpdateFullNameDTO request,
            Principal principal) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponseDTO(false, "Unauthorized: No user logged in", new Date()));
        }

        try {
            String result = userProfileService.updateFullName(principal.getName(), request.getFullName());
            return ResponseEntity.ok(new MessageResponseDTO(true, result, new Date()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponseDTO(false, e.getMessage(), new Date()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponseDTO(false, "Failed to update full name", new Date()));
        }
    }

    /**
     * Update user's email only
     * POST body: { "email": "newemail@example.com" }
     */
    @PutMapping("/email")
    public ResponseEntity<?> updateEmail(
            @RequestBody UpdateEmailDTO request,
            Principal principal) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponseDTO(false, "Unauthorized: No user logged in", new Date()));
        }

        try {
            userProfileService.updateEmail(principal.getName(), request.getEmail());
            // Return new JWT token since email changed
            String newToken = userService.newJwtToken(request.getEmail());
            return ResponseEntity.ok(new BearerToken(newToken, "Bearer "));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponseDTO(false, e.getMessage(), new Date()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponseDTO(false, "Failed to update email", new Date()));
        }
    }

    /**
     * Update user's username only
     * POST body: { "username": "@newusername" }
     */
    @PutMapping("/username")
    public ResponseEntity<?> updateUsername(
            @RequestBody UpdateUsernameDTO request,
            Principal principal) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponseDTO(false, "Unauthorized: No user logged in", new Date()));
        }

        try {
            userProfileService.updateUsername(principal.getName(), request.getUsername());
            // Return new JWT token since username changed
            String newToken = userService.newJwtToken(request.getUsername());
            return ResponseEntity.ok(new BearerToken(newToken, "Bearer "));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponseDTO(false, e.getMessage(), new Date()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponseDTO(false, "Failed to update username", new Date()));
        }
    }

    /**
     * Update user's recovery phone only
     * POST body: { "recoveryPhone": "+1234567890" }
     */
    @PutMapping("/recovery-phone")
    public ResponseEntity<MessageResponseDTO> updateRecoveryPhone(
            @RequestBody UpdateRecoveryPhoneDTO request,
            Principal principal) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponseDTO(false, "Unauthorized: No user logged in", new Date()));
        }

        try {
            String result = userProfileService.updateRecoveryPhone(principal.getName(), request.getRecoveryPhone());
            return ResponseEntity.ok(new MessageResponseDTO(true, result, new Date()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponseDTO(false, e.getMessage(), new Date()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponseDTO(false, "Failed to update recovery phone", new Date()));
        }
    }

    /**
     * Update user's avatar only
     * Form data with file upload
     */
    @PutMapping("/avatar")
    public ResponseEntity<MessageResponseDTO> updateAvatar(
            @RequestParam("avatar") MultipartFile avatarFile,
            Principal principal) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponseDTO(false, "Unauthorized: No user logged in", new Date()));
        }

        try {
            String result = userProfileService.updateAvatar(principal.getName(), avatarFile);
            return ResponseEntity.ok(new MessageResponseDTO(true, result, new Date()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponseDTO(false, e.getMessage(), new Date()));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponseDTO(false, "Failed to upload avatar", new Date()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponseDTO(false, "Failed to update avatar", new Date()));
        }
    }

    /**
     * Update user's banner only
     * Form data with file upload
     */
    @PutMapping("/banner")
    public ResponseEntity<MessageResponseDTO> updateBanner(
            @RequestParam("banner") MultipartFile bannerFile,
            Principal principal) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponseDTO(false, "Unauthorized: No user logged in", new Date()));
        }

        try {
            String result = userProfileService.updateBanner(principal.getName(), bannerFile);
            return ResponseEntity.ok(new MessageResponseDTO(true, result, new Date()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponseDTO(false, e.getMessage(), new Date()));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponseDTO(false, "Failed to upload banner", new Date()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponseDTO(false, "Failed to update banner", new Date()));
        }
    }

    /**
     * Remove user's avatar
     */
    @DeleteMapping("/avatar")
    public ResponseEntity<MessageResponseDTO> removeAvatar(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponseDTO(false, "Unauthorized: No user logged in", new Date()));
        }

        try {
            String result = userProfileService.removeAvatar(principal.getName());
            return ResponseEntity.ok(new MessageResponseDTO(true, result, new Date()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponseDTO(false, "Failed to remove avatar", new Date()));
        }
    }

    /**
     * Remove user's banner
     */
    @DeleteMapping("/banner")
    public ResponseEntity<MessageResponseDTO> removeBanner(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponseDTO(false, "Unauthorized: No user logged in", new Date()));
        }

        try {
            String result = userProfileService.removeBanner(principal.getName());
            return ResponseEntity.ok(new MessageResponseDTO(true, result, new Date()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponseDTO(false, "Failed to remove banner", new Date()));
        }
    }
}
