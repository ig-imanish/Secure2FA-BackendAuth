package com.bristoHQ.securetotp.controllers.users;

import java.security.Principal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bristoHQ.securetotp.dto.MessageResponseDTO;
import com.bristoHQ.securetotp.dto.auth.BearerToken;
import com.bristoHQ.securetotp.dto.user.AdminUserActionDTO;
import com.bristoHQ.securetotp.dto.user.UserDTO;
import com.bristoHQ.securetotp.helper.Validator;
import com.bristoHQ.securetotp.services.admin.AdminUserManagementService;
import com.bristoHQ.securetotp.services.premium.PremiumService;
import com.bristoHQ.securetotp.services.user.UserServiceImpl;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class PrivateUserController {

    private final UserServiceImpl userService;
    private final PremiumService premiumService;

    private final AdminUserManagementService adminUserManagementService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDTO>> getUsers() {
        return ResponseEntity.ok(userService.getAllUsers() != null ? userService.getAllUsers() : List.of());
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Principal principal) {
        if (principal == null || principal.getName() == null) {
            return ResponseEntity.status(401).body(new MessageResponseDTO(false, "Unauthorized: No user logged in", new Date()));
        }
        System.out.println("Principal Name: " + principal.getName());
        UserDTO user = userService.findByEmailOrUsername(principal.getName(), principal.getName());
        System.out.println("Sending /api/v1/users/me user: " + user);
        return user != null ? ResponseEntity.ok(user) : ResponseEntity.status(404).body(new MessageResponseDTO(false, "User not found", new Date()));
    }

    @GetMapping("/redeem")
    public ResponseEntity<MessageResponseDTO> redeemPremium(@RequestParam String code, Principal principal) {
        return ResponseEntity.ok(premiumService.redeemPremium(code, principal.getName()));
    }

    @GetMapping("/info")
    public String displayUserInfo() {
        var securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();
        return (authentication == null) ? "No authentication found!" : "Authenticated user: " + authentication.getPrincipal().toString();
    }

    @PutMapping("/username")
    public ResponseEntity<?> updateUsername(@RequestParam String username, Principal principal) {
        if (username == null || username.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponseDTO(false, "Username cannot be empty", new Date()));
        }
        if (!username.startsWith("@")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponseDTO(false, "Username must start with '@'", new Date()));
        }
        if(principal == null || principal.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponseDTO(false, "Unauthorized: No user logged in", new Date()));
        }

        Map<String, Object> validation = Validator.validateUsername(username);
        if (!validation.get("status").equals(true)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponseDTO(false, validation.get("error").toString(), new Date()));
        }

        UserDTO user = userService.findByEmailOrUsername(principal.getName(), principal.getName());
        if (user == null) {
            return ResponseEntity.status(404).body(new MessageResponseDTO(false, "User not found", new Date()));
        }
        if (user.getUsername().equalsIgnoreCase(username)) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(new MessageResponseDTO(false, "Username is same as current username", new Date()));
        }
        if (userService.findByUsername(username) != null) {
            return ResponseEntity.status(HttpStatus.IM_USED).body(new MessageResponseDTO(false, "Username already exists", new Date()));
        }

        userService.updateUsername(principal.getName(), username);
        return ResponseEntity.ok(new BearerToken(userService.newJwtToken(username), "Bearer "));
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable String userId) {
        AdminUserActionDTO actionDTO = new AdminUserActionDTO("DELETE", null, null);
        String result = adminUserManagementService.performUserAction(userId, actionDTO);
        Map<String, String> response = new HashMap<>();
        response.put("message", result);
        return ResponseEntity.ok(response);
    }
}
