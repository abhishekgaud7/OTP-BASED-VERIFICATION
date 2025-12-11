# OTP-Based Email Verification Microservice

A Spring Boot microservice that issues signed OTP tokens, validates expiry, and secures onboarding flows with audit-grade logs.

## Features

- User registration with email and password
- OTP generation and email delivery
- OTP verification with expiration checks
- JWT token generation for authenticated users
- Comprehensive audit logging
- MySQL database for persistence
- Email notifications using JavaMailSender

## Technology Stack

- **Spring Boot 3.2.0** - Backend framework
- **Spring Security** - Authentication and authorization
- **Spring Data JPA** - Database ORM
- **JWT (jjwt)** - Token-based authentication
- **JavaMailSender** - Email service
- **MySQL** - Relational database
- **Maven** - Build tool
- **Java 17** - Programming language

## Prerequisites

- Java 17 or higher
- Maven 3.8 or higher
- MySQL 8.0 or higher
- Git

## Installation & Setup

### 1. Clone the Repository
```bash
git clone <repository-url>
cd OTP-Based-Verification
```

### 2. Create MySQL Database
```sql
CREATE DATABASE otp_verification;
```

### 3. Configure Application Properties
Edit `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/otp_verification
    username: root
    password: root
  mail:
    host: smtp.gmail.com
    username: your-email@gmail.com
    password: your-app-password

jwt:
  secret: your-secret-key-change-this-in-production-at-least-256-bits-long
```

### 4. Build the Project
```bash
mvn clean install
```

### 5. Run the Application
```bash
mvn spring-boot:run
```

The service will start on `http://localhost:8080/api`

## API Endpoints

### 1. Register User
**POST** `/api/v1/auth/register`

```json
{
  "email": "user@example.com",
  "password": "securePassword123",
  "firstName": "John",
  "lastName": "Doe"
}
```

**Response:**
```json
{
  "message": "User registered successfully. Please verify your email.",
  "success": true,
  "user": {
    "id": 1,
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "emailVerified": false
  }
}
```

### 2. Request OTP
**POST** `/api/v1/auth/request-otp?email=user@example.com`

**Response:**
```json
{
  "message": "OTP sent successfully to your email",
  "success": true,
  "token": "eyJhbGc..."
}
```

### 3. Verify OTP
**POST** `/api/v1/auth/verify-otp`

```json
{
  "email": "user@example.com",
  "otp": "123456"
}
```

**Response:**
```json
{
  "message": "Email verified successfully",
  "success": true,
  "token": "eyJhbGc...",
  "user": {
    "id": 1,
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "emailVerified": true
  }
}
```

### 4. Login
**POST** `/api/v1/auth/login?email=user@example.com&password=securePassword123`

**Response:**
```json
{
  "message": "Login successful",
  "success": true,
  "token": "eyJhbGc...",
  "user": {
    "id": 1,
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "emailVerified": true
  }
}
```

### 5. Health Check
**GET** `/api/v1/auth/health`

## Database Schema

### Users Table
```sql
CREATE TABLE users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  email VARCHAR(255) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL,
  first_name VARCHAR(100) NOT NULL,
  last_name VARCHAR(100) NOT NULL,
  email_verified BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### OTP Tokens Table
```sql
CREATE TABLE otp_tokens (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  token VARCHAR(500) NOT NULL,
  otp VARCHAR(6) NOT NULL,
  expiry_time TIMESTAMP NOT NULL,
  is_used BOOLEAN DEFAULT FALSE,
  attempt_count INT DEFAULT 0,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### Audit Logs Table
```sql
CREATE TABLE audit_logs (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  action VARCHAR(100) NOT NULL,
  entity_type VARCHAR(50) NOT NULL,
  entity_id BIGINT,
  details TEXT NOT NULL,
  ip_address VARCHAR(45) NOT NULL,
  status VARCHAR(20) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## Testing with Postman

1. Import the Postman collection (if provided)
2. Set up environment variables:
   - `base_url`: http://localhost:8080/api
   - `email`: test@example.com
   - `token`: (will be set dynamically after login)

3. Test the flow:
   - Register → Request OTP → Verify OTP → Login

## Configuration

### OTP Settings
- **OTP Length**: 6 digits
- **Expiration Time**: 15 minutes
- **Maximum Attempts**: 3

### JWT Settings
- **Algorithm**: HS256
- **Expiration**: 24 hours (configurable in `application.yml`)

## Logging

Logs are written to:
- Console output
- File: `logs/otp-verification.log`

Log levels can be adjusted in `application.yml`:
```yaml
logging:
  level:
    com.otp.verification: DEBUG
```

## Error Handling

Common error responses:

1. **User Already Exists** (400)
```json
{
  "message": "User with email ... already exists",
  "success": false
}
```

2. **Invalid OTP** (400)
```json
{
  "message": "Invalid OTP",
  "success": false
}
```

3. **OTP Expired** (400)
```json
{
  "message": "OTP has expired",
  "success": false
}
```

4. **User Not Found** (404)
```json
{
  "message": "User with email ... not found",
  "success": false
}
```

## Deployment

### Docker Deployment
```dockerfile
FROM openjdk:17-jdk-slim
COPY target/email-verification-1.0.0.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

## Security Considerations

1. Store JWT secret securely (use environment variables)
2. Use HTTPS in production
3. Configure CORS properly
4. Implement rate limiting for OTP requests
5. Use environment-specific configurations
6. Enable CSRF protection
7. Validate all input data

## Troubleshooting

### Email Not Sending
- Check Gmail app password (not regular password)
- Enable "Less secure app access" if using Gmail
- Verify SMTP settings in `application.yml`

### OTP Verification Issues
- Ensure OTP format is 6 digits
- Check OTP expiration time
- Verify user exists in database
- Check maximum attempt limit

### Database Connection Issues
- Ensure MySQL is running
- Verify credentials in `application.yml`
- Check database `otp_verification` exists

## Future Enhancements

- [ ] Rate limiting for OTP requests
- [ ] Two-factor authentication (2FA)
- [ ] Email verification templates
- [ ] SMS OTP support
- [ ] OAuth2 integration
- [ ] API key authentication
- [ ] WebSocket support for real-time notifications

## License

This project is licensed under the MIT License.

## Support

For issues and questions, please create an issue in the repository.
