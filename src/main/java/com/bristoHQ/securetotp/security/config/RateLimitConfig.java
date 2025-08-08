package com.bristoHQ.securetotp.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "rate-limit")
@Data
public class RateLimitConfig {

    /**
     * Rate limit for login and authentication endpoints (requests per minute)
     */
    private int loginRequestsPerMinute = 5;

    /**
     * Rate limit for API endpoints (requests per minute)
     */
    private int apiRequestsPerMinute = 100;

    /**
     * Rate limit for general endpoints (requests per minute)
     */
    private int generalRequestsPerMinute = 50;

    /**
     * Enable or disable rate limiting
     */
    private boolean enabled = true;

    /**
     * Time window for rate limiting in minutes
     */
    private int windowSizeInMinutes = 1;
}
