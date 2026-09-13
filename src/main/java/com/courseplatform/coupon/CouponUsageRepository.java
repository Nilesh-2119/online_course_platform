package com.courseplatform.coupon;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Repository for coupon usage tracking.
 */
@Repository
public interface CouponUsageRepository extends JpaRepository<CouponUsageEntity, Long> {

    long countByCouponIdAndUserId(Long couponId, Long userId);

    long countByCouponId(Long couponId);

    @Modifying
    @Transactional
    @Query("DELETE FROM CouponUsageEntity u WHERE u.coupon.id = :couponId")
    void deleteByCouponId(@Param("couponId") Long couponId);
}
