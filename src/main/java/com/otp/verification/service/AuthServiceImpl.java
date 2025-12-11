package com.otp.verification.service;

import com.otp.verification.dto.AuthResponse;
import com.otp.verification.dto.OtpVerificationRequest;
import com.otp.verification.dto.RegisterRequest;
import com.otp.verification.dto.UserDto;
import com.otp.verification.entity.OtpToken;
import com.otp.verification.entity.User;
import com.otp.verification.exception.InvalidOtpException;
import com.otp.verification.exception.UserAlreadyExistsException;
import com.otp.verification.exception.UserNotFoundException;
import com.otp.verification.repository.OtpTokenRepository;
import com.otp.verification.repository.UserRepository;
import com.otp.verification.util.JwtUtil;
import com.otp.verification.util.OtpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final OtpTokenRepository otpTokenRepository;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;
    private final OtpUtil otpUtil;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    private static final long OTP_EXPIRATION_MINUTES = 15;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        try {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new UserAlreadyExistsException("User with email " + request.getEmail() + " already exists");
            }

            User user = User.builder()
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .emailVerified(false)
                    .build();

            user = userRepository.save(user);
            log.info("User registered successfully: {}", request.getEmail());
            
            auditLogService.logAction("USER_REGISTRATION", "User", user.getId(), 
                    "User registration initiated for email: " + request.getEmail(), "SUCCESS");

            return AuthResponse.builder()
                    .message("User registered successfully. Please verify your email.")
                    .success(true)
                    .user(mapUserToDto(user))
                    .build();

        } catch (UserAlreadyExistsException e) {
            log.warn("Registration failed: {}", e.getMessage());
            auditLogService.logAction("USER_REGISTRATION", "User", null, 
                    "Registration failed: " + request.getEmail(), "FAILED");
            throw e;
        }
    }

    @Override
    @Transactional
    public AuthResponse requestOtp(String email) {
        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UserNotFoundException("User with email " + email + " not found"));

            // Delete expired tokens
            otpTokenRepository.deleteExpiredTokens(LocalDateTime.now());

            String otp = otpUtil.generateOtp();
            String token = jwtUtil.generateToken(email);
            LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(OTP_EXPIRATION_MINUTES);

            OtpToken otpToken = OtpToken.builder()
                    .user(user)
                    .otp(otp)
                    .token(token)
                    .expiryTime(expiryTime)
                    .build();

            otpToken = otpTokenRepository.save(otpToken);
            
            emailService.sendOtpEmail(email, otp);
            log.info("OTP sent successfully to: {}", email);
            
            auditLogService.logAction("OTP_REQUEST", "User", user.getId(), 
                    "OTP requested for email: " + email, "SUCCESS");

            return AuthResponse.builder()
                    .message("OTP sent successfully to your email")
                    .success(true)
                    .token(token)
                    .build();

        } catch (UserNotFoundException e) {
            log.warn("OTP request failed: {}", e.getMessage());
            auditLogService.logAction("OTP_REQUEST", "User", null, 
                    "OTP request failed for email: " + email, "FAILED");
            throw e;
        }
    }

    @Override
    @Transactional
    public AuthResponse verifyOtp(OtpVerificationRequest request) {
        try {
            if (!otpUtil.isValidOtp(request.getOtp())) {
                throw new InvalidOtpException("OTP format is invalid");
            }

            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new UserNotFoundException("User with email " + request.getEmail() + " not found"));

            OtpToken otpToken = otpTokenRepository.findByUserAndOtpAndIsUsedFalse(user, request.getOtp())
                    .orElseThrow(() -> new InvalidOtpException("Invalid OTP"));

            if (LocalDateTime.now().isAfter(otpToken.getExpiryTime())) {
                throw new InvalidOtpException("OTP has expired");
            }

            if (otpToken.getAttemptCount() >= 3) {
                throw new InvalidOtpException("Maximum attempts exceeded");
            }

            otpToken.setIsUsed(true);
            otpTokenRepository.save(otpToken);

            user.setEmailVerified(true);
            userRepository.save(user);

            String jwtToken = jwtUtil.generateToken(request.getEmail());
            log.info("Email verified successfully for: {}", request.getEmail());
            
            auditLogService.logAction("OTP_VERIFICATION", "User", user.getId(), 
                    "Email verified for: " + request.getEmail(), "SUCCESS");

            return AuthResponse.builder()
                    .message("Email verified successfully")
                    .success(true)
                    .token(jwtToken)
                    .user(mapUserToDto(user))
                    .build();

        } catch (InvalidOtpException e) {
            log.warn("OTP verification failed: {}", e.getMessage());
            auditLogService.logAction("OTP_VERIFICATION", "User", null, 
                    "OTP verification failed for email: " + request.getEmail(), "FAILED");
            throw e;
        }
    }

    @Override
    @Transactional
    public AuthResponse login(String email, String password) {
        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UserNotFoundException("User with email " + email + " not found"));

            if (!passwordEncoder.matches(password, user.getPassword())) {
                throw new InvalidOtpException("Invalid password");
            }

            if (!user.getEmailVerified()) {
                throw new InvalidOtpException("Email not verified. Please verify your email first.");
            }

            String token = jwtUtil.generateToken(email);
            log.info("User logged in successfully: {}", email);
            
            auditLogService.logAction("USER_LOGIN", "User", user.getId(), 
                    "User login successful", "SUCCESS");

            return AuthResponse.builder()
                    .message("Login successful")
                    .success(true)
                    .token(token)
                    .user(mapUserToDto(user))
                    .build();

        } catch (Exception e) {
            log.warn("Login failed for user: {}", email);
            auditLogService.logAction("USER_LOGIN", "User", null, 
                    "Login failed for email: " + email, "FAILED");
            throw e;
        }
    }

    private UserDto mapUserToDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .emailVerified(user.getEmailVerified())
                .build();
    }
}
