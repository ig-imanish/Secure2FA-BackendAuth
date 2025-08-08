package com.bristoHQ.securetotp.controllers;

import java.util.Collections;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1")
public class GlobalAccessController {

    // @GetMapping("/auth/public")
    // public String publicAccessAuth() {

    //     return "Hello World!";
    // }

    @GetMapping("/auth/public")
    public Map<String, String> getUserIP(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        if ("0:0:0:0:0:0:0:1".equals(ipAddress)) {  // Convert IPv6 localhost to IPv4
            ipAddress = "127.0.0.1";
        }
        return Collections.singletonMap("ip", ipAddress);
    }

}
