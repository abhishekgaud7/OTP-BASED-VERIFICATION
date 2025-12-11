package com.otp.verification.repository;

import com.otp.verification.entity.OtpToken;
import com.otp.verification.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

@Repository
public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {

    Optional<OtpToken> findByTokenAndUser(String token, User user);

    Optional<OtpToken> findByUserAndOtpAndIsUsedFalse(User user, String otp);

    @Query("SELECT o FROM OtpToken o WHERE o.user = :user AND o.isUsed = false AND o.expiryTime > :now ORDER BY o.createdAt DESC LIMIT 1")
    Optional<OtpToken> findLatestValidOtpByUser(@Param("user") User user, @Param("now") LocalDateTime now);

    List<OtpToken> findByUserAndIsUsedTrue(User user);

    @Query("DELETE FROM OtpToken o WHERE o.expiryTime < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);
}
