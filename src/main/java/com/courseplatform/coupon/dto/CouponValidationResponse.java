package com.courseplatform.coupon.dto;

import java.math.BigDecimal;

/**
 * Response DTO for coupon validation at checkout.
 */
public class CouponValidationResponse {

    private boolean valid;
    private String message;
    private Long couponId;
    private String couponCode;
    private String discountType;
    private BigDecimal discountValue;
    private BigDecimal originalPrice;
    private BigDecimal discountAmount;
    private BigDecimal finalPrice;

    public CouponValidationResponse() {
    }

    /** Build an invalid/error response. */
    public static CouponValidationResponse invalid(String message) {
        CouponValidationResponse r = new CouponValidationResponse();
        r.valid = false;
        r.message = message;
        return r;
    }

    /** Build a valid response with discount breakdown. */
    public static CouponValidationResponse valid(Long couponId, String couponCode, String discountType,
                                                   BigDecimal discountValue, BigDecimal originalPrice,
                                                   BigDecimal discountAmount, BigDecimal finalPrice) {
        CouponValidationResponse r = new CouponValidationResponse();
        r.valid = true;
        r.message = "Coupon applied successfully";
        r.couponId = couponId;
        r.couponCode = couponCode;
        r.discountType = discountType;
        r.discountValue = discountValue;
        r.originalPrice = originalPrice;
        r.discountAmount = discountAmount;
        r.finalPrice = finalPrice;
        return r;
    }

    // --- Getters & Setters ---

    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Long getCouponId() { return couponId; }
    public void setCouponId(Long couponId) { this.couponId = couponId; }

    public String getCouponCode() { return couponCode; }
    public void setCouponCode(String couponCode) { this.couponCode = couponCode; }

    public String getDiscountType() { return discountType; }
    public void setDiscountType(String discountType) { this.discountType = discountType; }

    public BigDecimal getDiscountValue() { return discountValue; }
    public void setDiscountValue(BigDecimal discountValue) { this.discountValue = discountValue; }

    public BigDecimal getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(BigDecimal originalPrice) { this.originalPrice = originalPrice; }

    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }

    public BigDecimal getFinalPrice() { return finalPrice; }
    public void setFinalPrice(BigDecimal finalPrice) { this.finalPrice = finalPrice; }
}
