package com.bristoHQ.securetotp.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BearerToken {
    private String accessToken;
    private String tokenType;
    private String redirectUrl;
    
    public BearerToken(String accessToken, String tokenType) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.redirectUrl = null;
    }
}
