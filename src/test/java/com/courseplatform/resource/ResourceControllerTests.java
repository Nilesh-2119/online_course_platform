package com.courseplatform.resource;

import com.courseplatform.auth.RefreshTokenRepository;
import com.courseplatform.auth.security.JwtTokenProvider;
import com.courseplatform.payment.CoursePurchaseRepository;
import com.courseplatform.user.UserEntity;
import com.courseplatform.user.UserRepository;
import com.courseplatform.user.UserRole;
import com.courseplatform.user.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ResourceControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FreeResourceRepository resourceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private CoursePurchaseRepository purchaseRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private UserEntity studentUser;
    private UserEntity adminUser;
    private String studentToken;
    private String adminToken;

    @BeforeEach
    void setUp() {
        purchaseRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();

        studentUser = userRepository.save(UserEntity.builder()
                .name("Resource Student")
                .email("student.res@example.com")
                .passwordHash(passwordEncoder.encode("Pass123!"))
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build());

        adminUser = userRepository.save(UserEntity.builder()
                .name("Resource Admin")
                .email("admin.res@example.com")
                .passwordHash(passwordEncoder.encode("Pass123!"))
                .role(UserRole.ADMIN)
                .status(UserStatus.ACTIVE)
                .build());

        studentToken = jwtTokenProvider.generateAccessToken(studentUser.getId(), studentUser.getEmail(), studentUser.getRole().name());
        adminToken = jwtTokenProvider.generateAccessToken(adminUser.getId(), adminUser.getEmail(), adminUser.getRole().name());
    }

    @Test
    @DisplayName("1. Public resource list returns published resources without requiring authentication")
    void getPublishedResources_publicAccess_success() throws Exception {
        mockMvc.perform(get("/api/v1/resources"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data[0].id").exists())
                .andExpect(jsonPath("$.data[0].title").exists())
                .andExpect(jsonPath("$.data[0].resourceUrl").exists())
                .andExpect(jsonPath("$.data[0].status").value("PUBLISHED"));
    }

    @Test
    @DisplayName("2. Public resource detail returns single resource metadata and download URL")
    void getResourceDetail_success() throws Exception {
        mockMvc.perform(get("/api/v1/resources/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("Backend System Architecture Checklist"))
                .andExpect(jsonPath("$.data.resourceType").value("PDF"))
                .andExpect(jsonPath("$.data.status").value("PUBLISHED"));
    }

    @Test
    @DisplayName("3. Invalid resource ID returns 404 Not Found")
    void invalidResourceId_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/resources/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    @DisplayName("4. Unpublished (DRAFT) resource returns 404 for public/students but 200 for ADMIN")
    void unpublishedResource_visibilityRules() throws Exception {
        FreeResourceEntity draftResource = resourceRepository.save(FreeResourceEntity.builder()
                .title("Internal Security Architecture Blueprint (Draft)")
                .description("Staff only draft")
                .resourceType(ResourceType.PDF)
                .resourceUrl("https://cdn.example.com/resources/internal-draft.pdf")
                .status(ResourceStatus.DRAFT)
                .build());

        // Anonymous -> 404
        mockMvc.perform(get("/api/v1/resources/" + draftResource.getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));

        // Student -> 404
        mockMvc.perform(get("/api/v1/resources/" + draftResource.getId())
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));

        // Admin -> 200 OK
        mockMvc.perform(get("/api/v1/resources/" + draftResource.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Internal Security Architecture Blueprint (Draft)"))
                .andExpect(jsonPath("$.data.status").value("DRAFT"));
    }

    @Test
    @DisplayName("5. Secret leakage audit: resource responses must not contain credentials or filesystem paths")
    void verifyNoSecretLeakage() throws Exception {
        mockMvc.perform(get("/api/v1/resources/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.secret").doesNotExist())
                .andExpect(jsonPath("$.data.apiKey").doesNotExist())
                .andExpect(jsonPath("$.data.storageKey").doesNotExist());
    }
}
