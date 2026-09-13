package com.courseplatform.payment;

import com.courseplatform.common.BaseAuditableEntity;
import com.courseplatform.course.CourseEntity;
import com.courseplatform.user.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "course_purchases")
public class CoursePurchaseEntity extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private CourseEntity course;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "INR";

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PurchaseStatus status = PurchaseStatus.CREATED;

    @Column(name = "razorpay_order_id", unique = true, length = 100)
    private String razorpayOrderId;

    @Column(name = "razorpay_payment_id", unique = true, length = 100)
    private String razorpayPaymentId;

    @Column(name = "razorpay_signature", length = 255)
    private String razorpaySignature;

    @Column(name = "paid_at")
    private Instant paidAt;

    @Column(name = "coupon_code", length = 50)
    private String couponCode;

    @Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "original_amount", precision = 10, scale = 2)
    private BigDecimal originalAmount;

    public CoursePurchaseEntity() {
    }

    public CoursePurchaseEntity(Long id, UserEntity user, CourseEntity course, BigDecimal amount, String currency, PurchaseStatus status, String razorpayOrderId, String razorpayPaymentId, String razorpaySignature, Instant paidAt) {
        this(id, user, course, amount, currency, status, razorpayOrderId, razorpayPaymentId, razorpaySignature, paidAt, null, BigDecimal.ZERO, null);
    }

    public CoursePurchaseEntity(Long id, UserEntity user, CourseEntity course, BigDecimal amount, String currency, PurchaseStatus status, String razorpayOrderId, String razorpayPaymentId, String razorpaySignature, Instant paidAt, String couponCode, BigDecimal discountAmount, BigDecimal originalAmount) {
        this.id = id;
        this.user = user;
        this.course = course;
        this.amount = amount;
        this.currency = currency != null ? currency : "INR";
        this.status = status != null ? status : PurchaseStatus.CREATED;
        this.razorpayOrderId = razorpayOrderId;
        this.razorpayPaymentId = razorpayPaymentId;
        this.razorpaySignature = razorpaySignature;
        this.paidAt = paidAt;
        this.couponCode = couponCode;
        this.discountAmount = discountAmount != null ? discountAmount : BigDecimal.ZERO;
        this.originalAmount = originalAmount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public CourseEntity getCourse() {
        return course;
    }

    public void setCourse(CourseEntity course) {
        this.course = course;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public PurchaseStatus getStatus() {
        return status;
    }

    public void setStatus(PurchaseStatus status) {
        this.status = status;
    }

    public String getRazorpayOrderId() {
        return razorpayOrderId;
    }

    public void setRazorpayOrderId(String razorpayOrderId) {
        this.razorpayOrderId = razorpayOrderId;
    }

    public String getRazorpayPaymentId() {
        return razorpayPaymentId;
    }

    public void setRazorpayPaymentId(String razorpayPaymentId) {
        this.razorpayPaymentId = razorpayPaymentId;
    }

    public String getRazorpaySignature() {
        return razorpaySignature;
    }

    public void setRazorpaySignature(String razorpaySignature) {
        this.razorpaySignature = razorpaySignature;
    }

    public Instant getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(Instant paidAt) {
        this.paidAt = paidAt;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getOriginalAmount() {
        return originalAmount;
    }

    public void setOriginalAmount(BigDecimal originalAmount) {
        this.originalAmount = originalAmount;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private UserEntity user;
        private CourseEntity course;
        private BigDecimal amount;
        private String currency = "INR";
        private PurchaseStatus status = PurchaseStatus.CREATED;
        private String razorpayOrderId;
        private String razorpayPaymentId;
        private String razorpaySignature;
        private Instant paidAt;
        private String couponCode;
        private BigDecimal discountAmount = BigDecimal.ZERO;
        private BigDecimal originalAmount;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder user(UserEntity user) {
            this.user = user;
            return this;
        }

        public Builder course(CourseEntity course) {
            this.course = course;
            return this;
        }

        public Builder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public Builder currency(String currency) {
            this.currency = currency;
            return this;
        }

        public Builder status(PurchaseStatus status) {
            this.status = status;
            return this;
        }

        public Builder razorpayOrderId(String razorpayOrderId) {
            this.razorpayOrderId = razorpayOrderId;
            return this;
        }

        public Builder razorpayPaymentId(String razorpayPaymentId) {
            this.razorpayPaymentId = razorpayPaymentId;
            return this;
        }

        public Builder razorpaySignature(String razorpaySignature) {
            this.razorpaySignature = razorpaySignature;
            return this;
        }

        public Builder paidAt(Instant paidAt) {
            this.paidAt = paidAt;
            return this;
        }

        public Builder couponCode(String couponCode) {
            this.couponCode = couponCode;
            return this;
        }

        public Builder discountAmount(BigDecimal discountAmount) {
            this.discountAmount = discountAmount;
            return this;
        }

        public Builder originalAmount(BigDecimal originalAmount) {
            this.originalAmount = originalAmount;
            return this;
        }

        public CoursePurchaseEntity build() {
            return new CoursePurchaseEntity(id, user, course, amount, currency, status, razorpayOrderId, razorpayPaymentId, razorpaySignature, paidAt, couponCode, discountAmount, originalAmount);
        }
    }
}
