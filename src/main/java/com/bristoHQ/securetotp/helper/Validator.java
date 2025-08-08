package com.bristoHQ.securetotp.helper;

import java.util.Map;

public class Validator {
    public static Map<String, Object> validateUsername(String username) {
        if (isContainsEmojis(username)) {
            return Map.of("error", "Username must not contain emojis.", "status", false);
        }

        Map<String, Object> result = new java.util.HashMap<>();

        // Rule 1: Not null or empty
        if (username == null || username.trim().isEmpty()) {
            result.put("error", "Username cannot be empty.");
            result.put("status", false);
            return result;
        }

        // Rule 2: Username length between 4 and 20
        if (username.length() < 4 || username.length() > 20) {
            result.put("error", "Username must be between 4 and 20 characters.");
            result.put("status", false);
            return result;
        }

        // Rule 3: Only one '@' allowed
        int atCount = 0;
        for (char c : username.toCharArray()) {
            if (c == '@')
                atCount++;
        }
        if (atCount > 1) {
            result.put("error", "Username must not contain more than one '@' character.");
            result.put("status", false);
            return result;
        }

        // Rule 4: Username must start with '@'
        if (!username.startsWith("@")) {
            result.put("error", "Username must start with '@'.");
            result.put("status", false);
            return result;
        }

        // Rule 5: No special characters except '_' and '@'
        if (!username.matches("^[A-Za-z0-9_@]+$")) {
            result.put("error", "Username can only contain letters, digits, '_', and one '@'.");
            result.put("status", false);
            return result;
        }

        // Rule 6: No consecutive underscores or '@'
        if (username.contains("__") || username.contains("@@")) {
            result.put("error", "Username must not contain consecutive underscores or '@'.");
            result.put("status", false);
            return result;
        }

        // Rule 7: Cannot end with '_' or '@'
        if (username.endsWith("_") || username.endsWith("@")) {
            result.put("error", "Username cannot end with '_' or '@'.");
            result.put("status", false);
            return result;
        }

        // Rule 8: No spaces allowed
        if (username.contains(" ")) {
            result.put("error", "Username cannot contain spaces.");
            result.put("status", false);
            return result;
        }

        result.put("error", "");
        result.put("status", true);
        return result;
    }

    public static Map<String, Object> validateEmail(String email) {
        if (isContainsEmojis(email)) {
            return Map.of("error", "Email must not contain emojis.", "status", false);
        }
        Map<String, Object> result = new java.util.HashMap<>();

        if (email == null || email.trim().isEmpty()) {
            result.put("error", "Email cannot be empty.");
            result.put("status", false);
            return result;
        }

        // Basic email pattern: local@domain.tld
        String emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        if (!email.matches(emailRegex)) {
            result.put("error", "Invalid email format.");
            result.put("status", false);
            return result;
        }

        // Length check
        if (email.length() > 254) {
            result.put("error", "Email must not exceed 254 characters.");
            result.put("status", false);
            return result;
        }

        // No spaces allowed
        if (email.contains(" ")) {
            result.put("error", "Email cannot contain spaces.");
            result.put("status", false);
            return result;
        }

        result.put("error", "");
        result.put("status", true);
        return result;
    }

    public static Map<String, Object> validatePassword(String password) {
        Map<String, Object> result = new java.util.HashMap<>();
        if(password == null || password.trim().isEmpty()) {
            result.put("error", "Password cannot be empty.");
            result.put("status", false);
            return result;
        }
        if (isContainsEmojis(password)) {
            return Map.of("error", "Password must not contain emojis.", "status", false);
        }

        if(password == null || password.length() > 20) {
            result.put("error", "Password must not exceed 20 characters.");
            result.put("status", false);
            return result;
        }

        if (password == null || password.length() < 8) {
            result.put("error", "Password must be at least 8 characters long.");
            result.put("status", false);
            return result;
        }

        // At least one uppercase, one lowercase, one digit, one special character
        if (!password.matches(".*[A-Z].*") || !password.matches(".*[a-z].*") ||
            !password.matches(".*[0-9].*") || !password.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) {
            result.put("error", "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character.");
            result.put("status", false);
            return result;
        }

        // No spaces allowed
        if (password.contains(" ")) {
            result.put("error", "Password cannot contain spaces.");
            result.put("status", false);
            return result;
        }

        // No common passwords (simple check)
        String[] commonPasswords = {"password", "123456", "12345678", "qwerty", "abc123"};
        for (String common : commonPasswords) {
            if (password.equalsIgnoreCase(common)) {
                result.put("error", "Password is too common.");
                result.put("status", false);
                return result;
            }
        }

        result.put("error", "");
        result.put("status", true);
        return result;
    }

    public static Boolean isContainsEmojis(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        // Regex to match emojis
        String emojiRegex = "[\\p{So}\\p{C}]";
        return input.matches(".*" + emojiRegex + ".*");
    }
}
