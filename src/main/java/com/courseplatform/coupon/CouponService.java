package com.courseplatform.coupon;

import com.courseplatform.common.exception.ResourceNotFoundException;
import com.courseplatform.coupon.dto.CouponResponse;
import com.courseplatform.coupon.dto.CouponValidationResponse;
import com.courseplatform.coupon.dto.CreateCouponRequest;
import com.courseplatform.course.CourseEntity;
import com.courseplatform.course.CourseRepository;
import com.courseplatform.course.CourseStatus;
import com.courseplatform.payment.CoursePurchaseEntity;
import com.courseplatform.user.UserEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for coupon CRUD and validation logic.
 */
@Service
public class CouponService {

    private static final Logger log = LoggerFactory.getLogger(CouponService.class);

    private final CouponRepository couponRepository;
    private final CouponUsageRepository couponUsageRepository;
    private final CourseRepository courseRepository;

    public CouponService(CouponRepository couponRepository,
                         CouponUsageRepository couponUsageRepository,
                         CourseRepository courseRepository) {
        this.couponRepository = couponRepository;
        this.couponUsageRepository = couponUsageRepository;
        this.courseRepository = courseRepository;
    }

    // ========================================================================
    // Admin CRUD
    // ========================================================================

    @Transactional(readOnly = true)
    public List<CouponResponse> getAllCoupons() {
        return couponRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(CouponResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CouponResponse getCoupon(Long id) {
        CouponEntity entity = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon", id));
        return CouponResponse.from(entity);
    }

    @Transactional
    public CouponResponse createCoupon(CreateCouponRequest request) {
        String code = request.getCode().trim().toUpperCase();

        if (couponRepository.existsByCodeIgnoreCase(code)) {
            throw new IllegalArgumentException("Coupon code '" + code + "' already exists");
        }

        DiscountType discountType;
        try {
            discountType = DiscountType.valueOf(request.getDiscountType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid discount type. Must be PERCENTAGE or FIXED");
        }

        // Validate percentage range
        if (discountType == DiscountType.PERCENTAGE && request.getDiscountValue().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("Percentage discount cannot exceed 100%");
        }

        CouponEntity entity = CouponEntity.builder()
                .code(code)
                .discountType(discountType)
                .discountValue(request.getDiscountValue())
                .currency(request.getCurrency() != null ? request.getCurrency() : "INR")
                .usageLimit(request.getUsageLimit() > 0 ? request.getUsageLimit() : 100)
                .perUserLimit(request.getPerUserLimit() > 0 ? request.getPerUserLimit() : 1)
                .minimumPurchase(request.getMinimumPurchase() != null ? request.getMinimumPurchase() : BigDecimal.ZERO)
                .startDate(request.getStartDate())
                .expiryDate(request.getExpiryDate())
                .active(request.isActive())
                .build();

        entity = couponRepository.save(entity);
        log.info("Created coupon: code={}, type={}, value={}", code, discountType, request.getDiscountValue());
        return CouponResponse.from(entity);
    }

    @Transactional
    public CouponResponse updateCoupon(Long id, CreateCouponRequest request) {
        CouponEntity entity = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon", id));

        String newCode = request.getCode().trim().toUpperCase();
        if (!entity.getCode().equalsIgnoreCase(newCode) && couponRepository.existsByCodeIgnoreCase(newCode)) {
            throw new IllegalArgumentException("Coupon code '" + newCode + "' already exists");
        }

        DiscountType discountType;
        try {
            discountType = DiscountType.valueOf(request.getDiscountType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid discount type. Must be PERCENTAGE or FIXED");
        }

        entity.setCode(newCode);
        entity.setDiscountType(discountType);
        entity.setDiscountValue(request.getDiscountValue());
        entity.setCurrency(request.getCurrency() != null ? request.getCurrency() : "INR");
        entity.setUsageLimit(request.getUsageLimit() > 0 ? request.getUsageLimit() : 100);
        entity.setPerUserLimit(request.getPerUserLimit() > 0 ? request.getPerUserLimit() : 1);
        entity.setMinimumPurchase(request.getMinimumPurchase() != null ? request.getMinimumPurchase() : BigDecimal.ZERO);
        entity.setStartDate(request.getStartDate());
        entity.setExpiryDate(request.getExpiryDate());
        entity.setActive(request.isActive());

        entity = couponRepository.save(entity);
        log.info("Updated coupon ID {}: code={}", id, newCode);
        return CouponResponse.from(entity);
    }

    @Transactional
    public void deleteCoupon(Long id) {
        CouponEntity entity = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon", id));
        // Delete all associated coupon usages first to prevent foreign key constraint failure
        couponUsageRepository.deleteByCouponId(id);
        couponRepository.delete(entity);
        log.info("Deleted coupon ID {}: code={}", id, entity.getCode());
    }

    @Transactional
    public CouponResponse toggleActive(Long id) {
        CouponEntity entity = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon", id));
        entity.setActive(!entity.isActive());
        entity = couponRepository.save(entity);
        log.info("Toggled coupon ID {} active={}", id, entity.isActive());
        return CouponResponse.from(entity);
    }

    // ========================================================================
    // Public validation (used at checkout)
    // ========================================================================

    /**
     * Validates a coupon code for a given user and course price.
     * Returns discount breakdown if valid, or an error message if invalid.
     */
    @Transactional(readOnly = true)
    public CouponValidationResponse validateCoupon(String code, Long userId, Long courseId) {
        if (code == null || code.isBlank()) {
            return CouponValidationResponse.invalid("Coupon code is required");
        }

        String normalizedCode = code.trim().toUpperCase();

        // 1. Find coupon
        CouponEntity coupon = couponRepository.findByCodeIgnoreCase(normalizedCode).orElse(null);
        if (coupon == null) {
            return CouponValidationResponse.invalid("Invalid coupon code");
        }

        // 2. Check active
        if (!coupon.isActive()) {
            return CouponValidationResponse.invalid("This coupon is no longer active");
        }

        // 3. Check expiry
        Instant now = Instant.now();
        if (coupon.getExpiryDate().isBefore(now)) {
            return CouponValidationResponse.invalid("This coupon has expired");
        }

        // 4. Check start date
        if (coupon.getStartDate() != null && coupon.getStartDate().isAfter(now)) {
            return CouponValidationResponse.invalid("This coupon is not yet valid");
        }

        // 5. Check total usage limit
        if (coupon.getUsageCount() >= coupon.getUsageLimit()) {
            return CouponValidationResponse.invalid("This coupon has reached its usage limit");
        }

        // 6. Check per-user limit
        long userUsageCount = couponUsageRepository.countByCouponIdAndUserId(coupon.getId(), userId);
        if (userUsageCount >= coupon.getPerUserLimit()) {
            return CouponValidationResponse.invalid("You have already used this coupon");
        }

        // 7. Get course price
        CourseEntity course = courseRepository.findById(courseId).orElse(null);
        if (course == null || course.getStatus() != CourseStatus.PUBLISHED) {
            return CouponValidationResponse.invalid("Course not found");
        }
        BigDecimal originalPrice = course.getPrice();

        // 8. Check minimum purchase
        if (coupon.getMinimumPurchase() != null && originalPrice.compareTo(coupon.getMinimumPurchase()) < 0) {
            return CouponValidationResponse.invalid("Minimum purchase amount of ₹" + coupon.getMinimumPurchase() + " not met");
        }

        // 9. Calculate discount
        BigDecimal discountAmount;
        if (coupon.getDiscountType() == DiscountType.PERCENTAGE) {
            discountAmount = originalPrice
                    .multiply(coupon.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else {
            discountAmount = coupon.getDiscountValue();
        }

        // Ensure discount doesn't exceed original price
        if (discountAmount.compareTo(originalPrice) > 0) {
            discountAmount = originalPrice;
        }

        BigDecimal finalPrice = originalPrice.subtract(discountAmount);
        if (finalPrice.compareTo(BigDecimal.ZERO) < 0) {
            finalPrice = BigDecimal.ZERO;
        }

        return CouponValidationResponse.valid(
                coupon.getId(),
                coupon.getCode(),
                coupon.getDiscountType().name(),
                coupon.getDiscountValue(),
                originalPrice,
                discountAmount,
                finalPrice
        );
    }

    /**
     * Record coupon usage after a successful payment.
     * Called by PaymentService after payment verification succeeds.
     */
    @Transactional
    public void recordCouponUsage(String couponCode, UserEntity user, CoursePurchaseEntity purchase) {
        if (couponCode == null || couponCode.isBlank()) return;

        CouponEntity coupon = couponRepository.findByCodeIgnoreCase(couponCode.trim().toUpperCase()).orElse(null);
        if (coupon == null) {
            log.warn("Cannot record usage: coupon code '{}' not found", couponCode);
            return;
        }

        // Create usage record
        CouponUsageEntity usage = new CouponUsageEntity(coupon, user, purchase);
        couponUsageRepository.save(usage);

        // Increment usage count
        coupon.setUsageCount(coupon.getUsageCount() + 1);
        couponRepository.save(coupon);

        log.info("Recorded coupon usage: code={}, userId={}, purchaseId={}",
                coupon.getCode(), user.getId(), purchase.getId());
    }

    /**
     * Validate coupon server-side during order creation.
     * Returns the CouponEntity if valid, throws exception if invalid.
     */
    @Transactional(readOnly = true)
    public CouponValidationResult validateForOrder(String couponCode, Long userId, BigDecimal coursePrice) {
        if (couponCode == null || couponCode.isBlank()) {
            return new CouponValidationResult(null, coursePrice, BigDecimal.ZERO);
        }

        String normalizedCode = couponCode.trim().toUpperCase();
        CouponEntity coupon = couponRepository.findByCodeIgnoreCase(normalizedCode)
                .orElseThrow(() -> new IllegalArgumentException("Invalid coupon code: " + normalizedCode));

        Instant now = Instant.now();

        if (!coupon.isActive()) {
            throw new IllegalArgumentException("Coupon '" + normalizedCode + "' is no longer active");
        }
        if (coupon.getExpiryDate().isBefore(now)) {
            throw new IllegalArgumentException("Coupon '" + normalizedCode + "' has expired");
        }
        if (coupon.getStartDate() != null && coupon.getStartDate().isAfter(now)) {
            throw new IllegalArgumentException("Coupon '" + normalizedCode + "' is not yet valid");
        }
        if (coupon.getUsageCount() >= coupon.getUsageLimit()) {
            throw new IllegalArgumentException("Coupon '" + normalizedCode + "' has reached its usage limit");
        }

        long userUsageCount = couponUsageRepository.countByCouponIdAndUserId(coupon.getId(), userId);
        if (userUsageCount >= coupon.getPerUserLimit()) {
            throw new IllegalArgumentException("You have already used coupon '" + normalizedCode + "'");
        }

        if (coupon.getMinimumPurchase() != null && coursePrice.compareTo(coupon.getMinimumPurchase()) < 0) {
            throw new IllegalArgumentException("Minimum purchase amount for coupon '" + normalizedCode + "' not met");
        }

        // Calculate discount
        BigDecimal discountAmount;
        if (coupon.getDiscountType() == DiscountType.PERCENTAGE) {
            discountAmount = coursePrice
                    .multiply(coupon.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else {
            discountAmount = coupon.getDiscountValue();
        }

        if (discountAmount.compareTo(coursePrice) > 0) {
            discountAmount = coursePrice;
        }

        BigDecimal finalPrice = coursePrice.subtract(discountAmount);
        if (finalPrice.compareTo(BigDecimal.ZERO) < 0) {
            finalPrice = BigDecimal.ZERO;
        }

        return new CouponValidationResult(coupon, finalPrice, discountAmount);
    }

    /**
     * Internal result object for validated coupon during order creation.
     */
    public static class CouponValidationResult {
        private final CouponEntity coupon;
        private final BigDecimal finalPrice;
        private final BigDecimal discountAmount;

        public CouponValidationResult(CouponEntity coupon, BigDecimal finalPrice, BigDecimal discountAmount) {
            this.coupon = coupon;
            this.finalPrice = finalPrice;
            this.discountAmount = discountAmount;
        }

        public CouponEntity getCoupon() { return coupon; }
        public BigDecimal getFinalPrice() { return finalPrice; }
        public BigDecimal getDiscountAmount() { return discountAmount; }
        public boolean hasCoupon() { return coupon != null; }
    }
}
