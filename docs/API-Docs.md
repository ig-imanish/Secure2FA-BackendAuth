# SecureTOTP Backend Authentication API Documentation

## Overview

SecureTOTP Backend Authentication API provides comprehensive endpoints for user management, authentication, TOTP operations, and administrative functions with JWT-based security.

- **Version**: 1.0
- **Base URL**: `http://localhost:9000`
- **Protocol**: HTTPS (recommended for production)
- **Authentication**: JWT Bearer tokens

## Quick Start

### 1. Register a New User

```bash
curl -X POST http://localhost:9000/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "John Doe",
    "username": "johndoe",
    "email": "john@example.com",
    "password": "SecurePass123!"
  }'
```

### 2. Login and Get Token

```bash
curl -X POST http://localhost:9000/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "emailOrUsername": "johndoe",
    "password": "SecurePass123!"
  }'
```

### 3. Access Protected Endpoints

```bash
curl -X GET http://localhost:9000/api/v1/users/me \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Authentication

### JWT Token Format

All protected endpoints require a Bearer token in the `Authorization` header:

```http
Authorization: Bearer <your-jwt-token>
```

### Token Lifecycle

- **Access Token**: 24 hours validity
- **Refresh Token**: 7 days validity
- **Automatic Refresh**: Use refresh endpoint before expiration

## API Endpoints

### Authentication Endpoints

#### POST /api/v1/auth/register

Register a new user account.

**Request Body:**

```json
{
  "fullName": "John Doe",
  "username": "johndoe",
  "email": "john@example.com",
  "password": "SecurePass123!",
  "provider": "local"
}
```

**Response (200 OK):**

```json
{
  "message": "User registered successfully. Please verify your email.",
  "timestamp": "2024-01-15T10:30:00Z",
  "success": true
}
```

#### POST /api/v1/auth/login

Authenticate user and receive JWT token.

**Request Body:**

```json
{
  "emailOrUsername": "johndoe",
  "password": "SecurePass123!"
}
```

**Response (200 OK):**

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "id": "user-id-123",
  "username": "johndoe",
  "email": "john@example.com",
  "roles": ["USER"]
}
```

#### POST /api/v1/auth/verify-otp

Verify email with OTP code.

**Request Body:**

```json
{
  "email": "john@example.com",
  "otp": "123456"
}
```

**Response (200 OK):**

```json
{
  "message": "Email verified successfully",
  "timestamp": "2024-01-15T10:30:00Z",
  "success": true
}
```

#### POST /api/v1/auth/logout

Logout and invalidate current session.

**Headers:** `Authorization: Bearer <token>`

**Response (200 OK):**

```json
{
  "message": "Logged out successfully",
  "timestamp": "2024-01-15T10:30:00Z",
  "success": true
}
```

#### GET /api/v1/auth/reset-password

Request password reset via email.

**Query Parameters:**

- `email` (required): User's email address

**Response (200 OK):**

```json
{
  "message": "Password reset email sent",
  "timestamp": "2024-01-15T10:30:00Z",
  "success": true
}
```

#### POST /api/v1/auth/reset-password

Complete password reset with token.

**Query Parameters:**

- `token` (required): Reset token from email
- `newPassword` (required): New password

**Response (200 OK):**

```json
{
  "message": "Password reset successfully",
  "timestamp": "2024-01-15T10:30:00Z",
  "success": true
}
```

### User Management Endpoints

#### GET /api/v1/users/me

Get current user's profile information.

**Headers:** `Authorization: Bearer <token>`

**Response (200 OK):**

```json
{
  "id": "user-id-123",
  "fullName": "John Doe",
  "username": "johndoe",
  "email": "john@example.com",
  "roles": [{ "id": "1", "roleName": "USER", "username": "johndoe" }],
  "provider": "local",
  "accountCreatedAt": "2024-01-15T10:30:00Z",
  "verified": true,
  "userAvatar": "https://res.cloudinary.com/avatar.jpg",
  "premium": false
}
```

#### PUT /api/v1/users/profile/update

Update user profile with optional file uploads.

**Headers:**

- `Authorization: Bearer <token>`
- `Content-Type: multipart/form-data`

**Form Data:**

```
user: {
  "fullName": "John Doe Updated",
  "bio": "Software Developer",
  "countryName": "USA",
  "city": "New York"
}
avatar: <image_file>
banner: <image_file>
```

**Response (200 OK):**

```json
{
  "message": "Profile updated successfully",
  "timestamp": "2024-01-15T10:30:00Z",
  "success": true
}
```

#### PUT /api/v1/users/profile/update/username

Update username.

**Headers:** `Authorization: Bearer <token>`

**Query Parameters:**

- `username` (required): New username

**Response (200 OK):**

```json
{
  "message": "Username updated successfully",
  "timestamp": "2024-01-15T10:30:00Z",
  "success": true
}
```

#### PUT /api/v1/users/profile/update/email

Update email address.

**Headers:** `Authorization: Bearer <token>`

**Query Parameters:**

- `email` (required): New email address

**Response (200 OK):**

```json
{
  "message": "Email updated successfully. Please verify your new email.",
  "timestamp": "2024-01-15T10:30:00Z",
  "success": true
}
```

### Premium Features

#### GET /api/v1/users/redeem

Redeem premium code.

**Headers:** `Authorization: Bearer <token>`

**Query Parameters:**

- `code` (required): Premium redeem code

**Response (200 OK):**

```json
{
  "message": "Premium activated successfully",
  "timestamp": "2024-01-15T10:30:00Z",
  "success": true
}
```

#### POST /api/v1/premium/generate

Generate new premium redeem code (Admin only).

**Headers:** `Authorization: Bearer <admin_token>`

**Response (200 OK):**

```json
{
  "id": "code-id-123",
  "redeemCode": "PREMIUM-ABC123",
  "redeemedBy": null,
  "redeemedAt": null,
  "createdAt": "2024-01-15T10:30:00Z",
  "used": false
}
```

### Admin Endpoints

#### GET /api/v1/admins/getAllUsers

Get all users (Admin only).

**Headers:** `Authorization: Bearer <admin_token>`

**Response (200 OK):**

```json
[
  {
    "id": "user-id-123",
    "fullName": "John Doe",
    "username": "johndoe",
    "email": "john@example.com",
    "verified": true,
    "premium": false,
    "accountCreatedAt": "2024-01-15T10:30:00Z"
  }
]
```

#### GET /api/v1/admins/getAllUsers/{id}

Get user by ID (Admin only).

**Headers:** `Authorization: Bearer <admin_token>`

**Path Parameters:**

- `id` (required): User ID

**Response (200 OK):**

```json
{
  "id": "user-id-123",
  "fullName": "John Doe",
  "username": "johndoe",
  "email": "john@example.com",
  "roles": [{ "id": "1", "roleName": "USER" }],
  "verified": true,
  "premium": false,
  "accountCreatedAt": "2024-01-15T10:30:00Z"
}
```

#### POST /api/v1/admins/generatejwtToken/{emailOrUsername}

Generate JWT token for user (Admin only).

**Headers:** `Authorization: Bearer <admin_token>`

**Path Parameters:**

- `emailOrUsername` (required): User's email or username

**Response (200 OK):**

```
"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

## Error Handling

### Error Response Format

```json
{
  "error": "ERROR_CODE",
  "message": "Human readable error message",
  "details": ["Specific validation errors"],
  "timestamp": "2024-01-15T10:30:00Z",
  "path": "/api/v1/endpoint"
}
```

### Common Error Codes

| Status Code | Error Code       | Description                     |
| ----------- | ---------------- | ------------------------------- |
| 400         | VALIDATION_ERROR | Invalid input data              |
| 401         | UNAUTHORIZED     | Authentication required         |
| 403         | FORBIDDEN        | Insufficient permissions        |
| 404         | NOT_FOUND        | Resource not found              |
| 409         | CONFLICT         | Duplicate data (username/email) |
| 429         | RATE_LIMITED     | Too many requests               |
| 500         | INTERNAL_ERROR   | Server error                    |

### Example Error Responses

#### Validation Error (400)

```json
{
  "error": "VALIDATION_ERROR",
  "message": "Invalid input data",
  "details": [
    "Password must be at least 8 characters",
    "Email format is invalid"
  ],
  "timestamp": "2024-01-15T10:30:00Z",
  "path": "/api/v1/auth/register"
}
```

#### Authentication Error (401)

```json
{
  "error": "UNAUTHORIZED",
  "message": "Invalid credentials",
  "timestamp": "2024-01-15T10:30:00Z",
  "path": "/api/v1/auth/login"
}
```

#### Permission Error (403)

```json
{
  "error": "FORBIDDEN",
  "message": "Admin access required",
  "timestamp": "2024-01-15T10:30:00Z",
  "path": "/api/v1/admins/getAllUsers"
}
```

## Rate Limiting

### Rate Limit Headers

```http
X-RateLimit-Remaining: 95
X-RateLimit-Reset: 1642252800
X-RateLimit-Limit: 100
```

### Default Limits by Endpoint Category

| Category           | Limit        | Window   |
| ------------------ | ------------ | -------- |
| Authentication     | 5 requests   | 1 minute |
| User Profile       | 10 requests  | 1 minute |
| Premium Operations | 3 requests   | 1 hour   |
| Admin Operations   | 50 requests  | 1 minute |
| General API        | 100 requests | 1 minute |

## Data Models

### User Object

```json
{
  "id": "string",
  "fullName": "string",
  "username": "string",
  "email": "string",
  "roles": [{ "id": "string", "roleName": "string" }],
  "provider": "string",
  "accountCreatedAt": "string (ISO 8601)",
  "verified": "boolean",
  "userAvatar": "string (URL)",
  "userBanner": "string (URL)",
  "premium": "boolean",
  "bio": "string",
  "countryName": "string",
  "city": "string"
}
```

### Premium Redeem Code Object

```json
{
  "id": "string",
  "redeemCode": "string",
  "redeemedBy": "string",
  "redeemedAt": "string (ISO 8601)",
  "createdAt": "string (ISO 8601)",
  "used": "boolean"
}
```

## Security Considerations

### Password Requirements

- Minimum 8 characters
- At least one uppercase letter
- At least one lowercase letter
- At least one number
- At least one special character

### JWT Security

- Tokens are signed with HS256
- Access tokens expire in 24 hours
- Refresh tokens expire in 7 days
- Tokens should be stored securely on client side

### File Upload Security

- Maximum file size: 5MB
- Allowed image types: JPEG, PNG, GIF
- Files are uploaded to Cloudinary with virus scanning
- File names are sanitized

## Testing

### Test Account

For development/testing purposes:

```json
{
  "username": "testuser",
  "email": "test@example.com",
  "password": "TestPass123!"
}
```

### Postman Collection

Download the Postman collection: [SecureTOTP API Collection](./postman-collection.json)

## Support

- **Documentation**: https://docs.securetotp.com
- **GitHub Issues**: https://github.com/securetotp/backend/issues
- **Email Support**: api-support@securetotp.com
- **Status Page**: https://status.securetotp.com

## Changelog

### Version 1.0 (Current)

- Initial API release
- JWT authentication
- User management
- Premium features
- Admin endpoints
- File upload support
  ```

  ```
- **Security**: No additional security specified

#### Validate Token

**POST** `/api/v1/auth/validateToken`

Validate a JWT token.

- **Parameters**:
  - `Authorization` (header, required): Bearer token
- **Responses**:
  - **200**: Token validated successfully
    ```json
    {
      "email": "string",
      "httpStatus": "200 OK"
    }
    ```
- **Security**: Bearer token required

#### Validate Token or Not

**POST** `/api/v1/auth/validateTokenOrNot`

Check if a JWT token is valid.

- **Parameters**:
  - `Authorization` (header, required): Bearer token
- **Responses**:
  - **200**: Token validation status
    ```json
    true
    ```
- **Security**: Bearer token required

#### Is Authenticated by Token

**GET** `/api/v1/auth/isLogin`

Check if the user is authenticated by token.

- **Parameters**:
  - `Authorization` (header, required): Bearer token
- **Responses**:
  - **200**: Authentication status
    ```json
    true
    ```
- **Security**: Bearer token required

#### Get User by Email or Username

**GET** `/api/v1/auth/byEmailUsername/{emailOrUsername}`

Retrieve a user by email or username.

- **Parameters**:
  - `emailOrUsername` (path, required): Email or username
- **Responses**:
  - **200**: User retrieved successfully
    ```json
    {
      "id": "string",
      "fullName": "string",
      "username": "string",
      "email": "string",
      "roles": [
        {
          "id": "string",
          "roleName": "string",
          "username": "string"
        }
      ],
      "provider": "string",
      "redeemCode": {
        "id": "string",
        "redeemCode": "string",
        "redeemedBy": "string",
        "redeemedAt": "string (date-time)",
        "createdAt": "string (date-time)",
        "used": "boolean"
      },
      "accountCreatedAt": "string (date-time)",
      "verified": "boolean",
      "otp": "string",
      "otpGeneratedTime": "string (date-time)",
      "userAvatar": "string",
      "userAvatarpublicId": "string",
      "userBanner": "string",
      "userBannerpublicId": "string",
      "premium": "boolean"
    }
    ```
- **Security**: Bearer token required

#### Public Access

**GET** `/api/v1/auth/public`

Publicly accessible endpoint.

- **Responses**:
  - **200**: Public access successful
    ```json
    {
      "message": "string",
      "timestamp": "string (date-time)",
      "success": "boolean"
    }
    ```
- **Security**: No additional security specified

### Admin Rest Controller

#### Say Hello

**GET** `/api/v1/admins`

Administrative endpoint to return a greeting.

- **Responses**:
  - **200**: Greeting returned
    ```json
    "string"
    ```
- **Security**: Bearer token required

#### Get All Users

**GET** `/api/v1/admins/getAllUsers`

Retrieve a list of all users (admin only).

- **Responses**:
  - **200**: List of users retrieved successfully
    ```json
    [
      {
        "id": "string",
        "fullName": "string",
        "username": "string",
        "email": "string",
        "roles": [
          {
            "id": "string",
            "roleName": "string",
            "username": "string"
          }
        ],
        "provider": "string",
        "redeemCode": {
          "id": "string",
          "redeemCode": "string",
          "redeemedBy": "string",
          "redeemedAt": "string (date-time)",
          "createdAt": "string (date-time)",
          "used": "boolean"
        },
        "accountCreatedAt": "string (date-time)",
        "verified": "boolean",
        "otp": "string",
        "otpGeneratedTime": "string (date-time)",
        "userAvatar": "string",
        "userAvatarpublicId": "string",
        "userBanner": "string",
        "userBannerpublicId": "string",
        "premium": "boolean"
      }
    ]
    ```
- **Security**: Bearer token required

#### Get User by ID

**GET** `/api/v1/admins/getAllUsers/{id}`

Retrieve a user by ID (admin only).

- **Parameters**:
  - `id` (path, required): User ID
- **Responses**:
  - **200**: User retrieved successfully
    ```json
    {
      "$ref": "#/components/schemas/UserDTO"
    }
    ```
- **Security**: Bearer token required

#### Get User by Username

**GET** `/api/v1/admins/getAllUsers/username/{username}`

Retrieve a user by username (admin only).

- **Parameters**:
  - `username` (path, required): Username
- **Responses**:
  - **200**: User retrieved successfully
    ```json
    {
      "$ref": "#/components/schemas/UserDTO"
    }
    ```
- **Security**: Bearer token required

#### Get User by Email

**GET** `/api/v1/admins/getAllUsers/email/{email}`

Retrieve a user by email (admin only).

- **Parameters**:
  - `email` (path, required): Email
- **Responses**:
  - **200**: User retrieved successfully
    ```json
    {
      "$ref": "#/components/schemas/UserDTO"
    }
    ```
- **Security**: Bearer token required

#### Get Users by Role

**GET** `/api/v1/admins/getAllUsers/role/{role}`

Retrieve users by role (admin only).

- **Parameters**:
  - `role` (path, required): Role name
- **Responses**:
  - **200**: List of users retrieved successfully
    ```json
    [
      {
        "$ref": "#/components/schemas/Role"
      }
    ]
    ```
- **Security**: Bearer token required

#### Get User by Email or Username

**GET** `/api/v1/admins/byEmailUsername/{emailOrUsername}`

Retrieve a user by email or username (admin only).

- **Parameters**:
  - `emailOrUsername` (path, required): Email or username
- **Responses**:
  - **200**: User retrieved successfully
    ```json
    {
      "$ref": "#/components/schemas/UserDTO"
    }
    ```
- **Security**: Bearer token required

#### Generate JWT Token for User

**POST** `/api/v1/admins/generatejwtToken/{emailOrUsername}`

Generate a JWT token for a specific user (admin only).

- **Parameters**:
  - `emailOrUsername` (path, required): Email or username
- **Responses**:
  - **200**: JWT token generated successfully
    ```json
    "string"
    ```
- **Security**: Bearer token required

### Super Admin Rest Controller

#### Say Hi

**GET** `/api/v1/superadmins`

Super admin endpoint to return a greeting.

- **Responses**:
  - **200**: Greeting returned
    ```json
    "string"
    ```
- **Security**: Bearer token required

## Schemas

### UserProfileUpdateDTO

```json
{
  "type": "object",
  "properties": {
    "fullName": "string",
    "username": "string",
    "email": "string",
    "userAvatar": "string",
    "userAvatarpublicId": "string",
    "userBanner": "string",
    "userBannerpublicId": "string",
    "bio": "string",
    "countryName": "string",
    "city": "string",
    "recoveryPhone": "string",
    "recoveryEmail": "string",
    "socialLinks": {
      "type": "object",
      "additionalProperties": {
        "type": "string"
      }
    },
    "jobTitle": "string",
    "company": "string",
    "website": "string",
    "birthDate": "string (date-time)",
    "gender": "string"
  }
}
```

### MessageResponseDTO

```json
{
  "type": "object",
  "properties": {
    "message": "string",
    "timestamp": "string (date-time)",
    "success": "boolean"
  }
}
```

### PremiumRedeemCodeResponseDTO

```json
{
  "type": "object",
  "properties": {
    "id": "string",
    "redeemCode": "string",
    "redeemedBy": "string",
    "redeemedAt": "string (date-time)",
    "createdAt": "string (date-time)",
    "used": "boolean"
  }
}
```

### VerifyOtpRequest

```json
{
  "type": "object",
  "properties": {
    "email": "string",
    "otp": "string"
  }
}
```

### TokenResponse

```json
{
  "type": "object",
  "properties": {
    "email": "string",
    "httpStatus": "string",
    "enum": [
      "100 CONTINUE",
      "101 SWITCHING_PROTOCOLS",
      "102 PROCESSING",
      "103 EARLY_HINTS",
      "103 CHECKPOINT",
      "200 OK",
      "201 CREATED",
      "202 ACCEPTED",
      "203 NON_AUTHORITATIVE_INFORMATION",
      "204 NO_CONTENT",
      "205 RESET_CONTENT",
      "206 PARTIAL_CONTENT",
      "207 MULTI_STATUS",
      "208 ALREADY_REPORTED",
      "226 IM_USED",
      "300 MULTIPLE_CHOICES",
      "301 MOVED_PERMANENTLY",
      "302 FOUND",
      "302 MOVED_TEMPORARILY",
      "303 SEE_OTHER",
      "304 NOT_MODIFIED",
      "305 USE_PROXY",
      "307 TEMPORARY_REDIRECT",
      "308 PERMANENT_REDIRECT",
      "400 BAD_REQUEST",
      "401 UNAUTHORIZED",
      "402 PAYMENT_REQUIRED",
      "403 FORBIDDEN",
      "404 NOT_FOUND",
      "405 METHOD_NOT_ALLOWED",
      "406 NOT_ACCEPTABLE",
      "407 PROXY_AUTHENTICATION_REQUIRED",
      "408 REQUEST_TIMEOUT",
      "409 CONFLICT",
      "410 GONE",
      "411 LENGTH_REQUIRED",
      "412 PRECONDITION_FAILED",
      "413 PAYLOAD_TOO_LARGE",
      "413 REQUEST_ENTITY_TOO_LARGE",
      "414 URI_TOO_LONG",
      "414 REQUEST_URI_TOO_LONG",
      "415 UNSUPPORTED_MEDIA_TYPE",
      "416 REQUESTED_RANGE_NOT_SATISFIABLE",
      "417 EXPECTATION_FAILED",
      "418 I_AM_A_TEAPOT",
      "419 INSUFFICIENT_SPACE_ON_RESOURCE",
      "420 METHOD_FAILURE",
      "421 DESTINATION_LOCKED",
      "422 UNPROCESSABLE_ENTITY",
      "423 LOCKED",
      "424 FAILED_DEPENDENCY",
      "425 TOO_EARLY",
      "426 UPGRADE_REQUIRED",
      "428 PRECONDITION_REQUIRED",
      "429 TOO_MANY_REQUESTS",
      "431 REQUEST_HEADER_FIELDS_TOO_LARGE",
      "451 UNAVAILABLE_FOR_LEGAL_REASONS",
      "500 INTERNAL_SERVER_ERROR",
      "501 NOT_IMPLEMENTED",
      "502 BAD_GATEWAY",
      "503 SERVICE_UNAVAILABLE",
      "504 GATEWAY_TIMEOUT",
      "505 HTTP_VERSION_NOT_SUPPORTED",
      "506 VARIANT_ALSO_NEGOTIATES",
      "507 INSUFFICIENT_STORAGE",
      "508 LOOP_DETECTED",
      "509 BANDWIDTH_LIMIT_EXCEEDED",
      "510 NOT_EXTENDED",
      "511 NETWORK_AUTHENTICATION_REQUIRED"
    ]
  }
}
```

### ResendOtpRequest

```json
{
  "type": "object",
  "properties": {
    "email": "string"
  }
}
```

### RegisterDto

```json
{
  "type": "object",
  "properties": {
    "fullName": "string",
    "username": "string",
    "email": "string",
    "password": "string",
    "provider": "string"
  }
}
```

### LoginDto

```json
{
  "type": "object",
  "properties": {
    "emailOrUsername": "string",
    "password": "string"
  }
}
```

### RedeemCode

```json
{
  "type": "object",
  "properties": {
    "id": "string",
    "redeemCode": "string",
    "redeemedBy": "string",
    "redeemedAt": "string (date-time)",
    "createdAt": "string (date-time)",
    "used": "boolean"
  }
}
```

### Role

```json
{
  "type": "object",
  "properties": {
    "id": "string",
    "roleName": "string",
    "username": "string"
  }
}
```

### UserDTO

```json
{
  "type": "object",
  "properties": {
    "id": "string",
    "fullName": "string",
    "username": "string",
    "email": "string",
    "roles": [
      {
        "$ref": "#/components/schemas/Role"
      }
    ],
    "provider": "string",
    "redeemCode": {
      "$ref": "#/components/schemas/RedeemCode"
    },
    "accountCreatedAt": "string (date-time)",
    "verified": "boolean",
    "otp": "string",
    "otpGeneratedTime": "string (date-time)",
    "userAvatar": "string",
    "userAvatarpublicId": "string",
    "userBanner": "string",
    "userBannerpublicId": "string",
    "premium": "boolean"
  }
}
```
