package com.courseplatform.auth.dto;

import com.courseplatform.user.UserRole;
import com.courseplatform.user.UserStatus;

public class UserSummaryDto {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private UserRole role;
    private UserStatus status;
    private boolean coursePurchased;

    public UserSummaryDto() {
    }

    public UserSummaryDto(Long id, String name, String email, String phone, UserRole role, UserStatus status, boolean coursePurchased) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.status = status;
        this.coursePurchased = coursePurchased;
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

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public boolean isCoursePurchased() {
        return coursePurchased;
    }

    public void setCoursePurchased(boolean coursePurchased) {
        this.coursePurchased = coursePurchased;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String name;
        private String email;
        private String phone;
        private UserRole role;
        private UserStatus status;
        private boolean coursePurchased;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public Builder role(UserRole role) {
            this.role = role;
            return this;
        }

        public Builder status(UserStatus status) {
            this.status = status;
            return this;
        }

        public Builder coursePurchased(boolean coursePurchased) {
            this.coursePurchased = coursePurchased;
            return this;
        }

        public UserSummaryDto build() {
            return new UserSummaryDto(id, name, email, phone, role, status, coursePurchased);
        }
    }
}
