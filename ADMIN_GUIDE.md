# SecureTOTP Backend Authentication - Admin Guide

## Overview

Administrative interface for managing users, analytics, and system operations in the SecureTOTP Backend Authentication service.

## Admin Features

### 1. Analytics System

#### Real-time Analytics Dashboard

- **User Metrics**: Total, active, new users tracking
- **Registration Trends**: Daily/weekly registration patterns
- **Activity Tracking**: Daily active users and engagement
- **Premium Analytics**: Premium user statistics
- **Page Views**: Traffic and usage analytics

#### Analytics API Endpoints

##### Get Complete Analytics

```
GET /api/v1/admins/analytics
Authorization: Bearer <admin_token>

Response: {
  "totalUsers": 1250,
  "activeUsers": 892,
  "newUsersToday": 15,
  "newUsersThisWeek": 89,
  "premiumUsers": 156,
  "verifiedUsers": 1100,
  "totalViews": 45678,
  "todayViews": 234,
  "thisWeekViews": 1456,
  "usersByRole": {
    "USER": 1200,
    "ADMIN": 48,
    "SUPER_ADMIN": 2
  },
  "registrationTrend": [...],
  "dailyActiveUsers": [...]
}
```

##### Get Analytics Summary

```
GET /api/v1/admins/analytics/summary
Authorization: Bearer <admin_token>
```

### 2. User Management System

#### User Management Features

- **Comprehensive User Search**: Search by name, email, username
- **Advanced Filtering**: Filter by role, status, verification, premium
- **Pagination Support**: Configurable page size and sorting
- **Bulk Operations**: Perform actions on multiple users
- **Real-time Updates**: Live user data synchronization

#### User Management API Endpoints

##### Get Paginated User List

```
GET /api/v1/admins/users?page=0&size=10&sortBy=accountCreatedAt&sortDir=desc
Authorization: Bearer <admin_token>

Query Parameters:
- page: Page number (default: 0)
- size: Page size (default: 10)
- sortBy: Sort field (default: accountCreatedAt)
- sortDir: Sort direction (asc/desc, default: desc)
```

##### Search Users

```
GET /api/v1/admins/users/search?query=john
Authorization: Bearer <admin_token>

Query Parameters:
- query: Search term (searches name, email, username)
```

##### Get User Details

```
GET /api/v1/admins/users/{userId}
Authorization: Bearer <admin_token>
```

### 3. User Account Management

#### Account Status Operations

##### Ban User

```
PUT /api/v1/admins/users/{userId}/ban?reason=policy_violation
Authorization: Bearer <admin_token>

Query Parameters:
- reason: Ban reason (optional)
```

##### Unban User

```
PUT /api/v1/admins/users/{userId}/unban
Authorization: Bearer <admin_token>
```

##### Verify User Account

```
PUT /api/v1/admins/users/{userId}/verify
Authorization: Bearer <admin_token>
```

#### Session Management

##### Force User Logout

```
POST /api/v1/admins/users/{userId}/force-logout
Authorization: Bearer <admin_token>
```

#### Premium Management

##### Upgrade to Premium

```
PUT /api/v1/admins/users/{userId}/premium
Authorization: Bearer <admin_token>
```

##### Remove Premium Status

```
DELETE /api/v1/admins/users/{userId}/premium
Authorization: Bearer <admin_token>
```

### 4. Security Management

#### TOTP Secret Management

##### Delete User TOTP Secrets

```
DELETE /api/v1/admins/users/{userId}/secrets
Authorization: Bearer <admin_token>
```

##### Delete All User Secrets

```
DELETE /api/v1/admins/users/{userId}/secrets/all
Authorization: Bearer <admin_token>
```

#### Account Deletion

##### Delete User Account

```
DELETE /api/v1/admins/users/{userId}
Authorization: Bearer <admin_token>
```

### 5. Generic Action System

#### Perform User Actions

```
POST /api/v1/admins/users/{userId}/actions
Authorization: Bearer <admin_token>
Content-Type: application/json

{
  "action": "BAN",
  "reason": "Terms violation",
  "data": {
    "duration": "7d",
    "notifyUser": true
  }
}

Available Actions:
- BAN: Ban user account
- UNBAN: Unban user account
- VERIFY: Verify user email
- FORCE_LOGOUT: Force logout from all devices
- DELETE_SECRETS: Delete TOTP secrets
- UPGRADE_PREMIUM: Upgrade to premium
- DOWNGRADE_PREMIUM: Remove premium status
```

## Admin Dashboard UI

### Access

- **URL**: `/admin/dashboard`
- **Authentication**: Admin role required
- **Technology**: Bootstrap 5, Chart.js, jQuery

### Dashboard Sections

#### 1. Analytics Overview

- **Key Metrics Cards**: Real-time user statistics
- **Registration Trend Chart**: Visual registration patterns
- **Daily Active Users Chart**: Activity tracking
- **User Role Distribution**: Role-based analytics

#### 2. User Management Interface

- **Paginated User Table**: Sortable and filterable
- **Search Functionality**: Real-time user search
- **Action Buttons**: Quick access to user operations
- **User Detail Modal**: Comprehensive user information
- **Bulk Action Support**: Multiple user operations

#### 3. Quick Actions Panel

- **User Status Management**: Ban/unban operations
- **Account Verification**: Email verification controls
- **Premium Management**: Premium status controls
- **Session Management**: Force logout capabilities
- **Account Deletion**: Secure account removal

### Dashboard Features

- **Responsive Design**: Mobile-friendly interface
- **Real-time Updates**: Auto-refresh capabilities
- **Action Confirmation**: Safety dialogs for destructive actions
- **Error Handling**: User-friendly error messages
- **Loading States**: Progressive loading indicators

## Security & Permissions

### Admin Access Control

- **Authentication Required**: All endpoints require valid JWT
- **Role-based Access**: ADMIN or SUPER_ADMIN roles only
- **Action Logging**: All admin actions are logged
- **Rate Limiting**: Protected against abuse

### Security Best Practices

- **Two-Factor Authentication**: Required for admin accounts
- **Session Timeout**: Automatic session expiration
- **IP Restrictions**: Optional IP whitelist support
- **Audit Trail**: Comprehensive action logging

## Data Analytics

### Tracked Metrics

- **User Registration**: Daily, weekly, monthly trends
- **User Activity**: Login frequency, session duration
- **Feature Usage**: TOTP generation, premium features
- **Geographic Data**: User location analytics
- **Device Analytics**: Browser, OS, device statistics

### Analytics Storage

- **Page Views**: MongoDB collection for view tracking
- **User Events**: Comprehensive event logging
- **Performance Metrics**: Response time and error tracking

## Monitoring & Alerts

### System Health Monitoring

- **Database Connectivity**: MongoDB connection status
- **External Services**: Cloudinary, email service status
- **Memory Usage**: JVM memory monitoring
- **Response Times**: API performance tracking

### Alert Configuration

- **High Error Rate**: Automatic alerts for error spikes
- **Database Issues**: Connection and performance alerts
- **Security Events**: Suspicious activity notifications
- **System Resources**: Memory and CPU alerts

## Backup & Recovery

### Data Backup

- **User Data**: Automated MongoDB backups
- **Configuration**: Environment-specific backups
- **Analytics Data**: Historical data preservation

### Recovery Procedures

- **Database Restore**: Step-by-step recovery process
- **User Account Recovery**: Manual account restoration
- **System Rollback**: Version rollback procedures

## Troubleshooting

### Common Issues

#### Authentication Problems

- **Invalid Tokens**: Check JWT configuration
- **Role Issues**: Verify user role assignments
- **Session Expiry**: Review session timeout settings

#### Analytics Issues

- **Missing Data**: Check analytics interceptor
- **Performance**: Optimize database queries
- **Chart Problems**: Verify Chart.js integration

#### User Management Issues

- **Ban/Unban Failures**: Check user status validation
- **Premium Issues**: Verify premium logic
- **Deletion Problems**: Check referential integrity

### Debug Mode

Enable debug logging in `application.yml`:

```yaml
logging:
  level:
    com.bristohq.securetotp: DEBUG
    org.springframework.security: DEBUG
```

## API Rate Limits

### Admin Endpoint Limits

- **Analytics**: 60 requests per minute
- **User Management**: 100 requests per minute
- **Bulk Operations**: 10 requests per minute
- **Destructive Actions**: 5 requests per minute

## Support & Maintenance

### Admin Support

- **Documentation**: Comprehensive admin guides
- **Training**: Admin interface training materials
- **Support Tickets**: Priority support for admin issues

### System Maintenance

- **Regular Updates**: Security and feature updates
- **Database Optimization**: Performance tuning
- **Cache Management**: Redis cache optimization
- **Log Rotation**: Automated log management

For admin support or escalation:

- **Emergency Contact**: admin-support@securetotp.com
- **Documentation**: https://admin-docs.securetotp.com
- **Training Portal**: https://training.securetotp.com
