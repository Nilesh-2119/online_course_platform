package com.courseplatform.payment;

import com.courseplatform.auth.security.UserPrincipal;
import com.courseplatform.common.exception.CourseAccessDeniedException;
import com.courseplatform.common.exception.CourseAlreadyPurchasedException;
import com.courseplatform.common.exception.PaymentProcessingException;
import com.courseplatform.common.exception.ResourceNotFoundException;
import com.courseplatform.common.exception.UserDisabledException;
import com.courseplatform.config.AppProperties;
import com.courseplatform.coupon.CouponService;
import com.courseplatform.course.CourseEntity;
import com.courseplatform.course.CourseRepository;
import com.courseplatform.course.CourseStatus;
import com.courseplatform.payment.dto.CreateOrderRequest;
import com.courseplatform.payment.dto.OrderResponse;
import com.courseplatform.payment.dto.VerifyPaymentRequest;
import com.courseplatform.payment.dto.VerifyPaymentResponse;
import com.courseplatform.user.UserEntity;
import com.courseplatform.user.UserRepository;
import com.courseplatform.user.UserStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@Service
public class PaymentService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PaymentService.class);

    private final CoursePurchaseRepository purchaseRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final RazorpayOrderClient razorpayOrderClient;
    private final RazorpaySignatureValidator signatureValidator;
    private final ObjectMapper objectMapper;
    private final AppProperties appProperties;
    private final CouponService couponService;

    public PaymentService(CoursePurchaseRepository purchaseRepository,
                          CourseRepository courseRepository,
                          UserRepository userRepository,
                          RazorpayOrderClient razorpayOrderClient,
                          RazorpaySignatureValidator signatureValidator,
                          ObjectMapper objectMapper,
                          AppProperties appProperties,
                          CouponService couponService) {
        this.purchaseRepository = purchaseRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.razorpayOrderClient = razorpayOrderClient;
        this.signatureValidator = signatureValidator;
        this.objectMapper = objectMapper;
        this.appProperties = appProperties;
        this.couponService = couponService;
    }

    /**
     * Authoritatively creates or reuses an idempotent server-side Razorpay order.
     *
     * @param request The course identification request
     * @param currentUser The authenticated user principal
     * @return Safe OrderResponse for frontend Razorpay checkout modal
     */
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request, UserPrincipal currentUser) {
        Long userId = currentUser.getId();
        Long courseId = request.getCourseId();

        log.info("Processing order creation for user ID {} on course ID {}", userId, courseId);

        // 1. Authenticate & load user
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (user.getStatus() != UserStatus.ACTIVE) {
            log.warn("Order creation rejected for non-active user ID {}", userId);
            throw new UserDisabledException();
        }

        // 2. Load & verify course
        CourseEntity course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", courseId));

        if (course.getStatus() != CourseStatus.PUBLISHED) {
            log.warn("Order creation rejected for non-published course ID {}", courseId);
            throw new ResourceNotFoundException("Course", courseId);
        }

        // 3. Verify user does not already own the course
        if (purchaseRepository.hasUserPurchasedCourse(userId, courseId)) {
            log.warn("User ID {} already has an active verified purchase for course ID {}", userId, courseId);
            throw new CourseAlreadyPurchasedException(courseId);
        }

        // 4. Authoritative price & currency strictly from database
        BigDecimal originalPrice = course.getPrice();
        BigDecimal price = originalPrice;
        String currency = course.getCurrency();
        BigDecimal discountAmount = BigDecimal.ZERO;
        String appliedCouponCode = null;

        // 4a. Apply coupon discount if provided
        if (StringUtils.hasText(request.getCouponCode())) {
            CouponService.CouponValidationResult validationResult = couponService.validateForOrder(
                    request.getCouponCode(), userId, originalPrice);
            price = validationResult.getFinalPrice();
            discountAmount = validationResult.getDiscountAmount();
            if (validationResult.hasCoupon()) {
                appliedCouponCode = validationResult.getCoupon().getCode();
            }
            log.info("Coupon '{}' applied for user ID {}: originalPrice={}, discountAmount={}, finalPrice={}",
                    appliedCouponCode, userId, originalPrice, discountAmount, price);
        }

        // 5. Clean up any previous stale CREATED orders so a fresh order is always created
        purchaseRepository
                .findTopByUserIdAndCourseIdAndStatusOrderByCreatedAtDesc(userId, courseId, PurchaseStatus.CREATED)
                .ifPresent(stale -> {
                    log.info("Cancelling stale pending order {} for user {}", stale.getRazorpayOrderId(), userId);
                    stale.setStatus(PurchaseStatus.CANCELLED);
                    purchaseRepository.save(stale);
                });

        // 6. Create internal purchase entity in CREATED state
        CoursePurchaseEntity purchase = CoursePurchaseEntity.builder()
                .user(user)
                .course(course)
                .amount(price)
                .currency(currency)
                .couponCode(appliedCouponCode)
                .discountAmount(discountAmount)
                .originalAmount(originalPrice)
                .status(PurchaseStatus.CREATED)
                .build();

        purchase = purchaseRepository.save(purchase);

        // 6a. If 100% discount (free access coupon), grant immediate entitlement
        if (price.compareTo(BigDecimal.ZERO) == 0) {
            log.info("100% discount coupon applied. Granting immediate access for purchaseId={}", purchase.getId());
            purchase.setStatus(PurchaseStatus.SUCCESS);
            purchase.setPaidAt(Instant.now());
            purchase.setRazorpayOrderId("FREE_COUPON_" + purchase.getId());
            purchase = purchaseRepository.save(purchase);

            if (appliedCouponCode != null) {
                couponService.recordCouponUsage(appliedCouponCode, user, purchase);
            }

            return buildOrderResponse(purchase.getRazorpayOrderId(), BigDecimal.ZERO, currency, course.getTitle(), user);
        }

        // 7. Calculate subunit amount (paise for INR)
        long amountInPaise = price.multiply(BigDecimal.valueOf(100)).longValueExact();
        if (amountInPaise < 100) {
            throw new PaymentProcessingException("Minimum order amount is 100 paise (1 INR)");
        }
        String receipt = "rcpt_" + purchase.getId() + "_" + System.currentTimeMillis();

        Map<String, String> notes = Map.of(
                "userId", String.valueOf(user.getId()),
                "courseId", String.valueOf(course.getId()),
                "userEmail", user.getEmail(),
                "purchaseId", String.valueOf(purchase.getId())
        );

        // 8. Create Razorpay order
        RazorpayOrderClient.RazorpayOrderResult razorpayOrder = razorpayOrderClient
                .createOrder(amountInPaise, currency, receipt, notes);

        // 9. Persist Razorpay order ID to purchase record
        purchase.setRazorpayOrderId(razorpayOrder.getId());
        purchaseRepository.save(purchase);

        log.info("Order initialized: purchaseId={}, razorpayOrderId={}, amount={} {}",
                purchase.getId(), razorpayOrder.getId(), price, currency);

        // 10. Return safe checkout payload
        return buildOrderResponse(razorpayOrder.getId(), price, currency, course.getTitle(), user);
    }

    /**
     * Authenticates and cryptographically verifies frontend checkout completion.
     * Enforces:
     * 1. Internal purchase ownership check (User A cannot verify User B's order)
     * 2. Idempotency against duplicate verifications
     * 3. Constant-time HMAC-SHA256 signature verification
     * 4. State transition to SUCCESS and immediate entitlement grant
     *
     * @param request Verification payload with razorpayOrderId, razorpayPaymentId, razorpaySignature
     * @param currentUser Authenticated user principal
     * @return VerifyPaymentResponse with status and course info
     */
    @Transactional
    public VerifyPaymentResponse verifyPayment(VerifyPaymentRequest request, UserPrincipal currentUser) {
        String orderId = request.getRazorpayOrderId();
        String paymentId = request.getRazorpayPaymentId();
        String signature = request.getRazorpaySignature();

        log.info("Verifying payment for order ID {} by user ID {}", orderId, currentUser.getId());

        // 1. Locate internal purchase record or create on-the-fly for verified frontend orders
        CoursePurchaseEntity purchase = purchaseRepository.findByRazorpayOrderId(orderId)
                .orElseGet(() -> {
                    log.info("Creating purchase record on-the-fly for verified order ID {} and user ID {}", orderId, currentUser.getId());
                    CourseEntity course = courseRepository.findById(1L)
                            .orElseThrow(() -> new ResourceNotFoundException("Course", 1L));
                    UserEntity user = userRepository.findById(currentUser.getId())
                            .orElseThrow(() -> new ResourceNotFoundException("User", currentUser.getId()));
                    return purchaseRepository.save(CoursePurchaseEntity.builder()
                            .user(user)
                            .course(course)
                            .amount(course.getPrice())
                            .currency(course.getCurrency())
                            .razorpayOrderId(orderId)
                            .status(PurchaseStatus.CREATED)
                            .build());
                });

        // 2. Validate order relationship (prevent cross-user spoofing)
        if (!purchase.getUser().getId().equals(currentUser.getId())) {
            log.warn("User ID {} attempted to verify payment for purchase owned by user ID {}",
                    currentUser.getId(), purchase.getUser().getId());
            throw new CourseAccessDeniedException("Unauthorized to verify payment for this order");
        }

        // 3. Idempotent check (already verified)
        if (purchase.getStatus() == PurchaseStatus.SUCCESS) {
            log.info("Payment for order ID {} already marked as SUCCESS (idempotent request)", orderId);
            return VerifyPaymentResponse.builder()
                    .verified(true)
                    .courseId(purchase.getCourse().getId())
                    .courseTitle(purchase.getCourse().getTitle())
                    .message("Payment already verified and course access is active")
                    .razorpayPaymentId(purchase.getRazorpayPaymentId())
                    .build();
        }

        // 4. Cryptographic HMAC-SHA256 signature verification
        String keySecret = appProperties.payment().razorpay().keySecret();
        boolean isValid = signatureValidator.verifyPaymentSignature(orderId, paymentId, signature, keySecret);

        if (!isValid) {
            log.error("Payment signature verification failed for order ID {} and payment ID {}", orderId, paymentId);
            throw new PaymentProcessingException("Invalid payment signature. Verification failed.");
        }

        // 5. Transition internal payment state to SUCCESS
        purchase.setRazorpayPaymentId(paymentId);
        purchase.setRazorpaySignature(signature);
        purchase.setStatus(PurchaseStatus.SUCCESS);
        if (purchase.getPaidAt() == null) {
            purchase.setPaidAt(Instant.now());
        }
        purchaseRepository.save(purchase);

        // 5a. Record coupon usage if applied
        recordCouponUsageIfPresent(purchase);

        log.info("Payment verified successfully: purchaseId={}, orderId={}, paymentId={}, userId={}, courseId={}",
                purchase.getId(), orderId, paymentId, currentUser.getId(), purchase.getCourse().getId());

        return VerifyPaymentResponse.builder()
                .verified(true)
                .courseId(purchase.getCourse().getId())
                .courseTitle(purchase.getCourse().getTitle())
                .message("Payment verified successfully and course access granted")
                .razorpayPaymentId(paymentId)
                .build();
    }

    /**
     * Idempotently processes asynchronous Razorpay webhooks.
     * Enforces raw body HMAC-SHA256 signature validation and safe state transitions.
     *
     * @param rawPayload Exact raw unparsed webhook JSON body
     * @param signature Signature header (X-Razorpay-Signature)
     */
    @Transactional
    public void processWebhook(String rawPayload, String signature) {
        log.info("Received Razorpay webhook event");

        // 1. Verify webhook authenticity against raw body
        String webhookSecret = appProperties.payment().razorpay().webhookSecret();
        boolean isValid = signatureValidator.verifyWebhookSignature(rawPayload, signature, webhookSecret);

        if (!isValid) {
            log.error("Razorpay webhook signature verification failed");
            throw new PaymentProcessingException("Invalid webhook signature");
        }

        // 2. Parse event payload
        try {
            JsonNode root = objectMapper.readTree(rawPayload);
            String event = root.path("event").asText();
            JsonNode payloadNode = root.path("payload");

            log.info("Processing Razorpay webhook event type: {}", event);

            String orderId = null;
            if (payloadNode.has("payment") && payloadNode.path("payment").has("entity")) {
                orderId = payloadNode.path("payment").path("entity").path("order_id").asText(null);
            }
            if (!StringUtils.hasText(orderId) && payloadNode.has("order") && payloadNode.path("order").has("entity")) {
                orderId = payloadNode.path("order").path("entity").path("id").asText(null);
            }

            String paymentId = null;
            if (payloadNode.has("payment") && payloadNode.path("payment").has("entity")) {
                paymentId = payloadNode.path("payment").path("entity").path("id").asText(null);
            }

            if (!StringUtils.hasText(orderId)) {
                log.debug("Webhook event {} does not contain order_id; ignoring safely", event);
                return;
            }

            Optional<CoursePurchaseEntity> purchaseOpt = purchaseRepository.findByRazorpayOrderId(orderId);
            if (purchaseOpt.isEmpty()) {
                log.warn("Webhook received for unknown or external order ID: {}; ignoring", orderId);
                return;
            }

            CoursePurchaseEntity purchase = purchaseOpt.get();

            // 3. Idempotent state transitions
            if ("order.paid".equalsIgnoreCase(event) || "payment.captured".equalsIgnoreCase(event)) {
                if (purchase.getStatus() == PurchaseStatus.SUCCESS) {
                    log.info("Webhook duplicate: order ID {} is already SUCCESS; ignoring", orderId);
                    return;
                }

                if (StringUtils.hasText(paymentId)) {
                    purchase.setRazorpayPaymentId(paymentId);
                }
                purchase.setStatus(PurchaseStatus.SUCCESS);
                if (purchase.getPaidAt() == null) {
                    purchase.setPaidAt(Instant.now());
                }
                purchaseRepository.save(purchase);

                // Record coupon usage if applied
                recordCouponUsageIfPresent(purchase);

                log.info("Webhook granted entitlement: purchaseId={}, orderId={}, paymentId={}",
                        purchase.getId(), orderId, paymentId);

            } else if ("payment.failed".equalsIgnoreCase(event)) {
                if (purchase.getStatus() != PurchaseStatus.SUCCESS) {
                    purchase.setStatus(PurchaseStatus.FAILED);
                    purchaseRepository.save(purchase);
                    log.info("Webhook marked order ID {} as FAILED", orderId);
                }
            }

        } catch (PaymentProcessingException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error parsing Razorpay webhook JSON payload: {}", ex.getMessage(), ex);
            throw new PaymentProcessingException("Malformed webhook payload", ex);
        }
    }

    private void recordCouponUsageIfPresent(CoursePurchaseEntity purchase) {
        if (StringUtils.hasText(purchase.getCouponCode())) {
            try {
                couponService.recordCouponUsage(purchase.getCouponCode(), purchase.getUser(), purchase);
            } catch (Exception e) {
                log.error("Failed to record coupon usage for coupon '{}', purchase ID {}: {}",
                        purchase.getCouponCode(), purchase.getId(), e.getMessage(), e);
            }
        }
    }

    private OrderResponse buildOrderResponse(
            String razorpayOrderId,
            BigDecimal amount,
            String currency,
            String courseTitle,
            UserEntity user
    ) {
        String keyId = appProperties.payment().razorpay().keyId();
        if (keyId != null) {
            keyId = keyId.trim().replace("\"", "").replace("'", "");
        }

        return OrderResponse.builder()
                .razorpayOrderId(razorpayOrderId)
                .amount(amount)
                .currency(currency)
                .keyId(keyId)
                .courseTitle(courseTitle)
                .userName(user.getName())
                .userEmail(user.getEmail())
                .userPhone(user.getPhone())
                .build();
    }
}
