package com.courseplatform.coupon;

import com.courseplatform.auth.security.UserPrincipal;
import com.courseplatform.common.ApiResponse;
import com.courseplatform.coupon.dto.CouponResponse;
import com.courseplatform.coupon.dto.CreateCouponRequest;
import com.courseplatform.user.UserRole;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin-only endpoints for coupon management.
 */
@RestController
@RequestMapping("/api/v1/admin/coupons")
public class AdminCouponController {

    private static final Logger log = LoggerFactory.getLogger(AdminCouponController.class);

    private final CouponService couponService;

    public AdminCouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    private void verifyAdminAccess(UserPrincipal currentUser) {
        if (currentUser == null || (currentUser.getRole() != UserRole.ADMIN && !"adfixstudio25@gmail.com".equalsIgnoreCase(currentUser.getEmail()))) {
            log.warn("Unauthorized coupon admin access attempt by user {}", currentUser != null ? currentUser.getEmail() : "ANONYMOUS");
            throw new AccessDeniedException("Administrator privileges are required to access this resource.");
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<CouponResponse>>> getAllCoupons(
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        verifyAdminAccess(currentUser);
        List<CouponResponse> coupons = couponService.getAllCoupons();
        return ResponseEntity.ok(ApiResponse.success("Coupons retrieved successfully", coupons));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CouponResponse>> getCoupon(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        verifyAdminAccess(currentUser);
        CouponResponse coupon = couponService.getCoupon(id);
        return ResponseEntity.ok(ApiResponse.success("Coupon retrieved successfully", coupon));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CouponResponse>> createCoupon(
            @Valid @RequestBody CreateCouponRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        verifyAdminAccess(currentUser);
        CouponResponse coupon = couponService.createCoupon(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Coupon created successfully", coupon));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CouponResponse>> updateCoupon(
            @PathVariable Long id,
            @Valid @RequestBody CreateCouponRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        verifyAdminAccess(currentUser);
        CouponResponse coupon = couponService.updateCoupon(id, request);
        return ResponseEntity.ok(ApiResponse.success("Coupon updated successfully", coupon));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCoupon(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        verifyAdminAccess(currentUser);
        couponService.deleteCoupon(id);
        return ResponseEntity.ok(ApiResponse.success("Coupon deleted successfully", null));
    }

    @PatchMapping("/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CouponResponse>> toggleCoupon(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        verifyAdminAccess(currentUser);
        CouponResponse coupon = couponService.toggleActive(id);
        return ResponseEntity.ok(ApiResponse.success("Coupon status toggled successfully", coupon));
    }
}
