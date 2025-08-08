package com.bristoHQ.securetotp.interceptors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.bristoHQ.securetotp.security.jwt.JwtUtilities;
import com.bristoHQ.securetotp.services.analytics.AnalyticsService;

@Component
public class AnalyticsInterceptor implements HandlerInterceptor {

    @Autowired
    private AnalyticsService analyticsService;

    @Autowired
    private JwtUtilities jwtUtilities;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull Object handler) {
        try {
            String page = request.getRequestURI();

            // Skip analytics tracking for certain paths
            if (shouldSkipTracking(page)) {
                return true;
            }

            String userId = getUserIdFromRequest(request);
            String ipAddress = getClientIpAddress(request);
            String userAgent = request.getHeader("User-Agent");
            String sessionId = request.getSession().getId();

            // Track page view asynchronously
            analyticsService.trackPageView(userId, page, ipAddress, userAgent, sessionId);
        } catch (Exception e) {
            // Don't fail the request if analytics tracking fails
            e.printStackTrace();
        }
        return true;
    }

    private boolean shouldSkipTracking(String path) {
        // Additional filtering for edge cases not covered by WebConfig
        // Skip static files and certain endpoints
        return path.endsWith(".css") ||
                path.endsWith(".js") ||
                path.endsWith(".ico") ||
                path.endsWith(".png") ||
                path.endsWith(".jpg") ||
                path.endsWith(".jpeg") ||
                path.endsWith(".gif") ||
                path.endsWith(".svg") ||
                path.endsWith(".woff") ||
                path.endsWith(".woff2") ||
                path.endsWith(".ttf") ||
                path.endsWith(".map") ||
                path.endsWith(".json") ||
                path.contains("/actuator/") || // Spring actuator endpoints
                path.contains("/health") || // Health check endpoints
                path.contains("/metrics") || // Metrics endpoints
                path.equals("/error"); // Error page (causing the spam)
    }

    private String getUserIdFromRequest(HttpServletRequest request) {
        try {
            // First try to get from request attributes (set by JWT filter)
            Object userIdAttr = request.getAttribute("userId");
            if (userIdAttr != null) {
                return userIdAttr.toString();
            }

            // Try to extract from JWT token in Authorization header
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    String username = jwtUtilities.extractUsername(token);
                    if (username != null && !username.isEmpty()) {
                        return username;
                    }
                } catch (Exception e) {
                    // Token might be invalid or expired, fall through to anonymous
                    System.err.println("Error extracting username from JWT: " + e.getMessage());
                }
            }

            // Try to get from session
            Object sessionUser = request.getSession().getAttribute("username");
            if (sessionUser != null) {
                return sessionUser.toString();
            }

        } catch (Exception e) {
            System.err.println("Error getting user ID from request: " + e.getMessage());
        }

        return "anonymous";
    }

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
