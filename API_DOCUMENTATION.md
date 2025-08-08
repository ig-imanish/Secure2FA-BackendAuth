# SecureTOTP Backend API Documentation

## Table of Contents

1. [Authentication Endpoints](#authentication-endpoints)
2. [User Management Endpoints](#user-management-endpoints)
3. [Admin Endpoints](#admin-endpoints)
4. [Super Admin Endpoints](#super-admin-endpoints)
5. [Premium Endpoints](#premium-endpoints)
6. [Profile Management Endpoints](#profile-management-endpoints)
7. [Global Endpoints](#global-endpoints)

---

## Authentication Endpoints

**Base URL:** `/api/v1/auth`

### 1. Register User

**POST** `/api/v1/auth/register`

Register a new user account.

**Request Body:**

```json
{
  "email": "user@example.com",
  "password": "SecurePassword123!",
  "fullName": "John Doe",
  "username": "@johndoe"
}
```

**Response (Success):**

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer "
}
```

**Response (Error):**

```json
{
  "success": false,
  "message": "User already exists",
  "timestamp": "2025-08-01T10:00:00.000Z"
}
```

---

### 2. Login User

**POST** `/api/v1/auth/login`

Authenticate user and get access token.

**Request Body:**

```json
{
  "emailOrUsername": "user@example.com",
  "password": "SecurePassword123!"
}
```

**Response (Success):**

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer "
}
```

**Response (Error):**

```json
{
  "success": false,
  "message": "Incorrect password",
  "timestamp": "2025-08-01T10:00:00.000Z"
}
```

---

### 3. Verify OTP

**POST** `/api/v1/auth/verify-otp`

Verify email with OTP code.

**Request Body:**

```json
{
  "email": "user@example.com",
  "otp": "123456"
}
```

**Response (Success):**

```json
{
  "success": true,
  "message": "Email verified successfully",
  "timestamp": "2025-08-01T10:00:00.000Z"
}
```

---

### 4. Resend OTP

**POST** `/api/v1/auth/resend-otp`

Resend OTP to user's email.

**Request Body:**

```json
{
  "email": "user@example.com"
}
```

**Response (Success):**

```json
{
  "success": true,
  "message": "OTP sent to your email",
  "timestamp": "2025-08-01T10:00:00.000Z"
}
```

---

### 5. Reset Password (Request)

**GET** `/api/v1/auth/reset-password?email=user@example.com`

Send password reset link to email.

**Response (Success):**

```json
{
  "success": true,
  "message": "Reset Link sent to your email for password reset",
  "timestamp": "2025-08-01T10:00:00.000Z"
}
```

---

### 6. Reset Password (Submit)

**POST** `/api/v1/auth/reset-password?token=resetToken&newPassword=NewPassword123!`

Reset password using token.

**Response (Success):**

```json
{
  "success": true,
  "message": "Password reset successfully",
  "timestamp": "2025-08-01T10:00:00.000Z"
}
```

---

### 7. Logout

**POST** `/api/v1/auth/logout`

Logout user and blacklist token.

**Headers:**

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Response (Success):**

```json
{
  "success": true,
  "message": "Logout successful, token blacklisted",
  "timestamp": "2025-08-01T10:00:00.000Z"
}
```

---

### 8. Check Authentication Status

**GET** `/api/v1/auth/isLogin`

Check if user is authenticated.

**Headers:**

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Response:**

```json
true
```

---

### 9. Validate Token

**POST** `/api/v1/auth/validateToken`

Validate JWT token and get user info.

**Headers:**

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Response (Success):**

```json
{
  "username": "@johndoe",
  "status": "OK"
}
```

---

## User Management Endpoints

**Base URL:** `/api/v1/users`

### 1. Get Current User

**GET** `/api/v1/users/me`

Get current authenticated user's information.

**Headers:**

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Response (Success):**

```json
{
  "id": "66b4791a303e7c61de58f2fd",
  "fullName": "John Doe",
  "email": "user@example.com",
  "username": "@johndoe",
  "verified": true,
  "premium": false,
  "userAvatar": "https://res.cloudinary.com/avatar.jpg",
  "userBanner": "https://res.cloudinary.com/banner.jpg",
  "recoveryPhone": "+1234567890",
  "accountCreatedAt": "2025-08-01T10:00:00.000Z"
}
```

---

### 2. Get All Users (Admin Only)

**GET** `/api/v1/users`

Get list of all users (requires ADMIN role).

**Headers:**

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Response (Success):**

```json
[
  {
    "id": "66b4791a303e7c61de58f2fd",
    "fullName": "John Doe",
    "email": "user@example.com",
    "username": "@johndoe",
    "verified": true,
    "premium": false
  }
]
```

---

### 3. Update Username

**PUT** `/api/v1/users/username?username=@newusername`

Update user's username.

**Headers:**

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Response (Success):**

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer "
}
```

---

### 4. Redeem Premium Code

**GET** `/api/v1/users/redeem?code=PREMIUM123`

Redeem premium access code.

**Headers:**

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Response (Success):**

```json
{
  "success": true,
  "message": "Premium access activated",
  "timestamp": "2025-08-01T10:00:00.000Z"
}
```

---

## Profile Management Endpoints

**Base URL:** `/api/v1/users/profile`

### 1. Get User Profile

**GET** `/api/v1/users/profile`

Get current user's profile information.

**Headers:**

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Response (Success):**

```json
{
  "id": "66b4791a303e7c61de58f2fd",
  "fullName": "John Doe",
  "email": "user@example.com",
  "username": "@johndoe",
  "verified": true,
  "premium": false,
  "userAvatar": "https://res.cloudinary.com/avatar.jpg",
  "userBanner": "https://res.cloudinary.com/banner.jpg",
  "recoveryPhone": "+1234567890"
}
```

---

### 2. Update Full Name

**PUT** `/api/v1/users/profile/fullname?fullName=John Smith`

Update user's full name.

**Headers:**

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Response (Success):**

```json
{
  "success": true,
  "message": "Full name updated successfully",
  "timestamp": "2025-08-01T10:00:00.000Z"
}
```

---

### 3. Update Email

**PUT** `/api/v1/users/profile/email?email=newemail@example.com`

Update user's email address.

**Headers:**

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Response (Success):**

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer "
}
```

---

### 4. Update Username

**PUT** `/api/v1/users/profile/username?username=@newusername`

Update user's username.

**Headers:**

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Response (Success):**

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer "
}
```

---

### 5. Update Avatar

**PUT** `/api/v1/users/profile/avatar`

Update user's profile avatar.

**Headers:**

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: multipart/form-data
```

**Request Body (Form Data):**

```
avatar: [image file]
```

**Response (Success):**

```json
{
  "success": true,
  "message": "Avatar updated successfully",
  "timestamp": "2025-08-01T10:00:00.000Z"
}
```

---

### 6. Update Banner

**PUT** `/api/v1/users/profile/banner`

Update user's profile banner.

**Headers:**

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: multipart/form-data
```

**Request Body (Form Data):**

```
banner: [image file]
```

**Response (Success):**

```json
{
  "success": true,
  "message": "Banner updated successfully",
  "timestamp": "2025-08-01T10:00:00.000Z"
}
```

---

### 7. Update Recovery Phone

**PUT** `/api/v1/users/profile/recovery-phone?recoveryPhone=+1234567890`

Update user's recovery phone number.

**Headers:**

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Response (Success):**

```json
{
  "success": true,
  "message": "Recovery phone updated successfully",
  "timestamp": "2025-08-01T10:00:00.000Z"
}
```

---

### 8. Remove Avatar

**DELETE** `/api/v1/users/profile/avatar`

Remove user's profile avatar.

**Headers:**

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Response (Success):**

```json
{
  "success": true,
  "message": "Avatar removed successfully",
  "timestamp": "2025-08-01T10:00:00.000Z"
}
```

---

### 9. Remove Banner

**DELETE** `/api/v1/users/profile/banner`

Remove user's profile banner.

**Headers:**

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Response (Success):**

```json
{
  "success": true,
  "message": "Banner removed successfully",
  "timestamp": "2025-08-01T10:00:00.000Z"
}
```

---

## JSON Profile Management Endpoints

**Base URL:** `/api/v1/users/profile/individual`

### 1. Update Full Name (JSON)

**PUT** `/api/v1/users/profile/individual/fullname`

Update user's full name using JSON body.

**Headers:**

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json
```

**Request Body:**

```json
{
  "fullName": "John Smith"
}
```

**Response (Success):**

```json
{
  "success": true,
  "message": "Full name updated successfully",
  "timestamp": "2025-08-01T10:00:00.000Z"
}
```

---

### 2. Update Email (JSON)

**PUT** `/api/v1/users/profile/individual/email`

Update user's email using JSON body.

**Headers:**

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json
```

**Request Body:**

```json
{
  "email": "newemail@example.com"
}
```

**Response (Success):**

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer "
}
```

---

### 3. Update Username (JSON)

**PUT** `/api/v1/users/profile/individual/username`

Update user's username using JSON body.

**Headers:**

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json
```

**Request Body:**

```json
{
  "username": "@newusername"
}
```

**Response (Success):**

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer "
}
```

---

### 4. Update Recovery Phone (JSON)

**PUT** `/api/v1/users/profile/individual/recovery-phone`

Update recovery phone using JSON body.

**Headers:**

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json
```

**Request Body:**

```json
{
  "recoveryPhone": "+1234567890"
}
```

**Response (Success):**

```json
{
  "success": true,
  "message": "Recovery phone updated successfully",
  "timestamp": "2025-08-01T10:00:00.000Z"
}
```

---

## Admin Endpoints

**Base URL:** `/api/v1/admins`

### 1. Admin Health Check

**GET** `/api/v1/admins`

Check admin endpoint availability.

**Headers:**

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Response:**

```
"admin supported"
```

---

### 2. Get Analytics

**GET** `/api/v1/admins/analytics`

Get comprehensive analytics data.

**Headers:**

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Response (Success):**

```json
{
  "totalUsers": 1250,
  "activeUsers": 856,
  "newUsersToday": 23,
  "newUsersThisWeek": 167,
  "totalViews": 45623,
  "todayViews": 1234,
  "thisWeekViews": 8567
}
```

---

### 3. Get Analytics Summary

**GET** `/api/v1/admins/analytics/summary`

Get summarized analytics data.

**Headers:**

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Response (Success):**

```json
{
  "totalUsers": 1250,
  "activeUsers": 856,
  "newUsersToday": 23,
  "newUsersThisWeek": 167,
  "totalViews": 45623,
  "todayViews": 1234,
  "thisWeekViews": 8567
}
```

---

### 4. Upload File

**POST** `/api/v1/admins/upload`

Upload file to Cloudinary.

**Headers:**

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: multipart/form-data
```

**Request Body (Form Data):**

```
file: [image file]
```

**Response (Success):**

```json
{
  "success": true,
  "message": "File uploaded successfully",
  "url": "https://res.cloudinary.com/your-cloud/image/upload/v1234567890/sample.jpg",
  "publicId": "sample_abc123",
  "format": "jpg",
  "width": 1920,
  "height": 1080,
  "size": 245760,
  "originalName": "my-image.jpg"
}
```

---

### 5. Get All Users (Paginated)

**GET** `/api/v1/admins/users?page=0&size=10&sortBy=accountCreatedAt&sortDir=desc`

Get paginated list of users.

**Headers:**

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Response (Success):**

```json
{
  "content": [
    {
      "id": "66b4791a303e7c61de58f2fd",
      "fullName": "John Doe",
      "email": "user@example.com",
      "username": "@johndoe",
      "verified": true,
      "premium": false,
      "banned": false,
      "accountCreatedAt": "2025-08-01T10:00:00.000Z"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalElements": 1250,
  "totalPages": 125
}
```

---

### 6. Search Users

**GET** `/api/v1/admins/users/search?query=john`

Search users by name, email, or username.

**Headers:**

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Response (Success):**

```json
[
  {
    "id": "66b4791a303e7c61de58f2fd",
    "fullName": "John Doe",
    "email": "user@example.com",
    "username": "@johndoe",
    "verified": true,
    "premium": false,
    "banned": false
  }
]
```

---

### 7. Get User Details

**GET** `/api/v1/admins/users/{userId}`

Get detailed information about a specific user.

**Headers:**

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Response (Success):**

```json
{
  "id": "66b4791a303e7c61de58f2fd",
  "fullName": "John Doe",
  "email": "user@example.com",
  "username": "@johndoe",
  "verified": true,
  "premium": false,
  "banned": false,
  "userAvatar": "https://res.cloudinary.com/avatar.jpg",
  "userBanner": "https://res.cloudinary.com/banner.jpg",
  "recoveryPhone": "+1234567890",
  "accountCreatedAt": "2025-08-01T10:00:00.000Z"
}
```

---

### 8. Perform User Action

**POST** `/api/v1/admins/users/{userId}/actions`

Perform administrative action on user.

**Headers:**

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json
```

**Request Body:**

```json
{
  "action": "BAN",
  "reason": "Violation of terms of service",
  "duration": null
}
```

**Response (Success):**

```json
{
  "message": "User banned successfully",
  "action": "BAN",
  "userId": "66b4791a303e7c61de58f2fd"
}
```

---

### 9. Ban User

**PUT** `/api/v1/admins/users/{userId}/ban?reason=Violation of terms`

Ban a user account.

**Headers:**

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Response (Success):**

```json
{
  "message": "User banned successfully"
}
```

---

### 10. Unban User

**PUT** `/api/v1/admins/users/{userId}/unban`

Unban a user account.

**Headers:**

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Response (Success):**

```json
{
  "message": "User unbanned successfully"
}
```

---

### 11. Force Logout User

**POST** `/api/v1/admins/users/{userId}/force-logout`

Force logout a user by blacklisting their tokens.

**Headers:**

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Response (Success):**

```json
{
  "message": "User forcefully logged out"
}
```

---

### 12. Verify User

**PUT** `/api/v1/admins/users/{userId}/verify`

Manually verify a user account.

**Headers:**

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Response (Success):**

```json
{
  "message": "User verified successfully"
}
```

---

### 13. Make User Premium

**PUT** `/api/v1/admins/users/{userId}/premium`

Grant premium access to user.

**Headers:**

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Response (Success):**

```json
{
  "message": "User premium status activated"
}
```

---

### 14. Remove Premium Access

**DELETE** `/api/v1/admins/users/{userId}/premium`

Remove premium access from user.

**Headers:**

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Response (Success):**

```json
{
  "message": "User premium status removed"
}
```

---

### 15. Delete User Secrets

**DELETE** `/api/v1/admins/users/{userId}/secrets`

Delete user's TOTP secrets.

**Headers:**

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Response (Success):**

```json
{
  "message": "User secrets deleted successfully"
}
```

---

### 16. Delete All User Secrets

**DELETE** `/api/v1/admins/users/{userId}/secrets/all`

Delete all user's secrets and data.

**Headers:**

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Response (Success):**

```json
{
  "message": "All user secrets deleted successfully"
}
```

---

### 17. Delete User

**DELETE** `/api/v1/admins/users/{userId}`

Permanently delete user account.

**Headers:**

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Response (Success):**

```json
{
  "message": "User deleted successfully"
}
```

---

### Legacy Admin Endpoints

### 18. Get All Users (Legacy)

**GET** `/api/v1/admins/getAllUsers`

**Response:** Array of UserDTO objects

---

### 19. Get User by ID (Legacy)

**GET** `/api/v1/admins/getAllUsers/{id}`

**Response:** UserDTO object

---

### 20. Get User by Email (Legacy)

**GET** `/api/v1/admins/getAllUsers/email/{email}`

**Response:** UserDTO object

---

### 21. Get User by Username (Legacy)

**GET** `/api/v1/admins/getAllUsers/username/{username}`

**Response:** UserDTO object

---

### 22. Get Users by Role (Legacy)

**GET** `/api/v1/admins/getAllUsers/role/{role}`

**Response:** Array of Role objects

---

### 23. Generate JWT Token (Legacy)

**POST** `/api/v1/admins/generatejwtToken/{emailOrUsername}`

**Response:** JWT token string

---

### 24. Get User by Email or Username (Legacy)

**GET** `/api/v1/admins/byEmailUsername/{emailOrUsername}`

**Response:** UserDTO object

---

## Super Admin Endpoints

**Base URL:** `/api/v1/superadmins`

### 1. Super Admin Health Check

**GET** `/api/v1/superadmins`

Check super admin endpoint availability.

**Headers:**

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Response:**

```
"superadmin supported"
```

---

## Premium Endpoints

**Base URL:** `/api/v1/premium`

### 1. Generate Redeem Code

**POST** `/api/v1/premium/generate`

Generate a new premium redeem code.

**Headers:**

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Response (Success):**

```json
{
  "id": "66b4791a303e7c61de58f2fd",
  "redeemCode": "PREM-ABC123-XYZ789",
  "used": false,
  "createdAt": "2025-08-01T10:00:00.000Z",
  "usedBy": null,
  "usedAt": null
}
```

---

### 2. Get Redeem Code by Code

**GET** `/api/v1/premium/code/{code}`

Get details of a specific redeem code.

**Headers:**

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Response (Success):**

```json
{
  "id": "66b4791a303e7c61de58f2fd",
  "redeemCode": "PREM-ABC123-XYZ789",
  "used": true,
  "createdAt": "2025-08-01T10:00:00.000Z",
  "usedBy": "user@example.com",
  "usedAt": "2025-08-01T11:00:00.000Z"
}
```

---

### 3. Get All Redeem Codes

**GET** `/api/v1/premium/code`

Get list of all redeem codes.

**Headers:**

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Response (Success):**

```json
[
  {
    "id": "66b4791a303e7c61de58f2fd",
    "redeemCode": "PREM-ABC123-XYZ789",
    "used": true,
    "createdAt": "2025-08-01T10:00:00.000Z",
    "usedBy": "user@example.com",
    "usedAt": "2025-08-01T11:00:00.000Z"
  }
]
```

---

## Global Endpoints

**Base URL:** `/api/v1`

### 1. Get User IP

**GET** `/api/v1/auth/public`

Get user's IP address (public endpoint).

**Response (Success):**

```json
{
  "ip": "192.168.1.100"
}
```

---

## Common Response Formats

### Success Response (MessageResponseDTO)

```json
{
  "success": true,
  "message": "Operation completed successfully",
  "timestamp": "2025-08-01T10:00:00.000Z"
}
```

### Error Response (MessageResponseDTO)

```json
{
  "success": false,
  "message": "Error description",
  "timestamp": "2025-08-01T10:00:00.000Z"
}
```

### Bearer Token Response

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer "
}
```

### UserDTO Response

```json
{
  "id": "66b4791a303e7c61de58f2fd",
  "fullName": "John Doe",
  "email": "user@example.com",
  "username": "@johndoe",
  "verified": true,
  "premium": false,
  "userAvatar": "https://res.cloudinary.com/avatar.jpg",
  "userBanner": "https://res.cloudinary.com/banner.jpg",
  "recoveryPhone": "+1234567890",
  "accountCreatedAt": "2025-08-01T10:00:00.000Z"
}
```

---

## Authentication & Authorization

### JWT Token Usage

Most endpoints require authentication. Include the JWT token in the Authorization header:

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

### Role-Based Access Control

- **USER**: Basic user endpoints (`/api/v1/users/me`, profile updates)
- **ADMIN**: Administrative endpoints (`/api/v1/admins/*`)
- **SUPER_ADMIN**: Super administrative endpoints (`/api/v1/superadmins/*`)

---

## Error Codes

| HTTP Status | Description                            |
| ----------- | -------------------------------------- |
| 200         | Success                                |
| 400         | Bad Request - Invalid input            |
| 401         | Unauthorized - Authentication required |
| 403         | Forbidden - Insufficient permissions   |
| 404         | Not Found - Resource doesn't exist     |
| 500         | Internal Server Error                  |

---

## Rate Limiting

The API implements rate limiting with the following limits:

- **Authentication endpoints**: 5 requests per minute
- **API endpoints**: 100 requests per minute
- **General endpoints**: 50 requests per minute

Rate limit headers are included in responses:

- `X-RateLimit-Remaining`: Remaining requests in current window
- `Retry-After`: Seconds to wait when rate limited (429 status)

---

This documentation covers all the REST API endpoints available in the SecureTOTP backend authentication system. Each endpoint includes the required authentication, request/response formats, and example payloads.
