package com.courseplatform.payment;

import com.courseplatform.auth.RefreshTokenRepository;
import com.courseplatform.auth.security.JwtTokenProvider;
import com.courseplatform.config.AppProperties;
import com.courseplatform.course.CourseEntity;
import com.courseplatform.course.CourseRepository;
import com.courseplatform.course.CourseStatus;
import com.courseplatform.payment.dto.CreateOrderRequest;
import com.courseplatform.payment.dto.VerifyPaymentRequest;
import com.courseplatform.user.UserEntity;
import com.courseplatform.user.UserRepository;
import com.courseplatform.user.UserRole;
import com.courseplatform.user.UserStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HexFormat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PaymentControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CoursePurchaseRepository purchaseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private AppProperties appProperties;

    @MockitoBean
    private RazorpayOrderClient razorpayOrderClient;

    private UserEntity studentUser;
    private UserEntity attackerUser;
    private String studentToken;
    private String attackerToken;
    private CourseEntity course;

    @BeforeEach
    void setUp() {
        purchaseRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();

        course = courseRepository.findById(1L).orElseThrow();

        studentUser = userRepository.save(UserEntity.builder()
                .name("Student Buyer")
                .email("buyer.student@example.com")
                .phone("+91 9876543210")
                .passwordHash(passwordEncoder.encode("SecretPass123!"))
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build());

        attackerUser = userRepository.save(UserEntity.builder()
                .name("Attacker User")
                .email("attacker@example.com")
                .phone("+91 9876543211")
                .passwordHash(passwordEncoder.encode("SecretPass123!"))
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build());

        studentToken = jwtTokenProvider.generateAccessToken(studentUser.getId(), studentUser.getEmail(), studentUser.getRole().name());
        attackerToken = jwtTokenProvider.generateAccessToken(attackerUser.getId(), attackerUser.getEmail(), attackerUser.getRole().name());

        when(razorpayOrderClient.createOrder(anyLong(), anyString(), anyString(), anyMap()))
                .thenReturn(RazorpayOrderClient.RazorpayOrderResult.builder()
                        .id("order_rzp_mock_123456")
                        .amount(400000L)
                        .currency("INR")
                        .receipt("rcpt_1_test")
                        .status("created")
                        .build());
    }

    // ==========================================
    // ORDER CREATION TESTS (Phase 8)
    // ==========================================

    @Test
    @DisplayName("1. Valid purchase: Creates order using database price (₹4,000) and returns safe checkout info")
    void validPurchase_createsOrderSuccessfully() throws Exception {
        CreateOrderRequest request = CreateOrderRequest.builder()
                .courseId(course.getId())
                .build();

        mockMvc.perform(post("/api/v1/payments/orders")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.razorpayOrderId").value("order_rzp_mock_123456"))
                .andExpect(jsonPath("$.data.amount").value(4000.00))
                .andExpect(jsonPath("$.data.currency").value("INR"))
                .andExpect(jsonPath("$.data.courseTitle").value("Enterprise Backend Architecture & Security"))
                .andExpect(jsonPath("$.data.userName").value("Student Buyer"))
                .andExpect(jsonPath("$.data.userEmail").value("buyer.student@example.com"))
                .andExpect(jsonPath("$.data.keySecret").doesNotExist())
                .andExpect(jsonPath("$.data.webhookSecret").doesNotExist());

        verify(razorpayOrderClient, times(1)).createOrder(eq(400000L), eq("INR"), anyString(), anyMap());
    }

    @Test
    @DisplayName("2. Invalid course ID returns 404 Not Found")
    void invalidCourseId_returns404() throws Exception {
        CreateOrderRequest request = CreateOrderRequest.builder()
                .courseId(99999L)
                .build();

        mockMvc.perform(post("/api/v1/payments/orders")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    @DisplayName("3. Unpublished (Draft) course returns 404 Not Found")
    void unpublishedCourse_returns404() throws Exception {
        CourseEntity draftCourse = courseRepository.save(CourseEntity.builder()
                .title("Unpublished Secret Course")
                .description("In development")
                .price(new BigDecimal("9999.00"))
                .currency("INR")
                .status(CourseStatus.DRAFT)
                .build());

        CreateOrderRequest request = CreateOrderRequest.builder()
                .courseId(draftCourse.getId())
                .build();

        mockMvc.perform(post("/api/v1/payments/orders")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    @DisplayName("4. Already purchased course returns 409 Conflict")
    void alreadyPurchasedCourse_returnsConflict() throws Exception {
        purchaseRepository.save(CoursePurchaseEntity.builder()
                .user(studentUser)
                .course(course)
                .amount(course.getPrice())
                .currency("INR")
                .status(PurchaseStatus.SUCCESS)
                .razorpayOrderId("order_existing_done")
                .paidAt(Instant.now())
                .build());

        CreateOrderRequest request = CreateOrderRequest.builder()
                .courseId(course.getId())
                .build();

        mockMvc.perform(post("/api/v1/payments/orders")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("already own")));
    }

    @Test
    @DisplayName("5. Idempotent duplicate requests: Reuses active pending order instead of multiple gateway calls")
    void duplicateRequests_areIdempotent() throws Exception {
        CreateOrderRequest request = CreateOrderRequest.builder()
                .courseId(course.getId())
                .build();

        mockMvc.perform(post("/api/v1/payments/orders")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.razorpayOrderId").value("order_rzp_mock_123456"));

        mockMvc.perform(post("/api/v1/payments/orders")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.razorpayOrderId").value("order_rzp_mock_123456"));

        verify(razorpayOrderClient, times(1)).createOrder(anyLong(), anyString(), anyString(), anyMap());
    }

    @Test
    @DisplayName("6. Price tampering immunity: Client cannot override authoritative database price")
    void priceTampering_isIgnored() throws Exception {
        String maliciousPayload = "{\"courseId\": 1, \"amount\": 1.00, \"currency\": \"USD\"}";

        mockMvc.perform(post("/api/v1/payments/orders")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(maliciousPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.amount").value(4000.00))
                .andExpect(jsonPath("$.data.currency").value("INR"));

        verify(razorpayOrderClient, times(1)).createOrder(eq(400000L), eq("INR"), anyString(), anyMap());
    }

    // ==========================================
    // PAYMENT VERIFICATION TESTS (Phase 9)
    // ==========================================

    @Test
    @DisplayName("7. Valid payment signature grants course entitlement and transitions status to SUCCESS")
    void validPaymentVerification_grantsEntitlement() throws Exception {
        String orderId = "order_rzp_verify_valid";
        String paymentId = "pay_rzp_verify_valid";

        // Create pending purchase
        CoursePurchaseEntity purchase = purchaseRepository.save(CoursePurchaseEntity.builder()
                .user(studentUser)
                .course(course)
                .amount(course.getPrice())
                .currency("INR")
                .status(PurchaseStatus.CREATED)
                .razorpayOrderId(orderId)
                .build());

        // Compute valid signature
        String keySecret = appProperties.payment().razorpay().keySecret();
        String validSignature = calculateHmacSha256(orderId + "|" + paymentId, keySecret);

        VerifyPaymentRequest request = VerifyPaymentRequest.builder()
                .razorpayOrderId(orderId)
                .razorpayPaymentId(paymentId)
                .razorpaySignature(validSignature)
                .build();

        mockMvc.perform(post("/api/v1/payments/verify")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.verified").value(true))
                .andExpect(jsonPath("$.data.courseId").value(course.getId()))
                .andExpect(jsonPath("$.data.razorpayPaymentId").value(paymentId));

        // Verify database state
        CoursePurchaseEntity updated = purchaseRepository.findById(purchase.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(PurchaseStatus.SUCCESS);
        assertThat(updated.getRazorpayPaymentId()).isEqualTo(paymentId);
        assertThat(updated.getPaidAt()).isNotNull();

        // Verify entitlement
        assertThat(purchaseRepository.hasUserPurchasedCourse(studentUser.getId(), course.getId())).isTrue();
    }

    @Test
    @DisplayName("8. Invalid payment signature is rejected with 400 Bad Request and does not grant entitlement")
    void invalidSignature_isRejected() throws Exception {
        String orderId = "order_rzp_verify_invalid";
        String paymentId = "pay_rzp_verify_invalid";

        purchaseRepository.save(CoursePurchaseEntity.builder()
                .user(studentUser)
                .course(course)
                .amount(course.getPrice())
                .currency("INR")
                .status(PurchaseStatus.CREATED)
                .razorpayOrderId(orderId)
                .build());

        VerifyPaymentRequest request = VerifyPaymentRequest.builder()
                .razorpayOrderId(orderId)
                .razorpayPaymentId(paymentId)
                .razorpaySignature("forged_invalid_signature_hash")
                .build();

        mockMvc.perform(post("/api/v1/payments/verify")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PAYMENT_FAILED"));

        // Entitlement must NOT be granted
        assertThat(purchaseRepository.hasUserPurchasedCourse(studentUser.getId(), course.getId())).isFalse();
    }

    @Test
    @DisplayName("9. Cross-user verification attempt (User B verifying User A's order) is rejected with 403")
    void crossUserVerification_isDenied() throws Exception {
        String orderId = "order_rzp_cross_user";
        String paymentId = "pay_rzp_cross_user";

        purchaseRepository.save(CoursePurchaseEntity.builder()
                .user(studentUser)
                .course(course)
                .amount(course.getPrice())
                .currency("INR")
                .status(PurchaseStatus.CREATED)
                .razorpayOrderId(orderId)
                .build());

        String keySecret = appProperties.payment().razorpay().keySecret();
        String validSignature = calculateHmacSha256(orderId + "|" + paymentId, keySecret);

        VerifyPaymentRequest request = VerifyPaymentRequest.builder()
                .razorpayOrderId(orderId)
                .razorpayPaymentId(paymentId)
                .razorpaySignature(validSignature)
                .build();

        // Attacker attempts to verify Student's order
        mockMvc.perform(post("/api/v1/payments/verify")
                        .header("Authorization", "Bearer " + attackerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("COURSE_ACCESS_DENIED"));
    }

    @Test
    @DisplayName("10. Duplicate verification is idempotent and returns 200 OK without error")
    void duplicateVerification_isIdempotent() throws Exception {
        String orderId = "order_rzp_duplicate_verify";
        String paymentId = "pay_rzp_duplicate_verify";

        purchaseRepository.save(CoursePurchaseEntity.builder()
                .user(studentUser)
                .course(course)
                .amount(course.getPrice())
                .currency("INR")
                .status(PurchaseStatus.SUCCESS)
                .razorpayOrderId(orderId)
                .razorpayPaymentId(paymentId)
                .paidAt(Instant.now())
                .build());

        VerifyPaymentRequest request = VerifyPaymentRequest.builder()
                .razorpayOrderId(orderId)
                .razorpayPaymentId(paymentId)
                .razorpaySignature("any_sig")
                .build();

        mockMvc.perform(post("/api/v1/payments/verify")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.verified").value(true));
    }

    // ==========================================
    // WEBHOOK TESTS (Phase 9)
    // ==========================================

    @Test
    @DisplayName("11. Valid Razorpay webhook (order.paid) verifies signature and grants course entitlement")
    void validWebhook_orderPaid_grantsEntitlement() throws Exception {
        String orderId = "order_rzp_webhook_paid";
        String paymentId = "pay_rzp_webhook_paid";

        CoursePurchaseEntity purchase = purchaseRepository.save(CoursePurchaseEntity.builder()
                .user(studentUser)
                .course(course)
                .amount(course.getPrice())
                .currency("INR")
                .status(PurchaseStatus.CREATED)
                .razorpayOrderId(orderId)
                .build());

        String webhookJson = """
                {
                  "event": "order.paid",
                  "payload": {
                    "order": {
                      "entity": {
                        "id": "%s",
                        "amount": 400000,
                        "status": "paid"
                      }
                    },
                    "payment": {
                      "entity": {
                        "id": "%s",
                        "order_id": "%s",
                        "amount": 400000,
                        "status": "captured"
                      }
                    }
                  }
                }
                """.formatted(orderId, paymentId, orderId);

        String webhookSecret = appProperties.payment().razorpay().webhookSecret();
        String validWebhookSignature = calculateHmacSha256(webhookJson, webhookSecret);

        mockMvc.perform(post("/api/v1/payments/webhook")
                        .header("X-Razorpay-Signature", validWebhookSignature)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(webhookJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"));

        CoursePurchaseEntity updated = purchaseRepository.findById(purchase.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(PurchaseStatus.SUCCESS);
        assertThat(updated.getRazorpayPaymentId()).isEqualTo(paymentId);
        assertThat(purchaseRepository.hasUserPurchasedCourse(studentUser.getId(), course.getId())).isTrue();
    }

    @Test
    @DisplayName("12. Invalid webhook signature is rejected with 400 Bad Request")
    void invalidWebhookSignature_isRejected() throws Exception {
        String webhookJson = "{\"event\": \"payment.captured\"}";

        mockMvc.perform(post("/api/v1/payments/webhook")
                        .header("X-Razorpay-Signature", "forged_webhook_signature")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(webhookJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PAYMENT_FAILED"));
    }

    @Test
    @DisplayName("13. Webhook retry / duplicate delivery is idempotent")
    void webhookDuplicateDelivery_isIdempotent() throws Exception {
        String orderId = "order_rzp_webhook_duplicate";
        String paymentId = "pay_rzp_webhook_duplicate";

        Instant initialPaidAt = Instant.now().minusSeconds(100).truncatedTo(java.time.temporal.ChronoUnit.MILLIS);

        CoursePurchaseEntity purchase = purchaseRepository.save(CoursePurchaseEntity.builder()
                .user(studentUser)
                .course(course)
                .amount(course.getPrice())
                .currency("INR")
                .status(PurchaseStatus.SUCCESS)
                .razorpayOrderId(orderId)
                .razorpayPaymentId(paymentId)
                .paidAt(initialPaidAt)
                .build());

        String webhookJson = """
                {
                  "event": "payment.captured",
                  "payload": {
                    "payment": {
                      "entity": {
                        "id": "%s",
                        "order_id": "%s"
                      }
                    }
                  }
                }
                """.formatted(paymentId, orderId);

        String webhookSecret = appProperties.payment().razorpay().webhookSecret();
        String validWebhookSignature = calculateHmacSha256(webhookJson, webhookSecret);

        // Send duplicate webhook
        mockMvc.perform(post("/api/v1/payments/webhook")
                        .header("X-Razorpay-Signature", validWebhookSignature)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(webhookJson))
                .andExpect(status().isOk());

        // Status and original paidAt timestamp preserved
        CoursePurchaseEntity updated = purchaseRepository.findById(purchase.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(PurchaseStatus.SUCCESS);
        assertThat(updated.getPaidAt()).isEqualTo(initialPaidAt);
    }

    @Test
    @DisplayName("14. Webhook payment.failed transitions order status to FAILED")
    void webhookPaymentFailed_transitionsToFailed() throws Exception {
        String orderId = "order_rzp_webhook_failed";

        CoursePurchaseEntity purchase = purchaseRepository.save(CoursePurchaseEntity.builder()
                .user(studentUser)
                .course(course)
                .amount(course.getPrice())
                .currency("INR")
                .status(PurchaseStatus.CREATED)
                .razorpayOrderId(orderId)
                .build());

        String webhookJson = """
                {
                  "event": "payment.failed",
                  "payload": {
                    "payment": {
                      "entity": {
                        "id": "pay_failed_123",
                        "order_id": "%s"
                      }
                    }
                  }
                }
                """.formatted(orderId);

        String webhookSecret = appProperties.payment().razorpay().webhookSecret();
        String validWebhookSignature = calculateHmacSha256(webhookJson, webhookSecret);

        mockMvc.perform(post("/api/v1/payments/webhook")
                        .header("X-Razorpay-Signature", validWebhookSignature)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(webhookJson))
                .andExpect(status().isOk());

        CoursePurchaseEntity updated = purchaseRepository.findById(purchase.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(PurchaseStatus.FAILED);
        assertThat(purchaseRepository.hasUserPurchasedCourse(studentUser.getId(), course.getId())).isFalse();
    }

    // Helper method for computing HMAC-SHA256 in test assertions
    private String calculateHmacSha256(String data, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(hash);
    }
}
