package com.otp.verification.service;

public interface EmailService {

    void sendOtpEmail(String email, String otp);

    void sendWelcomeEmail(String email, String firstName);
}
