package com.courseplatform.coupon;

import com.courseplatform.payment.CoursePurchaseEntity;
import com.courseplatform.user.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Tracks individual coupon usages — which user used which coupon on which purchase.
 */
@Entity
@Table(name = "coupon_usages")
public class CouponUsageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "coupon_id", nullable = false)
    private CouponEntity coupon;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_id")
    private CoursePurchaseEntity purchase;

    @Column(name = "used_at", nullable = false)
    private Instant usedAt = Instant.now();

    public CouponUsageEntity() {
    }

    public CouponUsageEntity(CouponEntity coupon, UserEntity user, CoursePurchaseEntity purchase) {
        this.coupon = coupon;
        this.user = user;
        this.purchase = purchase;
        this.usedAt = Instant.now();
    }

    // --- Getters & Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public CouponEntity getCoupon() { return coupon; }
    public void setCoupon(CouponEntity coupon) { this.coupon = coupon; }

    public UserEntity getUser() { return user; }
    public void setUser(UserEntity user) { this.user = user; }

    public CoursePurchaseEntity getPurchase() { return purchase; }
    public void setPurchase(CoursePurchaseEntity purchase) { this.purchase = purchase; }

    public Instant getUsedAt() { return usedAt; }
    public void setUsedAt(Instant usedAt) { this.usedAt = usedAt; }
}
