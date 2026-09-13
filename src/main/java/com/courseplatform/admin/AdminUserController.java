package com.courseplatform.admin;

import com.courseplatform.admin.dto.AdminUserDetailDto;
import com.courseplatform.admin.dto.AdminUserDto;
import com.courseplatform.admin.dto.CreateAdminUserRequest;
import com.courseplatform.auth.RefreshTokenRepository;
import com.courseplatform.auth.oauth.UserAuthProviderRepository;
import com.courseplatform.auth.otp.VerificationOtpRepository;
import com.courseplatform.auth.security.UserPrincipal;
import com.courseplatform.common.ApiResponse;
import com.courseplatform.common.exception.BadRequestException;
import com.courseplatform.common.exception.ResourceNotFoundException;
import com.courseplatform.common.util.PhoneNumberNormalizer;
import com.courseplatform.course.CourseEntity;
import com.courseplatform.course.CourseRepository;
import com.courseplatform.course.CourseStatus;
import com.courseplatform.payment.CoursePurchaseEntity;
import com.courseplatform.payment.CoursePurchaseRepository;
import com.courseplatform.payment.PurchaseStatus;
import com.courseplatform.progress.VideoProgressRepository;
import com.courseplatform.user.UserEntity;
import com.courseplatform.user.UserRepository;
import com.courseplatform.user.UserRole;
import com.courseplatform.user.UserStatus;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/users")
public class AdminUserController {

    private static final Logger log = LoggerFactory.getLogger(AdminUserController.class);

    private final UserRepository userRepository;
    private final CoursePurchaseRepository purchaseRepository;
    private final CourseRepository courseRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final VerificationOtpRepository verificationOtpRepository;
    private final UserAuthProviderRepository userAuthProviderRepository;
    private final VideoProgressRepository videoProgressRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserController(UserRepository userRepository,
                               CoursePurchaseRepository purchaseRepository,
                               CourseRepository courseRepository,
                               RefreshTokenRepository refreshTokenRepository,
                               VerificationOtpRepository verificationOtpRepository,
                               UserAuthProviderRepository userAuthProviderRepository,
                               VideoProgressRepository videoProgressRepository,
                               PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.purchaseRepository = purchaseRepository;
        this.courseRepository = courseRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.verificationOtpRepository = verificationOtpRepository;
        this.userAuthProviderRepository = userAuthProviderRepository;
        this.videoProgressRepository = videoProgressRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private void verifyAdminAccess(UserPrincipal currentUser) {
        if (currentUser == null || (currentUser.getRole() != UserRole.ADMIN && !"adfixstudio25@gmail.com".equalsIgnoreCase(currentUser.getEmail()))) {
            log.warn("Unauthorized access attempt by user {}", currentUser != null ? currentUser.getEmail() : "ANONYMOUS");
            throw new AccessDeniedException("Administrator privileges are required to access this resource.");
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<AdminUserDto>>> getAllUsers(
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        verifyAdminAccess(currentUser);

        List<UserEntity> users = userRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        List<CoursePurchaseEntity> allPurchases = purchaseRepository.findAll();
        
        Set<Long> enrolledUserIds = new HashSet<>();
        for (CoursePurchaseEntity p : allPurchases) {
            if (p.getStatus() == PurchaseStatus.SUCCESS && p.getUser() != null) {
                enrolledUserIds.add(p.getUser().getId());
            }
        }

        List<AdminUserDto> response = new ArrayList<>(users.size());
        for (UserEntity u : users) {
            boolean isEnrolled = enrolledUserIds.contains(u.getId());
            response.add(new AdminUserDto(
                    u.getId(),
                    u.getName(),
                    u.getEmail(),
                    u.getPhone(),
                    u.getRole() != null ? u.getRole().name() : "USER",
                    u.getStatus() != null ? u.getStatus().name() : "ACTIVE",
                    isEnrolled ? "paid" : "free",
                    isEnrolled,
                    u.getCreatedAt(),
                    u.getLastLoginAt() != null ? u.getLastLoginAt() : u.getUpdatedAt()
            ));
        }

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<ApiResponse<AdminUserDto>> createUser(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody CreateAdminUserRequest request
    ) {
        verifyAdminAccess(currentUser);

        String normalizedEmail = request.getEmail().trim().toLowerCase();
        String rawPhone = request.getPhone() != null ? request.getPhone().trim() : "";
        String normalizedPhone = StringUtils.hasText(rawPhone) ? PhoneNumberNormalizer.normalize(rawPhone) : null;

        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new BadRequestException("An account with email '" + normalizedEmail + "' already exists.");
        }

        if (StringUtils.hasText(normalizedPhone) && userRepository.existsByPhone(normalizedPhone)) {
            throw new BadRequestException("An account with phone number '" + normalizedPhone + "' already exists.");
        }

        UserRole role = UserRole.USER;
        if (StringUtils.hasText(request.getRole())) {
            try {
                role = UserRole.valueOf(request.getRole().trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {
                role = UserRole.USER;
            }
        }

        UserEntity user = UserEntity.builder()
                .name(request.getName().trim())
                .email(normalizedEmail)
                .phone(normalizedPhone)
                .passwordHash(passwordEncoder.encode(request.getPassword().trim()))
                .role(role)
                .status(UserStatus.ACTIVE) // Created by admin: ACTIVE immediately
                .emailVerified(true)      // No OTP verification required for admin created users
                .phoneVerified(true)      // No OTP verification required
                .build();

        UserEntity savedUser = userRepository.save(user);

        boolean grantAccess = request.getGrantCourseAccess() == null || Boolean.TRUE.equals(request.getGrantCourseAccess());
        boolean hasAccess = false;

        if (grantAccess) {
            List<CourseEntity> courses = courseRepository.findByStatus(CourseStatus.PUBLISHED);
            CourseEntity targetCourse = !courses.isEmpty() ? courses.get(0) : courseRepository.findAll().stream().findFirst().orElse(null);
            if (targetCourse != null) {
                String uniqueSuffix = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
                CoursePurchaseEntity purchase = new CoursePurchaseEntity();
                purchase.setUser(savedUser);
                purchase.setCourse(targetCourse);
                purchase.setAmount(BigDecimal.ZERO);
                purchase.setOriginalAmount(targetCourse.getPrice() != null ? targetCourse.getPrice() : BigDecimal.ZERO);
                purchase.setDiscountAmount(BigDecimal.ZERO);
                purchase.setCurrency("INR");
                purchase.setStatus(PurchaseStatus.SUCCESS);
                purchase.setPaidAt(Instant.now());
                purchase.setRazorpayOrderId("ADMIN_CREATED_" + uniqueSuffix);
                purchase.setRazorpayPaymentId("ADMIN_MANUAL_" + uniqueSuffix);
                purchaseRepository.save(purchase);
                hasAccess = true;
            }
        }

        log.info("Admin {} created user ID: {} ({}) with ACTIVE status and instant course access={}",
                currentUser.getEmail(), savedUser.getId(), savedUser.getEmail(), hasAccess);

        AdminUserDto dto = new AdminUserDto(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getPhone(),
                savedUser.getRole() != null ? savedUser.getRole().name() : "USER",
                savedUser.getStatus() != null ? savedUser.getStatus().name() : "ACTIVE",
                hasAccess ? "paid" : "free",
                hasAccess,
                savedUser.getCreatedAt(),
                savedUser.getCreatedAt()
        );

        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<AdminUserDetailDto>> getUserDetail(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable("id") Long id
    ) {
        verifyAdminAccess(currentUser);

        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        List<CoursePurchaseEntity> userPurchases = purchaseRepository.findByUserIdWithCourseOrderByCreatedAtDesc(id);
        boolean hasAccess = userPurchases.stream().anyMatch(p -> p.getStatus() == PurchaseStatus.SUCCESS);

        String defaultCourseTitle = "AdFix High-Performing Ads Course";
        String courseTitle = defaultCourseTitle;
        List<AdminUserDetailDto.UserPurchaseDto> purchaseDtos = new ArrayList<>();
        for (CoursePurchaseEntity p : userPurchases) {
            String itemCourseTitle = defaultCourseTitle;
            try {
                if (p.getCourse() != null && p.getCourse().getTitle() != null) {
                    itemCourseTitle = p.getCourse().getTitle();
                    courseTitle = itemCourseTitle;
                }
            } catch (Exception e) {
                log.warn("Could not resolve course title for purchase ID {}: {}", p.getId(), e.getMessage());
            }
            purchaseDtos.add(new AdminUserDetailDto.UserPurchaseDto(
                    p.getId(),
                    itemCourseTitle,
                    p.getAmount(),
                    p.getCurrency(),
                    p.getStatus() != null ? p.getStatus().name() : "CREATED",
                    p.getRazorpayOrderId(),
                    p.getRazorpayPaymentId(),
                    p.getPaidAt(),
                    p.getCreatedAt()
            ));
        }

        AdminUserDetailDto detail = new AdminUserDetailDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole() != null ? user.getRole().name() : "USER",
                user.getStatus() != null ? user.getStatus().name() : "ACTIVE",
                hasAccess ? "paid" : "free",
                hasAccess,
                user.getCreatedAt(),
                user.getLastLoginAt() != null ? user.getLastLoginAt() : user.getUpdatedAt(),
                courseTitle,
                purchaseDtos
        );

        return ResponseEntity.ok(ApiResponse.success(detail));
    }

    @PostMapping("/{id}/revoke-access")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<ApiResponse<Map<String, Object>>> revokeCourseAccess(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable("id") Long id
    ) {
        verifyAdminAccess(currentUser);

        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        List<CoursePurchaseEntity> purchases = purchaseRepository.findByUserIdOrderByCreatedAtDesc(id);
        int revokedCount = 0;
        for (CoursePurchaseEntity p : purchases) {
            if (p.getStatus() == PurchaseStatus.SUCCESS) {
                p.setStatus(PurchaseStatus.CANCELLED);
                purchaseRepository.save(p);
                revokedCount++;
            }
        }

        log.info("Admin {} revoked course access for user ID {} ({} purchases updated)",
                currentUser.getEmail(), user.getId(), revokedCount);

        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "success", true,
                "message", "Course access revoked successfully.",
                "access", false
        )));
    }

    @PostMapping("/{id}/grant-access")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<ApiResponse<Map<String, Object>>> grantCourseAccess(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable("id") Long id
    ) {
        verifyAdminAccess(currentUser);

        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        List<CourseEntity> courses = courseRepository.findByStatus(CourseStatus.PUBLISHED);
        CourseEntity targetCourse = !courses.isEmpty() ? courses.get(0) : courseRepository.findAll().stream().findFirst().orElse(null);
        if (targetCourse == null) {
            throw new BadRequestException("No active course found in the platform to grant access.");
        }

        List<CoursePurchaseEntity> purchases = purchaseRepository.findByUserIdOrderByCreatedAtDesc(id);
        CoursePurchaseEntity existingPurchase = purchases.stream()
                .filter(p -> p.getCourse() != null && p.getCourse().getId().equals(targetCourse.getId()))
                .findFirst()
                .orElse(purchases.isEmpty() ? null : purchases.get(0));

        String uniqueSuffix = UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        if (existingPurchase != null) {
            existingPurchase.setStatus(PurchaseStatus.SUCCESS);
            if (existingPurchase.getPaidAt() == null) {
                existingPurchase.setPaidAt(Instant.now());
            }
            if (existingPurchase.getCourse() == null) {
                existingPurchase.setCourse(targetCourse);
            }
            if (existingPurchase.getRazorpayPaymentId() == null ||
                "MANUAL_GRANT".equals(existingPurchase.getRazorpayPaymentId()) ||
                "ADMIN_MANUAL_ENROLL".equals(existingPurchase.getRazorpayPaymentId())) {
                existingPurchase.setRazorpayPaymentId("MANUAL_GRANT_" + uniqueSuffix);
            }
            if (existingPurchase.getRazorpayOrderId() == null || existingPurchase.getRazorpayOrderId().isBlank()) {
                existingPurchase.setRazorpayOrderId("ADMIN_GRANT_" + uniqueSuffix);
            }
            purchaseRepository.save(existingPurchase);
        } else {
            CoursePurchaseEntity granted = new CoursePurchaseEntity();
            granted.setUser(user);
            granted.setCourse(targetCourse);
            granted.setAmount(BigDecimal.ZERO);
            granted.setOriginalAmount(targetCourse.getPrice() != null ? targetCourse.getPrice() : BigDecimal.ZERO);
            granted.setDiscountAmount(BigDecimal.ZERO);
            granted.setCurrency("INR");
            granted.setStatus(PurchaseStatus.SUCCESS);
            granted.setPaidAt(Instant.now());
            granted.setRazorpayOrderId("ADMIN_GRANT_" + uniqueSuffix);
            granted.setRazorpayPaymentId("MANUAL_GRANT_" + uniqueSuffix);
            purchaseRepository.save(granted);
        }

        if (user.getStatus() != UserStatus.ACTIVE || !user.isEmailVerified()) {
            user.setStatus(UserStatus.ACTIVE);
            user.setEmailVerified(true);
            userRepository.save(user);
        }

        log.info("Admin {} granted full course access to user ID {} ({})",
                currentUser.getEmail(), user.getId(), user.getEmail());

        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "success", true,
                "message", "Course access granted successfully.",
                "access", true
        )));
    }

    @PostMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateUserStatus(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable("id") Long id,
            @RequestBody Map<String, String> body
    ) {
        verifyAdminAccess(currentUser);

        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if ("adfixstudio25@gmail.com".equalsIgnoreCase(user.getEmail())) {
            throw new BadRequestException("The primary administrator account status cannot be altered.");
        }

        String targetStatus = body.getOrDefault("status", "ACTIVE").trim().toUpperCase();
        UserStatus newStatus;
        try {
            newStatus = UserStatus.valueOf(targetStatus);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid user status: " + targetStatus);
        }

        user.setStatus(newStatus);
        userRepository.save(user);

        if (newStatus == UserStatus.DISABLED || newStatus == UserStatus.SUSPENDED) {
            refreshTokenRepository.revokeAllUserTokens(id);
        }

        log.info("Admin {} updated user ID {} status to {}", currentUser.getEmail(), id, newStatus);

        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "success", true,
                "status", newStatus.name(),
                "message", "User status updated to " + newStatus.name()
        )));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteUser(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable("id") Long id
    ) {
        verifyAdminAccess(currentUser);

        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if ("adfixstudio25@gmail.com".equalsIgnoreCase(user.getEmail()) || (currentUser.getId() != null && currentUser.getId().equals(id))) {
            throw new BadRequestException("Primary Administrator account cannot be deleted.");
        }

        // Clean up all related child entities to preserve database relational integrity
        refreshTokenRepository.deleteByUserId(id);
        verificationOtpRepository.deleteByUserId(id);
        userAuthProviderRepository.deleteByUserId(id);
        videoProgressRepository.deleteByUserId(id);
        purchaseRepository.deleteByUserId(id);

        userRepository.delete(user);

        log.info("Admin {} permanently deleted user ID: {} ({})", currentUser.getEmail(), id, user.getEmail());

        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "success", true,
                "message", "User deleted successfully."
        )));
    }
}
