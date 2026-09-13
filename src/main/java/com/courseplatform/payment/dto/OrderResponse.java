package com.courseplatform.payment.dto;

import java.math.BigDecimal;

public class OrderResponse {

    private String razorpayOrderId;
    private BigDecimal amount;
    private String currency;
    private String keyId;
    private String courseTitle;
    private String userName;
    private String userEmail;
    private String userPhone;

    public OrderResponse() {
    }

    public OrderResponse(String razorpayOrderId, BigDecimal amount, String currency, String keyId, String courseTitle, String userName, String userEmail, String userPhone) {
        this.razorpayOrderId = razorpayOrderId;
        this.amount = amount;
        this.currency = currency;
        this.keyId = keyId;
        this.courseTitle = courseTitle;
        this.userName = userName;
        this.userEmail = userEmail;
        this.userPhone = userPhone;
    }

    public String getRazorpayOrderId() {
        return razorpayOrderId;
    }

    public void setRazorpayOrderId(String razorpayOrderId) {
        this.razorpayOrderId = razorpayOrderId;
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

    public String getKeyId() {
        return keyId;
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public void setCourseTitle(String courseTitle) {
        this.courseTitle = courseTitle;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String razorpayOrderId;
        private BigDecimal amount;
        private String currency;
        private String keyId;
        private String courseTitle;
        private String userName;
        private String userEmail;
        private String userPhone;

        public Builder razorpayOrderId(String razorpayOrderId) {
            this.razorpayOrderId = razorpayOrderId;
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

        public Builder keyId(String keyId) {
            this.keyId = keyId;
            return this;
        }

        public Builder courseTitle(String courseTitle) {
            this.courseTitle = courseTitle;
            return this;
        }

        public Builder userName(String userName) {
            this.userName = userName;
            return this;
        }

        public Builder userEmail(String userEmail) {
            this.userEmail = userEmail;
            return this;
        }

        public Builder userPhone(String userPhone) {
            this.userPhone = userPhone;
            return this;
        }

        public OrderResponse build() {
            return new OrderResponse(razorpayOrderId, amount, currency, keyId, courseTitle, userName, userEmail, userPhone);
        }
    }
}
