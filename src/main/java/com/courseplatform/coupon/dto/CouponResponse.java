package com.courseplatform.coupon.dto;

import com.courseplatform.coupon.CouponEntity;
import com.courseplatform.coupon.DiscountType;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Response DTO for coupon data.
 */
public class CouponResponse {

    private Long id;
    private String code;
    private String discountType;
    private BigDecimal discountValue;
    private String currency;
    private int usageCount;
    private int usageLimit;
    private int perUserLimit;
    private BigDecimal minimumPurchase;
    private Instant startDate;
    private Instant expiryDate;
    private boolean active;
    private String status; // ACTIVE, INACTIVE, EXPIRED (computed)
    private Instant createdAt;

    public CouponResponse() {
    }

    /**
     * Build a CouponResponse from a CouponEntity.
     */
    public static CouponResponse from(CouponEntity entity) {
        CouponResponse r = new CouponResponse();
        r.id = entity.getId();
        r.code = entity.getCode();
        r.discountType = entity.getDiscountType().name();
        r.discountValue = entity.getDiscountValue();
        r.currency = entity.getCurrency();
        r.usageCount = entity.getUsageCount();
        r.usageLimit = entity.getUsageLimit();
        r.perUserLimit = entity.getPerUserLimit();
        r.minimumPurchase = entity.getMinimumPurchase();
        r.startDate = entity.getStartDate();
        r.expiryDate = entity.getExpiryDate();
        r.active = entity.isActive();
        r.createdAt = entity.getCreatedAt();

        // Compute display status
        if (!entity.isActive()) {
            r.status = "INACTIVE";
        } else if (entity.getExpiryDate().isBefore(Instant.now())) {
            r.status = "EXPIRED";
        } else if (entity.getUsageCount() >= entity.getUsageLimit()) {
            r.status = "EXHAUSTED";
        } else {
            r.status = "ACTIVE";
        }

        return r;
    }

    // --- Getters & Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getDiscountType() { return discountType; }
    public void setDiscountType(String discountType) { this.discountType = discountType; }

    public BigDecimal getDiscountValue() { return discountValue; }
    public void setDiscountValue(BigDecimal discountValue) { this.discountValue = discountValue; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public int getUsageCount() { return usageCount; }
    public void setUsageCount(int usageCount) { this.usageCount = usageCount; }

    public int getUsageLimit() { return usageLimit; }
    public void setUsageLimit(int usageLimit) { this.usageLimit = usageLimit; }

    public int getPerUserLimit() { return perUserLimit; }
    public void setPerUserLimit(int perUserLimit) { this.perUserLimit = perUserLimit; }

    public BigDecimal getMinimumPurchase() { return minimumPurchase; }
    public void setMinimumPurchase(BigDecimal minimumPurchase) { this.minimumPurchase = minimumPurchase; }

    public Instant getStartDate() { return startDate; }
    public void setStartDate(Instant startDate) { this.startDate = startDate; }

    public Instant getExpiryDate() { return expiryDate; }
    public void setExpiryDate(Instant expiryDate) { this.expiryDate = expiryDate; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
