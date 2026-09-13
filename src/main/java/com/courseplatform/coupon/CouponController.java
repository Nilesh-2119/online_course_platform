package com.courseplatform.coupon;

import com.courseplatform.auth.security.UserPrincipal;
import com.courseplatform.common.ApiResponse;
import com.courseplatform.coupon.dto.CouponValidationResponse;
import com.courseplatform.coupon.dto.ValidateCouponRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public (authenticated user) endpoint for coupon validation at checkout.
 */
@RestController
@RequestMapping("/api/v1/coupons")
public class CouponController {

    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<CouponValidationResponse>> validateCoupon(
            @Valid @RequestBody ValidateCouponRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        CouponValidationResponse result = couponService.validateCoupon(
                request.getCode(), currentUser.getId(), request.getCourseId());

        return ResponseEntity.ok(ApiResponse.success(
                result.isValid() ? "Coupon validated successfully" : result.getMessage(),
                result));
    }
}
