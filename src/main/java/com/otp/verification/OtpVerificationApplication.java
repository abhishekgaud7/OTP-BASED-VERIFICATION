package com.otp.verification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class OtpVerificationApplication {

    public static void main(String[] args) {
        SpringApplication.run(OtpVerificationApplication.class, args);
    }

}
