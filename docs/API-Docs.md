# My API Documentation

## Overview

This API provides endpoints for user management, authentication, and premium features with JWT-based authentication.

- **Version**: 1.0
- **Base URL**: `http://localhost:9000`

## Authentication

All endpoints, unless specified, require a Bearer token in the `Authorization` header. The token must be a valid JWT.

```http
Authorization: Bearer <your-jwt-token>
```

## Tags

- **private-user-controller**: Endpoints for user management and profile updates.
- **user-profile-update-controller**: Endpoints for updating user profile details.
- **premium-rest-controller**: Endpoints for managing premium redeem codes.
- **auth-rest-controller**: Endpoints for authentication, including login, logout, and OTP verification.
- **admin-rest-controller**: Administrative endpoints for user management.
- **super-admin-rest-controller**: Super admin endpoints.
- **global-access-controller**: Publicly accessible endpoints.

## Endpoints

### Private User Controller

#### Get Current User

**GET** `/api/v1/users/me`

Retrieve the authenticated user's details.

- **Responses**:
  - **200**: User details retrieved successfully
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

#### Get Users

**GET** `/api/v1/users`

Retrieve a list of all users.

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

#### Redeem Premium

**GET** `/api/v1/users/redeem`

Redeem a premium code for the authenticated user.

- **Parameters**:
  - `code` (query, required): Premium redeem code
- **Responses**:
  - **200**: Premium code redeemed successfully
    ```json
    {
      "message": "string",
      "timestamp": "string (date-time)",
      "success": "boolean"
    }
    ```
- **Security**: Bearer token required

#### Display User Info

**GET** `/api/v1/users/info`

Display information about the authenticated user.

- **Responses**:
  - **200**: User information retrieved successfully
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

#### Update Username

**PUT** `/api/v1/users/username`

Update the authenticated user's username.

- **Parameters**:
  - `username` (query, required): New username
- **Responses**:
  - **200**: Username updated successfully
    ```json
    {
      "message": "string",
      "timestamp": "string (date-time)",
      "success": "boolean"
    }
    ```
- **Security**: Bearer token required

### User Profile Update Controller

#### Update Profile

**PUT** `/api/v1/users/profile/update`

Update the authenticated user's profile, including avatar and banner.

- **Parameters**:
  - `user` (query, required): User profile details
    ```json
    {
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
        "additionalProperties": "string"
      },
      "jobTitle": "string",
      "company": "string",
      "website": "string",
      "birthDate": "string (date-time)",
      "gender": "string"
    }
    ```
- **Request Body**:
  ```json
  {
    "banner": "string (binary)",
    "avatar": "string (binary)"
  }
  ```
- **Responses**:
  - **200**: Profile updated successfully
    ```json
    {
      "message": "string",
      "timestamp": "string (date-time)",
      "success": "boolean"
    }
    ```
- **Security**: Bearer token required

#### Update Username

**PUT** `/api/v1/users/profile/update/username`

Update the authenticated user's username.

- **Parameters**:
  - `username` (query, required): New username
- **Responses**:
  - **200**: Username updated successfully
    ```json
    {
      "message": "string",
      "timestamp": "string (date-time)",
      "success": "boolean"
    }
    ```
- **Security**: Bearer token required

#### Update Email

**PUT** `/api/v1/users/profile/update/email`

Update the authenticated user's email.

- **Parameters**:
  - `email` (query, required): New email
- **Responses**:
  - **200**: Email updated successfully
    ```json
    {
      "message": "string",
      "timestamp": "string (date-time)",
      "success": "boolean"
    }
    ```
- **Security**: Bearer token required

### Premium Rest Controller

#### Generate Redeem Code

**POST** `/api/v1/premium/generate`

Generate a new premium redeem code.

- **Responses**:
  - **200**: Redeem code generated successfully
    ```json
    {
      "id": "string",
      "redeemCode": "string",
      "redeemedBy": "string",
      "redeemedAt": "string (date-time)",
      "createdAt": "string (date-time)",
      "used": "boolean"
    }
    ```
- **Security**: Bearer token required

#### Get All Redeem Codes

**GET** `/api/v1/premium/code`

Retrieve all premium redeem codes.

- **Responses**:
  - **200**: List of redeem codes retrieved successfully
    ```json
    [
      {
        "id": "string",
        "redeemCode": "string",
        "redeemedBy": "string",
        "redeemedAt": "string (date-time)",
        "createdAt": "string (date-time)",
        "used": "boolean"
      }
    ]
    ```
- **Security**: Bearer token required

#### Get Redeem Code

**GET** `/api/v1/premium/code/{code}`

Retrieve a specific premium redeem code.

- **Parameters**:
  - `code` (path, required): Redeem code
- **Responses**:
  - **200**: Redeem code retrieved successfully
    ```json
    {
      "id": "string",
      "redeemCode": "string",
      "redeemedBy": "string",
      "redeemedAt": "string (date-time)",
      "createdAt": "string (date-time)",
      "used": "boolean"
    }
    ```
- **Security**: Bearer token required

### Auth Rest Controller

#### Login

**POST** `/api/v1/auth/login`

Authenticate a user and return a JWT token.

- **Parameters**:
  - `error` (query, optional): Error message
- **Request Body**:
  ```json
  {
    "emailOrUsername": "string",
    "password": "string"
  }
  ```
- **Responses**:
  - **200**: Login successful
    ```json
    {
      "token": "string",
      "type": "Bearer",
      "id": "string",
      "username": "string",
      "email": "string",
      "roles": ["string"]
    }
    ```
- **Security**: No additional security specified

#### Logout

**POST** `/api/v1/auth/logout`

Log out the authenticated user.

- **Responses**:
  - **200**: Logout successful
    ```json
    {
      "message": "string",
      "timestamp": "string (date-time)",
      "success": "boolean"
    }
    ```
- **Security**: Bearer token required

#### Register

**POST** `/api/v1/auth/register`

Register a new user.

- **Request Body**:
  ```json
  {
    "fullName": "string",
    "username": "string",
    "email": "string",
    "password": "string",
    "provider": "string"
  }
  ```
- **Responses**:
  - **200**: User registered successfully
    ```json
    {
      "message": "string",
      "timestamp": "string (date-time)",
      "success": "boolean"
    }
    ```
- **Security**: No additional security specified

#### Resend OTP

**POST** `/api/v1/auth/resend-otp`

Resend an OTP for user verification.

- **Request Body**:
  ```json
  {
    "email": "string"
  }
  ```
- **Responses**:
  - **200**: OTP resent successfully
    ```json
    {
      "message": "string",
      "timestamp": "string (date-time)",
      "success": "boolean"
    }
    ```
- **Security**: No additional security specified

#### Verify OTP

**POST** `/api/v1/auth/verify-otp`

Verify a user's OTP.

- **Request Body**:
  ```json
  {
    "email": "string",
    "otp": "string"
  }
  ```
- **Responses**:
  - **200**: OTP verified successfully
    ```json
    {
      "message": "string",
      "timestamp": "string (date-time)",
      "success": "boolean"
    }
    ```
- **Security**: No additional security specified

#### Reset Password (Request)

**GET** `/api/v1/auth/reset-password`

Request a password reset.

- **Parameters**:
  - `email` (query, required): User email
- **Responses**:
  - **200**: Password reset request successful
    ```json
    {
      "message": "string",
      "timestamp": "string (date-time)",
      "success": "boolean"
    }
    ```
- **Security**: No additional security specified

#### Reset Password (Complete)

**POST** `/api/v1/auth/reset-password`

Complete the password reset process.

- **Parameters**:
  - `token` (query, required): Reset token
  - `newPassword` (query, required): New password
- **Responses**:
  - **200**: Password reset successful
    ```json
    {
      "message": "string",
      "timestamp": "string (date-time)",
      "success": "boolean"
    }
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
