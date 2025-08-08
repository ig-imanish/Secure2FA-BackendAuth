# Individual Profile Update Endpoints

This document describes the new granular profile update endpoints that allow updating one field at a time.

## Base URLs

- **Query Parameter Style**: `/api/v1/users/profile/`
- **JSON Body Style**: `/api/v1/users/profile/individual/`

## Authentication

All endpoints require valid JWT token in the `Authorization` header:

```
Authorization: Bearer <your-jwt-token>
```

## Endpoints

### 1. Get Current User Profile

**GET** `/api/v1/users/profile`
**GET** `/api/v1/users/profile/individual`

**Response:**

```json
{
  "id": "user_id",
  "fullName": "John Doe",
  "username": "@johndoe",
  "email": "john@example.com",
  "userAvatar": "https://cloudinary.com/avatar.jpg",
  "userBanner": "https://cloudinary.com/banner.jpg",
  "recoveryPhone": "+1234567890",
  "verified": true,
  "premium": false,
  "accountCreatedAt": "2025-01-01T00:00:00Z"
}
```

### 2. Update Full Name

**PUT** `/api/v1/users/profile/fullname?fullName=New Name`

**PUT** `/api/v1/users/profile/individual/fullname`

```json
{
  "fullName": "New Full Name"
}
```

**Response:**

```json
{
  "success": true,
  "message": "Full name updated successfully",
  "timestamp": "2025-07-26T00:00:00Z"
}
```

### 3. Update Email

**PUT** `/api/v1/users/profile/email?email=newemail@example.com`

**PUT** `/api/v1/users/profile/individual/email`

```json
{
  "email": "newemail@example.com"
}
```

**Response:** Returns new JWT token

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer "
}
```

### 4. Update Username

**PUT** `/api/v1/users/profile/username?username=@newusername`

**PUT** `/api/v1/users/profile/individual/username`

```json
{
  "username": "@newusername"
}
```

**Response:** Returns new JWT token

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer "
}
```

### 5. Update Avatar

**PUT** `/api/v1/users/profile/avatar`
**PUT** `/api/v1/users/profile/individual/avatar`

**Content-Type:** `multipart/form-data`
**Form Data:**

- `avatar`: Image file (jpg, png, gif, etc.)

**Response:**

```json
{
  "success": true,
  "message": "Avatar updated successfully",
  "timestamp": "2025-07-26T00:00:00Z"
}
```

### 6. Update Banner

**PUT** `/api/v1/users/profile/banner`
**PUT** `/api/v1/users/profile/individual/banner`

**Content-Type:** `multipart/form-data`
**Form Data:**

- `banner`: Image file (jpg, png, gif, etc.)

**Response:**

```json
{
  "success": true,
  "message": "Banner updated successfully",
  "timestamp": "2025-07-26T00:00:00Z"
}
```

### 7. Update Recovery Phone

**PUT** `/api/v1/users/profile/recovery-phone?recoveryPhone=+1234567890`

**PUT** `/api/v1/users/profile/individual/recovery-phone`

```json
{
  "recoveryPhone": "+1234567890"
}
```

**Response:**

```json
{
  "success": true,
  "message": "Recovery phone updated successfully",
  "timestamp": "2025-07-26T00:00:00Z"
}
```

### 8. Remove Avatar

**DELETE** `/api/v1/users/profile/avatar`
**DELETE** `/api/v1/users/profile/individual/avatar`

**Response:**

```json
{
  "success": true,
  "message": "Avatar removed successfully",
  "timestamp": "2025-07-26T00:00:00Z"
}
```

### 9. Remove Banner

**DELETE** `/api/v1/users/profile/banner`
**DELETE** `/api/v1/users/profile/individual/banner`

**Response:**

```json
{
  "success": true,
  "message": "Banner removed successfully",
  "timestamp": "2025-07-26T00:00:00Z"
}
```

## Error Responses

**400 Bad Request:**

```json
{
  "success": false,
  "message": "Full name cannot be empty",
  "timestamp": "2025-07-26T00:00:00Z"
}
```

**401 Unauthorized:**

```json
{
  "success": false,
  "message": "Unauthorized: No user logged in",
  "timestamp": "2025-07-26T00:00:00Z"
}
```

**500 Internal Server Error:**

```json
{
  "success": false,
  "message": "Failed to update profile",
  "timestamp": "2025-07-26T00:00:00Z"
}
```

## Usage Examples

### Frontend (React/JavaScript)

```javascript
// Update full name
const updateFullName = async (fullName) => {
  const response = await fetch("/api/v1/users/profile/individual/fullname", {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify({ fullName }),
  });
  return response.json();
};

// Update avatar
const updateAvatar = async (avatarFile) => {
  const formData = new FormData();
  formData.append("avatar", avatarFile);

  const response = await fetch("/api/v1/users/profile/individual/avatar", {
    method: "PUT",
    headers: {
      Authorization: `Bearer ${token}`,
    },
    body: formData,
  });
  return response.json();
};
```

### cURL Examples

```bash
# Update full name
curl -X PUT "http://localhost:9000/api/v1/users/profile/fullname?fullName=John Doe" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Update email (JSON style)
curl -X PUT "http://localhost:9000/api/v1/users/profile/individual/email" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"email": "newemail@example.com"}'

# Upload avatar
curl -X PUT "http://localhost:9000/api/v1/users/profile/individual/avatar" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "avatar=@/path/to/avatar.jpg"
```

## Benefits

1. **Granular Control**: Update only the field you need to change
2. **Better Performance**: No need to send entire user object
3. **Cleaner API**: Each endpoint has a single responsibility
4. **Better Error Handling**: Specific validation for each field
5. **Flexibility**: Two styles available (query params vs JSON body)
6. **Automatic Cleanup**: Old images are deleted when updating avatar/banner
