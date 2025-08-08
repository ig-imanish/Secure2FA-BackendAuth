package com.bristoHQ.securetotp.controllers.admins;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.bristoHQ.securetotp.dto.analytics.AdminAnalyticsDTO;
import com.bristoHQ.securetotp.dto.user.AdminUserActionDTO;
import com.bristoHQ.securetotp.dto.user.UserDTO;
import com.bristoHQ.securetotp.dto.user.UserManagementDTO;
import com.bristoHQ.securetotp.models.role.Role;
import com.bristoHQ.securetotp.models.role.RoleName;
import com.bristoHQ.securetotp.security.jwt.JwtUtilities;
import com.bristoHQ.securetotp.services.admin.AdminUserManagementService;
import com.bristoHQ.securetotp.services.analytics.AnalyticsService;
import com.bristoHQ.securetotp.services.role.RoleService;
import com.bristoHQ.securetotp.services.storage.CloudinaryService;
import com.bristoHQ.securetotp.services.user.UserService;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/v1/admins")
@AllArgsConstructor
public class AdminRestController {
    private final UserService userService;
    private final RoleService roleService;
    private final JwtUtilities jwtUtilities;
    private final AdminUserManagementService adminUserManagementService;
    private final AnalyticsService analyticsService;
    private final CloudinaryService cloudinaryService;

    // Basic admin endpoint
    @GetMapping
    public String sayHello() {
        return "admin supported";
    }

    // Analytics endpoints
    @GetMapping("/analytics")
    public ResponseEntity<AdminAnalyticsDTO> getAnalytics() {
        AdminAnalyticsDTO analytics = analyticsService.getAdminAnalytics();
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/analytics/summary")
    public ResponseEntity<Map<String, Object>> getAnalyticsSummary() {
        AdminAnalyticsDTO analytics = analyticsService.getAdminAnalytics();
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalUsers", analytics.getTotalUsers());
        summary.put("activeUsers", analytics.getActiveUsers());
        summary.put("newUsersToday", analytics.getNewUsersToday());
        summary.put("newUsersThisWeek", analytics.getNewUsersThisWeek());
        summary.put("totalViews", analytics.getTotalViews());
        summary.put("todayViews", analytics.getTodayViews());
        summary.put("thisWeekViews", analytics.getThisWeekViews());
        return ResponseEntity.ok(summary);
    }

    // File Upload endpoint
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "File is required and cannot be empty");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Upload file to Cloudinary
            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = cloudinaryService.uploadImage(file);

            // Extract relevant information
            String imageUrl = uploadResult.get("url").toString();
            String publicId = uploadResult.get("public_id").toString();
            String format = uploadResult.get("format").toString();
            Object width = uploadResult.get("width");
            Object height = uploadResult.get("height");
            Object bytes = uploadResult.get("bytes");

            // Create response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "File uploaded successfully");
            response.put("url", imageUrl);
            response.put("publicId", publicId);
            response.put("format", format);
            response.put("width", width);
            response.put("height", height);
            response.put("size", bytes);
            response.put("originalName", file.getOriginalFilename());

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to upload file: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    // User Management endpoints
    @GetMapping("/users")
    public ResponseEntity<Page<UserManagementDTO>> getAllUsersWithPagination(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "accountCreatedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Page<UserManagementDTO> users = adminUserManagementService.getAllUsersWithPagination(page, size, sortBy,
                sortDir);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/search")
    public ResponseEntity<List<UserManagementDTO>> searchUsers(@RequestParam String query) {
        List<UserManagementDTO> users = adminUserManagementService.searchUsers(query);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserManagementDTO> getUserDetails(@PathVariable String userId) {
        UserManagementDTO user = adminUserManagementService.getUserDetails(userId);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/users/{userId}/actions")
    public ResponseEntity<Map<String, String>> performUserAction(
            @PathVariable String userId,
            @RequestBody AdminUserActionDTO actionDTO) {
        String result = adminUserManagementService.performUserAction(userId, actionDTO);
        Map<String, String> response = new HashMap<>();
        response.put("message", result);
        response.put("action", actionDTO.getAction());
        response.put("userId", userId.toString());
        return ResponseEntity.ok(response);
    }

    // Specific user management actions for convenience
    @PutMapping("/users/{userId}/ban")
    public ResponseEntity<Map<String, String>> banUser(
            @PathVariable String userId,
            @RequestParam(required = false) String reason) {
        AdminUserActionDTO actionDTO = new AdminUserActionDTO("BAN", reason, null);
        String result = adminUserManagementService.performUserAction(userId, actionDTO);
        Map<String, String> response = new HashMap<>();
        response.put("message", result);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/users/{userId}/unban")
    public ResponseEntity<Map<String, String>> unbanUser(@PathVariable String userId) {
        AdminUserActionDTO actionDTO = new AdminUserActionDTO("UNBAN", null, null);
        String result = adminUserManagementService.performUserAction(userId, actionDTO);
        Map<String, String> response = new HashMap<>();
        response.put("message", result);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/users/{userId}/force-logout")
    public ResponseEntity<Map<String, String>> forceLogoutUser(@PathVariable String userId) {
        AdminUserActionDTO actionDTO = new AdminUserActionDTO("FORCE_LOGOUT", null, null);
        String result = adminUserManagementService.performUserAction(userId, actionDTO);
        Map<String, String> response = new HashMap<>();
        response.put("message", result);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/users/{userId}/verify")
    public ResponseEntity<Map<String, String>> verifyUser(@PathVariable String userId) {
        AdminUserActionDTO actionDTO = new AdminUserActionDTO("VERIFY_USER", null, null);
        String result = adminUserManagementService.performUserAction(userId, actionDTO);
        Map<String, String> response = new HashMap<>();
        response.put("message", result);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/users/{userId}/premium")
    public ResponseEntity<Map<String, String>> makePremium(@PathVariable String userId) {
        AdminUserActionDTO actionDTO = new AdminUserActionDTO("MAKE_PREMIUM", null, null);
        String result = adminUserManagementService.performUserAction(userId, actionDTO);
        Map<String, String> response = new HashMap<>();
        response.put("message", result);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/users/{userId}/premium")
    public ResponseEntity<Map<String, String>> removePremium(@PathVariable String userId) {
        AdminUserActionDTO actionDTO = new AdminUserActionDTO("REMOVE_PREMIUM", null, null);
        String result = adminUserManagementService.performUserAction(userId, actionDTO);
        Map<String, String> response = new HashMap<>();
        response.put("message", result);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/users/{userId}/secrets")
    public ResponseEntity<Map<String, String>> deleteUserSecrets(@PathVariable String userId) {
        AdminUserActionDTO actionDTO = new AdminUserActionDTO("DELETE_USER_SECRETS", null, null);
        String result = adminUserManagementService.performUserAction(userId, actionDTO);
        Map<String, String> response = new HashMap<>();
        response.put("message", result);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/users/{userId}/secrets/all")
    public ResponseEntity<Map<String, String>> deleteAllUserSecrets(@PathVariable String userId) {
        AdminUserActionDTO actionDTO = new AdminUserActionDTO("DELETE_ALL_SECRETS", null, null);
        String result = adminUserManagementService.performUserAction(userId, actionDTO);
        Map<String, String> response = new HashMap<>();
        response.put("message", result);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable String userId) {
        AdminUserActionDTO actionDTO = new AdminUserActionDTO("DELETE", null, null);
        String result = adminUserManagementService.performUserAction(userId, actionDTO);
        Map<String, String> response = new HashMap<>();
        response.put("message", result);
        return ResponseEntity.ok(response);
    }

    // Profile update endpoints for admin to update any user's profile
    @PutMapping("/users/{userId}/fullname")
    public ResponseEntity<Map<String, String>> updateUserFullName(
            @PathVariable String userId,
            @RequestParam String fullName) {
        try {
            String result = adminUserManagementService.updateUserFullName(userId, fullName);
            Map<String, String> response = new HashMap<>();
            response.put("message", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PutMapping("/users/{userId}/email")
    public ResponseEntity<Map<String, String>> updateUserEmail(
            @PathVariable String userId,
            @RequestParam String email) {
        try {
            String result = adminUserManagementService.updateUserEmail(userId, email);
            Map<String, String> response = new HashMap<>();
            response.put("message", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PutMapping("/users/{userId}/username")
    public ResponseEntity<Map<String, String>> updateUserUsername(
            @PathVariable String userId,
            @RequestParam String username) {
        try {
            String result = adminUserManagementService.updateUserUsername(userId, username);
            Map<String, String> response = new HashMap<>();
            response.put("message", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PutMapping("/users/{userId}/recovery-phone")
    public ResponseEntity<Map<String, String>> updateUserRecoveryPhone(
            @PathVariable String userId,
            @RequestParam String recoveryPhone) {
        try {
            String result = adminUserManagementService.updateUserRecoveryPhone(userId, recoveryPhone);
            Map<String, String> response = new HashMap<>();
            response.put("message", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PutMapping("/users/{userId}/password")
    public ResponseEntity<Map<String, String>> updateUserPassword(
            @PathVariable String userId,
            @RequestParam String newPassword) {
        try {
            String result = adminUserManagementService.updateUserPassword(userId, newPassword);
            Map<String, String> response = new HashMap<>();
            response.put("message", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // Legacy endpoints (keeping for backward compatibility)
    @GetMapping("/getAllUsers")
    public List<UserDTO> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/getAllUsers/{id}")
    public UserDTO getUserById(@PathVariable String id) {
        return userService.findById(id);
    }

    @GetMapping("/getAllUsers/email/{email}")
    public UserDTO getUserByEmail(@PathVariable String email) {
        return userService.findByEmail(email);
    }

    @GetMapping("/getAllUsers/username/{username}")
    public UserDTO getUserByUsername(@PathVariable String username) {
        return userService.findByUsername(username);
    }

    @GetMapping("/getAllUsers/role/{role}")
    public List<Role> getUsersByRole(@PathVariable String role) {
        if (role.equals("ADMIN")) {
            return roleService.getRoleByRoleName(RoleName.ADMIN);
        } else if (role.equals("USER")) {
            return roleService.getRoleByRoleName(RoleName.USER);
        } else if (role.equals("SUPER_ADMIN")) {
            return roleService.getRoleByRoleName(RoleName.SUPERADMIN);
        }
        return List.of(new Role(null, RoleName.USER, null));
    }

    @PostMapping("/generatejwtToken/{emailOrUsername}")
    public String generateJwtTokenForUser(@PathVariable String emailOrUsername) {
        return jwtUtilities.generateToken(emailOrUsername, List.of("USER"));
    }

    @GetMapping("/byEmailUsername/{emailOrUsername}")
    public ResponseEntity<UserDTO> getUserByEmailOrUsername(@PathVariable String emailOrUsername) {
        UserDTO user = userService.findByEmailOrUsername(emailOrUsername, emailOrUsername);
        return ResponseEntity.ok(user);
    }
}
