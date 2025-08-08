package com.bristoHQ.securetotp.controllers.admins;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminViewController {

    @GetMapping("/dashboard")
    public String adminDashboard() {
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String userManagement() {
        return "admin/dashboard"; // Same page, different section
    }

    @GetMapping("/analytics")
    public String analytics() {
        return "admin/dashboard"; // Same page, different section
    }
}
