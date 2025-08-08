# Validation Rules for Frontend Implementation

This document outlines the validation rules for username, email, and password fields as implemented in the `Validator` class from the `com.bristoHQ.securetotp.helper` package. These rules should be implemented in the frontend to ensure consistency with backend validation.

## Username Validation Rules

1. **Not Empty**: Username must not be null or empty (after trimming whitespace).
   - Error: "Username cannot be empty."
2. **Length**: Must be between 4 and 20 characters.
   - Error: "Username must be between 4 and 20 characters."
3. **Single '@'**: Must contain exactly one '@' character.
   - Error: "Username must not contain more than one '@' character."
4. **Starts with '@'**: Must start with '@'.
   - Error: "Username must start with '@'."
5. **Allowed Characters**: Can only contain letters (A-Z, a-z), digits (0-9), '_', and one '@'.
   - Error: "Username can only contain letters, digits, '_', and one '@'."
6. **No Consecutive Special Characters**: No consecutive underscores ('__') or '@' ('@@').
   - Error: "Username must not contain consecutive underscores or '@'."
7. **Ending Character**: Cannot end with '_' or '@'.
   - Error: "Username cannot end with '_' or '@'."
8. **No Spaces**: Cannot contain spaces.
   - Error: "Username cannot contain spaces."
9. **No Emojis**: Cannot contain emojis or special Unicode characters.
   - Error: "Username must not contain emojis."

## Email Validation Rules

1. **Not Empty**: Email must not be null or empty (after trimming whitespace).
   - Error: "Email cannot be empty."
2. **Format**: Must follow the basic email pattern: `local@domain.tld`.
   - Regex: `^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$`
   - Error: "Invalid email format."
3. **Length**: Must not exceed 254 characters.
   - Error: "Email must not exceed 254 characters."
4. **No Spaces**: Cannot contain spaces.
   - Error: "Email cannot contain spaces."
5. **No Emojis**: Cannot contain emojis or special Unicode characters.
   - Error: "Email must not contain emojis."

## Password Validation Rules

1. **Not Empty**: Password must not be null or empty (after trimming whitespace).
   - Error: "Password cannot be empty."
2. **Length**: Must be between 8 and 20 characters.
   - Error: "Password must be at least 8 characters long." or "Password must not exceed 20 characters."
3. **Character Requirements**:
   - At least one uppercase letter (A-Z).
   - At least one lowercase letter (a-z).
   - At least one digit (0-9).
   - At least one special character from: `!@#$%^&*(),.?":{}|<>`.
   - Error: "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character."
4. **No Spaces**: Cannot contain spaces.
   - Error: "Password cannot contain spaces."
5. **No Common Passwords**: Cannot be one of the following common passwords (case-insensitive):
   - "password", "123456", "12345678", "qwerty", "abc123".
   - Error: "Password is too common."
6. **No Emojis**: Cannot contain emojis or special Unicode characters.
   - Error: "Password must not contain emojis."