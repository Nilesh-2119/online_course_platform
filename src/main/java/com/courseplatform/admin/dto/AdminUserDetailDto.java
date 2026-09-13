package com.courseplatform.admin.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class AdminUserDetailDto {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private String role;
    private String status;
    private String type;
    private boolean access;
    private Instant joinedAt;
    private Instant lastLoginAt;
    private String courseTitle;
    private List<UserPurchaseDto> purchases;

    public AdminUserDetailDto() {
    }

    public AdminUserDetailDto(Long id, String name, String email, String phone, String role, String status, String type, boolean access, Instant joinedAt, Instant lastLoginAt, String courseTitle, List<UserPurchaseDto> purchases) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.status = status;
        this.type = type;
        this.access = access;
        this.joinedAt = joinedAt;
        this.lastLoginAt = lastLoginAt;
        this.courseTitle = courseTitle;
        this.purchases = purchases;
    }

    public static class UserPurchaseDto {
        private Long id;
        private String courseTitle;
        private BigDecimal amount;
        private String currency;
        private String status;
        private String razorpayOrderId;
        private String razorpayPaymentId;
        private Instant paidAt;
        private Instant createdAt;

        public UserPurchaseDto() {
        }

        public UserPurchaseDto(Long id, String courseTitle, BigDecimal amount, String currency, String status, String razorpayOrderId, String razorpayPaymentId, Instant paidAt, Instant createdAt) {
            this.id = id;
            this.courseTitle = courseTitle;
            this.amount = amount;
            this.currency = currency;
            this.status = status;
            this.razorpayOrderId = razorpayOrderId;
            this.razorpayPaymentId = razorpayPaymentId;
            this.paidAt = paidAt;
            this.createdAt = createdAt;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getCourseTitle() {
            return courseTitle;
        }

        public void setCourseTitle(String courseTitle) {
            this.courseTitle = courseTitle;
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

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
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

        public Instant getPaidAt() {
            return paidAt;
        }

        public void setPaidAt(Instant paidAt) {
            this.paidAt = paidAt;
        }

        public Instant getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(Instant createdAt) {
            this.createdAt = createdAt;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isAccess() {
        return access;
    }

    public void setAccess(boolean access) {
        this.access = access;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(Instant joinedAt) {
        this.joinedAt = joinedAt;
    }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(Instant lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public void setCourseTitle(String courseTitle) {
        this.courseTitle = courseTitle;
    }

    public List<UserPurchaseDto> getPurchases() {
        return purchases;
    }

    public void setPurchases(List<UserPurchaseDto> purchases) {
        this.purchases = purchases;
    }
}
