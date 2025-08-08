# SecureTOTP Backend Authentication - User Guide

## Overview

SecureTOTP Backend Authentication is a Spring Boot application that provides secure two-factor authentication (2FA) services with TOTP (Time-based One-Time Password) support.

## Features

### Authentication & Security

- **User Registration**: Secure account creation with email verification
- **User Login**: JWT-based authentication with 2FA support
- **Password Management**: Secure password reset and change functionality
- **Two-Factor Authentication**: TOTP-based 2FA using authenticator apps
- **Session Management**: Secure session handling with token refresh

### TOTP Management

- **Secret Generation**: Generate secure TOTP secrets
- **QR Code Support**: Generate QR codes for authenticator apps
- **Backup Codes**: Generate and manage backup codes for account recovery
- **Multiple Secrets**: Support for multiple TOTP secrets per user

### User Profile

- **Profile Management**: Update personal information and settings
- **Avatar Support**: Upload and manage profile pictures via Cloudinary
- **PIN Management**: Set and update security PINs
- **Account Verification**: Email verification system

### Premium Features

- **Premium Accounts**: Enhanced features for premium users
- **Advanced Security**: Additional security options for premium users

## API Endpoints

### Authentication Endpoints

#### Register New User

```
POST /api/v1/auth/register
Content-Type: application/json

{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "SecurePassword123!",
  "firstName": "John",
  "lastName": "Doe"
}
```

#### User Login

```
POST /api/v1/auth/login
Content-Type: application/json

{
  "username": "john_doe",
  "password": "SecurePassword123!",
  "totpCode": "123456"
}
```

#### Refresh Token

```
POST /api/v1/auth/refresh
Authorization: Bearer <refresh_token>
```

#### Logout

```
POST /api/v1/auth/logout
Authorization: Bearer <access_token>
```

### TOTP Endpoints

#### Generate TOTP Secret

```
POST /api/v1/totp/generate
Authorization: Bearer <access_token>

{
  "label": "My Work Account"
}
```

#### Verify TOTP Code

```
POST /api/v1/totp/verify
Authorization: Bearer <access_token>

{
  "secretId": "secret-id",
  "totpCode": "123456"
}
```

#### Get QR Code

```
GET /api/v1/totp/{secretId}/qr
Authorization: Bearer <access_token>
```

#### Generate Backup Codes

```
POST /api/v1/totp/{secretId}/backup-codes
Authorization: Bearer <access_token>
```

### User Profile Endpoints

#### Get User Profile

```
GET /api/v1/users/profile
Authorization: Bearer <access_token>
```

#### Update Profile

```
PUT /api/v1/users/profile
Authorization: Bearer <access_token>

{
  "firstName": "John",
  "lastName": "Doe Updated",
  "email": "john.updated@example.com"
}
```

#### Upload Avatar

```
POST /api/v1/users/avatar
Authorization: Bearer <access_token>
Content-Type: multipart/form-data

file: <image_file>
```

#### Change Password

```
PUT /api/v1/users/password
Authorization: Bearer <access_token>

{
  "currentPassword": "OldPassword123!",
  "newPassword": "NewPassword123!"
}
```

#### Update PIN

```
PUT /api/v1/users/pin
Authorization: Bearer <access_token>

{
  "pin": "1234"
}
```

## Getting Started

### Prerequisites

- Java 17 or higher
- MongoDB 4.4 or higher
- Maven 3.6 or higher
- Cloudinary account (for avatar uploads)

### Installation

1. Clone the repository
2. Configure application properties
3. Install dependencies: `mvn clean install`
4. Run the application: `mvn spring-boot:run`

### Configuration

Create `application.yml` with:

```yaml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/securetotp

cloudinary:
  cloud-name: your-cloud-name
  api-key: your-api-key
  api-secret: your-api-secret

jwt:
  secret: your-jwt-secret
  expiration: 86400000
  refresh-expiration: 604800000
```

## Security Features

### Password Requirements

- Minimum 8 characters
- At least one uppercase letter
- At least one lowercase letter
- At least one number
- At least one special character

### TOTP Security

- SHA-1 algorithm (configurable)
- 30-second time step
- 6-digit codes
- Secret rotation support

### JWT Security

- Access tokens: 24 hours
- Refresh tokens: 7 days
- Secure token storage
- Automatic token rotation

## Error Handling

### Common Error Codes

- `400` - Bad Request (validation errors)
- `401` - Unauthorized (authentication required)
- `403` - Forbidden (insufficient permissions)
- `404` - Not Found (resource not found)
- `409` - Conflict (duplicate data)
- `500` - Internal Server Error

### Error Response Format

```json
{
  "error": "VALIDATION_ERROR",
  "message": "Invalid input data",
  "details": ["Password must be at least 8 characters"],
  "timestamp": "2024-01-15T10:30:00Z"
}
```

## Rate Limiting

### Default Limits

- Authentication: 5 requests per minute
- TOTP generation: 3 requests per hour
- Password reset: 3 requests per hour
- General API: 100 requests per minute

## Support

For technical support or questions:

- Create an issue in the GitHub repository
- Contact: support@securetotp.com
- Documentation: https://docs.securetotp.com
