package com.courseplatform.user.dto;

public class UserProfileResponse {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private String role;
    private String status;
    private String type;
    private boolean coursePurchased;

    public UserProfileResponse() {
    }

    public UserProfileResponse(Long id, String name, String email, String phone, boolean coursePurchased) {
        this(id, name, email, phone, "USER", "ACTIVE", coursePurchased ? "paid" : "free", coursePurchased);
    }

    public UserProfileResponse(Long id, String name, String email, String phone, String role, String status, String type, boolean coursePurchased) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.status = status;
        this.type = type;
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
        private String role = "USER";
        private String status = "ACTIVE";
        private String type = "free";
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

        public Builder role(String role) {
            this.role = role;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder coursePurchased(boolean coursePurchased) {
            this.coursePurchased = coursePurchased;
            return this;
        }

        public UserProfileResponse build() {
            return new UserProfileResponse(id, name, email, phone, role, status, type, coursePurchased);
        }
    }
}
