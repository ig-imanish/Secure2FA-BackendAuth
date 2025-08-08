package com.bristoHQ.securetotp.dto.user;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserManagementDTO {
    private String id;
    private String fullName;
    private String username;
    private String email;
    private boolean verified;
    private boolean premium;
    private Date accountCreatedAt;
    private Date lastActiveAt;
    private String status; // ACTIVE, BANNED, SUSPENDED
    private String provider;
    private String userAvatar;
    private int totalSecrets;
    private int redeemCodeCount;
}
