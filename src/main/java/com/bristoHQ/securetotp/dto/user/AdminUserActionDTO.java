package com.bristoHQ.securetotp.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminUserActionDTO {
    private String action; // BAN, UNBAN, DELETE, FORCE_LOGOUT, UPDATE_AVATAR, etc.
    private String reason;
    private Object data; // Additional data for the action
}
