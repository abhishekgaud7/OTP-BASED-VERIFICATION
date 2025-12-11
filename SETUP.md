# OTP-Based Email Verification - Setup & Build Guide

## Project Overview

A complete Spring Boot 3.2 microservice for OTP-based email verification with JWT authentication, MySQL persistence, and comprehensive audit logging.

## Technology Stack Implemented

- ✅ **Spring Boot 3.2.0** - Latest stable release
- ✅ **Java 17** - Long-term support version
- ✅ **Spring Security** - Authentication & authorization
- ✅ **Spring Data JPA** - ORM with Hibernate
- ✅ **JWT (JJWT)** - Token-based authentication
- ✅ **JavaMailSender** - Email notifications
- ✅ **MySQL 8.0+** - Relational database
- ✅ **Maven 3.8+** - Build management
- ✅ **Lombok** - Boilerplate reduction

## Project Structure

```
OTP-Based-Verification/
├── .github/
│   └── copilot-instructions.md
├── .vscode/
│   ├── tasks.json (Build & run tasks)
│   └── settings.json (IDE settings)
├── src/
│   ├── main/
│   │   ├── java/com/otp/verification/
│   │   │   ├── OtpVerificationApplication.java
│   │   │   ├── config/
│   │   │   │   └── SecurityConfig.java
│   │   │   ├── controller/
│   │   │   │   └── AuthController.java
│   │   │   ├── entity/
│   │   │   │   ├── User.java
│   │   │   │   ├── OtpToken.java
│   │   │   │   └── AuditLog.java
│   │   │   ├── dto/
│   │   │   │   ├── RegisterRequest.java
│   │   │   │   ├── OtpVerificationRequest.java
│   │   │   │   ├── AuthResponse.java
│   │   │   │   ├── UserDto.java
│   │   │   │   └── OtpResponse.java
│   │   │   ├── exception/
│   │   │   │   ├── InvalidOtpException.java
│   │   │   │   ├── UserAlreadyExistsException.java
│   │   │   │   └── UserNotFoundException.java
│   │   │   ├── repository/
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── OtpTokenRepository.java
│   │   │   │   └── AuditLogRepository.java
│   │   │   ├── service/
│   │   │   │   ├── AuthService.java
│   │   │   │   ├── AuthServiceImpl.java
│   │   │   │   ├── EmailService.java
│   │   │   │   ├── EmailServiceImpl.java
│   │   │   │   ├── AuditLogService.java
│   │   │   │   └── AuditLogServiceImpl.java
│   │   │   └── util/
│   │   │       ├── JwtUtil.java
│   │   │       ├── OtpUtil.java
│   │   │       └── IpAddressUtil.java
│   │   └── resources/
│   │       └── application.yml
│   └── test/java/com/otp/verification/
├── pom.xml
├── README.md
├── SETUP.md (this file)
├── OTP-Email-Verification.postman_collection.json
└── .gitignore
```

## Quick Start Guide

### 1. Prerequisites Installation

**Java 17:**
```powershell
# Windows: Download from https://adoptium.net/
# Verify installation
java -version
```

**Maven:**
```powershell
# Download from https://maven.apache.org/download.cgi
# Add to Windows PATH environment variable
# Verify installation
mvn -version
```

**MySQL:**
```powershell
# Download from https://dev.mysql.com/downloads/mysql/
# Run installer and complete setup
# Verify MySQL is running
mysql --version
```

### 2. Database Setup

Create the database before running the application:

```sql
-- Login to MySQL
mysql -u root -p

-- Create database
CREATE DATABASE otp_verification CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Exit MySQL
EXIT;
```

### 3. Configure Application

Edit `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/otp_verification
    username: root              # Change to your MySQL username
    password: root              # Change to your MySQL password
  mail:
    host: smtp.gmail.com
    port: 587
    username: your-email@gmail.com        # Your Gmail address
    password: your-app-password           # Gmail App Password (NOT regular password)
    
jwt:
  secret: your-very-long-secret-key-at-least-256-bits-change-in-production
  expiration: 86400000                    # 24 hours in milliseconds
```

**Gmail Setup for Email Sending:**
1. Enable 2-factor authentication on your Gmail account
2. Generate an App Password: https://myaccount.google.com/apppasswords
3. Use the 16-character password in `application.yml`

### 4. Build the Project

**Option A: Using Command Line**
```powershell
cd A:\OTP-Based-Verification
mvn clean install -DskipTests
```

**Option B: Using VS Code Tasks**
1. Open VS Code
2. Press `Ctrl + Shift + B` to run the default build task
3. Or use `Ctrl + Shift + P` → "Tasks: Run Task" → "Maven: Build Project"

### 5. Run the Application

**Option A: Command Line**
```powershell
mvn spring-boot:run
```

**Option B: VS Code Tasks**
1. Use `Ctrl + Shift + P` → "Tasks: Run Task" → "Maven: Run Spring Boot"

**Expected Output:**
```
2025-12-10 10:30:00 - Tomcat started on port(s): 8080 (http) with context path '/api'
2025-12-10 10:30:00 - Started OtpVerificationApplication in X.XXX seconds
```

The service is now running at: **http://localhost:8080/api**

## API Testing with Postman

### Import Collection

1. Open Postman
2. Click "Import" → "Upload Files"
3. Select `OTP-Email-Verification.postman_collection.json`
4. Configure environment variables:
   - `base_url`: http://localhost:8080/api
   - `email`: (auto-filled)
   - `token`: (auto-filled)

### Test Workflow

1. **Health Check**
   - GET `/v1/auth/health`
   - Expected: "Service is running"

2. **Register User**
   - POST `/v1/auth/register`
   - Body: Email, password, firstName, lastName
   - Returns: User details

3. **Request OTP**
   - POST `/v1/auth/request-otp?email=user@example.com`
   - Check your email for OTP code
   - Returns: JWT token

4. **Verify OTP**
   - POST `/v1/auth/verify-otp`
   - Body: Email and OTP from email
   - Returns: JWT token + verified user

5. **Login**
   - POST `/v1/auth/login?email=user@example.com&password=...`
   - Returns: JWT token + user details

## Configuration Options

### OTP Settings
- Length: 6 digits
- Expiration: 15 minutes
- Max attempts: 3

### JWT Settings
- Algorithm: HS256
- Default expiration: 24 hours
- Can be configured in `application.yml`

### Logging
- Logs written to console and `logs/otp-verification.log`
- Adjust levels in `application.yml`:
  ```yaml
  logging:
    level:
      com.otp.verification: DEBUG
  ```

## Database Schema

### Users Table
- Stores user registration data
- Tracks email verification status
- Timestamps for audit trail

### OTP Tokens Table
- Stores generated OTP codes
- Tracks token expiration
- Records usage and attempt count

### Audit Logs Table
- Comprehensive action logging
- IP address tracking
- Status and timestamp recording

### Auto-generated Tables
JPA will automatically create all tables on first run if `ddl-auto: update` is set.

## Troubleshooting

### Build Issues

**"mvn is not recognized"**
- Install Maven and add to PATH
- Or use full path to Maven executable
- Or use Docker (see Docker section)

**"Cannot find symbol" errors**
- Ensure Java 17+ is installed
- Run `mvn clean install` to download dependencies
- Check internet connection

### Runtime Issues

**"MySQL connection failed"**
- Ensure MySQL service is running
- Verify credentials in `application.yml`
- Check database `otp_verification` exists
- Verify firewall isn't blocking port 3306

**"No suitable driver found for jdbc:mysql"**
- Run `mvn clean install` to download drivers
- Clear Maven cache if needed

**"SMTP connection failed"**
- Verify Gmail App Password (not regular password)
- Check 2-factor authentication is enabled
- Verify SMTP settings in `application.yml`
- Check firewall/antivirus isn't blocking port 587

**"OTP not received"**
- Check spam/junk folder
- Verify email address in registration
- Check application logs for errors
- Ensure Gmail is properly configured

## Docker Deployment (Optional)

Create `Dockerfile`:
```dockerfile
FROM openjdk:17-jdk-slim
COPY target/email-verification-1.0.0.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

Build and run:
```powershell
docker build -t otp-verification .
docker run -p 8080:8080 otp-verification
```

## Performance Optimization

1. **Database Indexing**: Add indexes on frequently queried columns
2. **Connection Pooling**: Configure HikariCP settings
3. **Caching**: Add Spring Cache for OTP validation
4. **Rate Limiting**: Implement to prevent OTP brute-force

## Security Checklist

- [ ] Change JWT secret to a strong value
- [ ] Use HTTPS in production
- [ ] Configure CORS properly
- [ ] Enable request rate limiting
- [ ] Use environment variables for secrets
- [ ] Implement request validation
- [ ] Enable CSRF protection
- [ ] Use parameterized queries (JPA does this)
- [ ] Sanitize all inputs
- [ ] Regular dependency updates

## Next Steps

1. **Testing**: Run integration tests after setup
2. **Documentation**: Update API documentation with your changes
3. **Deployment**: Containerize and deploy to production
4. **Monitoring**: Set up logging and monitoring
5. **Enhancement**: Add additional features as needed

## Support Resources

- Spring Boot Docs: https://spring.io/projects/spring-boot
- JWT Documentation: https://github.com/jwtk/jjwt
- MySQL Docs: https://dev.mysql.com/doc/
- Postman Docs: https://learning.postman.com/

## Common Commands

```powershell
# Build project
mvn clean install -DskipTests

# Run application
mvn spring-boot:run

# Run tests
mvn test

# Generate project documentation
mvn site

# Check dependencies
mvn dependency:tree

# Clean build artifacts
mvn clean
```

---

**Project Status**: ✅ Ready for Development & Testing

**Created**: December 10, 2025
**Spring Boot Version**: 3.2.0
**Java Version**: 17 LTS
