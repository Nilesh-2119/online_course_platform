package com.courseplatform.auth.otp;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface VerificationOtpRepository extends JpaRepository<VerificationOtpEntity, Long> {

    @Query("""
        SELECT o FROM VerificationOtpEntity o
        WHERE o.user.id = :userId
          AND o.channel = :channel
          AND o.purpose = :purpose
          AND o.consumedAt IS NULL
        ORDER BY o.createdAt DESC
        LIMIT 1
    """)
    Optional<VerificationOtpEntity> findLatestActiveOtp(
            @Param("userId") Long userId,
            @Param("channel") OtpChannel channel,
            @Param("purpose") OtpPurpose purpose
    );

    @Query("""
        SELECT COUNT(o) FROM VerificationOtpEntity o
        WHERE o.user.id = :userId
          AND o.channel = :channel
          AND o.purpose = :purpose
          AND o.createdAt >= :since
    """)
    long countOtpsGeneratedSince(
            @Param("userId") Long userId,
            @Param("channel") OtpChannel channel,
            @Param("purpose") OtpPurpose purpose,
            @Param("since") Instant since
    );

    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true)
    @Query("DELETE FROM VerificationOtpEntity o WHERE o.user.id = :userId")
    int deleteByUserId(@Param("userId") Long userId);
}
