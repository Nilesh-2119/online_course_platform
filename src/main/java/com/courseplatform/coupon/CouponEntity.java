package com.courseplatform.coupon;

import com.courseplatform.common.BaseAuditableEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Coupon entity for discount codes.
 */
@Entity
@Table(name = "coupons")
public class CouponEntity extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false, length = 20)
    private DiscountType discountType;

    @Column(name = "discount_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountValue;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "INR";

    @Column(name = "usage_count", nullable = false)
    private int usageCount = 0;

    @Column(name = "usage_limit", nullable = false)
    private int usageLimit = 100;

    @Column(name = "per_user_limit", nullable = false)
    private int perUserLimit = 1;

    @Column(name = "minimum_purchase", nullable = false, precision = 10, scale = 2)
    private BigDecimal minimumPurchase = BigDecimal.ZERO;

    @Column(name = "start_date")
    private Instant startDate;

    @Column(name = "expiry_date", nullable = false)
    private Instant expiryDate;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "coupon", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CouponUsageEntity> usages = new ArrayList<>();

    public CouponEntity() {
    }

    // --- Getters & Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public DiscountType getDiscountType() {
        return discountType;
    }

    public void setDiscountType(DiscountType discountType) {
        this.discountType = discountType;
    }

    public BigDecimal getDiscountValue() {
        return discountValue;
    }

    public void setDiscountValue(BigDecimal discountValue) {
        this.discountValue = discountValue;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public int getUsageCount() {
        return usageCount;
    }

    public void setUsageCount(int usageCount) {
        this.usageCount = usageCount;
    }

    public int getUsageLimit() {
        return usageLimit;
    }

    public void setUsageLimit(int usageLimit) {
        this.usageLimit = usageLimit;
    }

    public int getPerUserLimit() {
        return perUserLimit;
    }

    public void setPerUserLimit(int perUserLimit) {
        this.perUserLimit = perUserLimit;
    }

    public BigDecimal getMinimumPurchase() {
        return minimumPurchase;
    }

    public void setMinimumPurchase(BigDecimal minimumPurchase) {
        this.minimumPurchase = minimumPurchase;
    }

    public Instant getStartDate() {
        return startDate;
    }

    public void setStartDate(Instant startDate) {
        this.startDate = startDate;
    }

    public Instant getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Instant expiryDate) {
        this.expiryDate = expiryDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<CouponUsageEntity> getUsages() {
        return usages;
    }

    public void setUsages(List<CouponUsageEntity> usages) {
        this.usages = usages;
    }

    // --- Builder ---

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String code;
        private DiscountType discountType;
        private BigDecimal discountValue;
        private String currency = "INR";
        private int usageLimit = 100;
        private int perUserLimit = 1;
        private BigDecimal minimumPurchase = BigDecimal.ZERO;
        private Instant startDate;
        private Instant expiryDate;
        private boolean active = true;

        public Builder code(String code) { this.code = code; return this; }
        public Builder discountType(DiscountType discountType) { this.discountType = discountType; return this; }
        public Builder discountValue(BigDecimal discountValue) { this.discountValue = discountValue; return this; }
        public Builder currency(String currency) { this.currency = currency; return this; }
        public Builder usageLimit(int usageLimit) { this.usageLimit = usageLimit; return this; }
        public Builder perUserLimit(int perUserLimit) { this.perUserLimit = perUserLimit; return this; }
        public Builder minimumPurchase(BigDecimal minimumPurchase) { this.minimumPurchase = minimumPurchase; return this; }
        public Builder startDate(Instant startDate) { this.startDate = startDate; return this; }
        public Builder expiryDate(Instant expiryDate) { this.expiryDate = expiryDate; return this; }
        public Builder active(boolean active) { this.active = active; return this; }

        public CouponEntity build() {
            CouponEntity entity = new CouponEntity();
            entity.code = this.code;
            entity.discountType = this.discountType;
            entity.discountValue = this.discountValue;
            entity.currency = this.currency;
            entity.usageLimit = this.usageLimit;
            entity.perUserLimit = this.perUserLimit;
            entity.minimumPurchase = this.minimumPurchase;
            entity.startDate = this.startDate;
            entity.expiryDate = this.expiryDate;
            entity.active = this.active;
            return entity;
        }
    }
}
