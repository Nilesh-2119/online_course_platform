package com.courseplatform.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateAdminUserRequest {

    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Name cannot exceed 100 characters")
    private String name;

    @NotBlank(message = "Email address is required")
    @Email(message = "Invalid email address format")
    private String email;

    private String phone;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    private Boolean grantCourseAccess = true;

    private String role = "USER";

    public CreateAdminUserRequest() {
    }

    public CreateAdminUserRequest(String name, String email, String phone, String password, Boolean grantCourseAccess, String role) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.grantCourseAccess = grantCourseAccess != null ? grantCourseAccess : true;
        this.role = role != null ? role : "USER";
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getGrantCourseAccess() {
        return grantCourseAccess;
    }

    public void setGrantCourseAccess(Boolean grantCourseAccess) {
        this.grantCourseAccess = grantCourseAccess;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
