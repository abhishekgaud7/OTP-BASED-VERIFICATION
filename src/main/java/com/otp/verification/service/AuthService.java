package com.otp.verification.service;

import com.otp.verification.dto.AuthResponse;
import com.otp.verification.dto.OtpVerificationRequest;
import com.otp.verification.dto.RegisterRequest;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse requestOtp(String email);

    AuthResponse verifyOtp(OtpVerificationRequest request);

    AuthResponse login(String email, String password);
}
