package com.courseplatform.coupon;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for coupon CRUD and lookup operations.
 */
@Repository
public interface CouponRepository extends JpaRepository<CouponEntity, Long> {

    Optional<CouponEntity> findByCodeIgnoreCase(String code);

    List<CouponEntity> findAllByOrderByCreatedAtDesc();

    boolean existsByCodeIgnoreCase(String code);
}
