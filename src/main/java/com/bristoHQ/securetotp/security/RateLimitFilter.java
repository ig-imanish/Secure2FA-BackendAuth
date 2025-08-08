package com.bristoHQ.securetotp.security;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.bristoHQ.securetotp.security.config.RateLimitConfig;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.Refill;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RateLimitFilter implements Filter {

    @Autowired
    private RateLimitConfig rateLimitConfig;

    // Cache for storing buckets per IP and endpoint type
    private final ConcurrentHashMap<String, Bucket> loginBuckets = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Bucket> generalBuckets = new ConcurrentHashMap<>();

    // Login and sensitive endpoints
    private static final String[] LOGIN_ENDPOINTS = {
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/verify-otp",
            "/api/v1/auth/resend-otp",
            "/api/v1/auth/reset-password"
    };

    // Endpoints that need stricter rate limiting (but not as strict as login)
    private static final String[] STRICT_ENDPOINTS = {
            "/api/v1/users/",
            "/api/v1/admins/",
            "/api/v1/superadmins/"
    };

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // Check if rate limiting is enabled
        if (!rateLimitConfig.isEnabled()) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String clientIp = getClientIpAddress(httpRequest);
        String requestPath = httpRequest.getRequestURI();

        // Determine the type of endpoint and get appropriate bucket
        Bucket bucket = getBucketForRequest(clientIp, requestPath);

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            // Add remaining requests info to response headers
            httpResponse.setHeader("X-RateLimit-Remaining", String.valueOf(probe.getRemainingTokens()));
            chain.doFilter(request, response);
        } else {
            // Rate limit exceeded
            long waitForRefill = probe.getNanosToWaitForRefill() / 1_000_000_000;
            httpResponse.setStatus(429);
            httpResponse.setHeader("Retry-After", String.valueOf(waitForRefill));
            httpResponse.setHeader("X-RateLimit-Remaining", "0");
            httpResponse.setContentType("application/json");

            String endpointType = getEndpointType(requestPath);
            httpResponse.getWriter().write(
                    String.format(
                            "{\"error\": \"Rate limit exceeded\", \"message\": \"Too many %s requests from your IP. Please try again after %d seconds.\", \"retryAfter\": %d}",
                            endpointType, waitForRefill, waitForRefill));
        }
    }

    /**
     * Get appropriate bucket based on request type and client IP
     */
    private Bucket getBucketForRequest(String clientIp, String requestPath) {
        if (isLoginEndpoint(requestPath)) {
            return loginBuckets.computeIfAbsent(clientIp, this::createLoginBucket);
        } else if (isStrictEndpoint(requestPath)) {
            return generalBuckets.computeIfAbsent(clientIp + "_strict", this::createStrictBucket);
        } else {
            return generalBuckets.computeIfAbsent(clientIp, this::createGeneralBucket);
        }
    }

    /**
     * Check if the request is for a login endpoint
     */
    private boolean isLoginEndpoint(String requestPath) {
        for (String endpoint : LOGIN_ENDPOINTS) {
            if (requestPath.startsWith(endpoint)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the request is for a strict endpoint (API endpoints that need more
     * restriction)
     */
    private boolean isStrictEndpoint(String requestPath) {
        for (String endpoint : STRICT_ENDPOINTS) {
            if (requestPath.startsWith(endpoint)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get endpoint type for error messages
     */
    private String getEndpointType(String requestPath) {
        if (isLoginEndpoint(requestPath)) {
            return "authentication";
        } else if (isStrictEndpoint(requestPath)) {
            return "API";
        } else {
            return "general";
        }
    }

    /**
     * Create bucket for login endpoints (very restrictive)
     */
    private Bucket createLoginBucket(String key) {
        int limit = rateLimitConfig.getLoginRequestsPerMinute();
        Bandwidth bandwidth = Bandwidth.classic(limit,
                Refill.intervally(limit, Duration.ofMinutes(rateLimitConfig.getWindowSizeInMinutes())));
        return Bucket.builder().addLimit(bandwidth).build();
    }

    /**
     * Create bucket for strict endpoints (moderately restrictive)
     */
    private Bucket createStrictBucket(String key) {
        int limit = rateLimitConfig.getApiRequestsPerMinute();
        Bandwidth bandwidth = Bandwidth.classic(limit,
                Refill.intervally(limit, Duration.ofMinutes(rateLimitConfig.getWindowSizeInMinutes())));
        return Bucket.builder().addLimit(bandwidth).build();
    }

    /**
     * Create bucket for general endpoints (less restrictive)
     */
    private Bucket createGeneralBucket(String key) {
        int limit = rateLimitConfig.getGeneralRequestsPerMinute();
        Bandwidth bandwidth = Bandwidth.classic(limit,
                Refill.intervally(limit, Duration.ofMinutes(rateLimitConfig.getWindowSizeInMinutes())));
        return Bucket.builder().addLimit(bandwidth).build();
    }

    /**
     * Get client IP address considering various headers
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}
