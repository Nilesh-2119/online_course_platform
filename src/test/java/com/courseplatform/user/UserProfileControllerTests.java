package com.courseplatform.user;

import com.courseplatform.auth.RefreshTokenRepository;
import com.courseplatform.auth.security.JwtTokenProvider;
import com.courseplatform.user.dto.UpdateProfileRequest;
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
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
public class UserProfileControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    private UserEntity userA;
    private UserEntity userB;
    private String tokenA;
    private String tokenB;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();

        userA = userRepository.save(UserEntity.builder()
                .name("User Alpha")
                .email("alpha@example.com")
                .phone("+919111111111")
                .passwordHash(passwordEncoder.encode("SecretPassA123!"))
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build());

        userB = userRepository.save(UserEntity.builder()
                .name("User Beta")
                .email("beta@example.com")
                .phone("+919222222222")
                .passwordHash(passwordEncoder.encode("SecretPassB123!"))
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build());

        tokenA = jwtTokenProvider.generateAccessToken(userA.getId(), userA.getEmail(), userA.getRole().name());
        tokenB = jwtTokenProvider.generateAccessToken(userB.getId(), userB.getEmail(), userB.getRole().name());
    }

    @Test
    @DisplayName("GET /api/v1/users/me returns authenticated user's own profile without secrets")
    void getOwnProfile_success() throws Exception {
        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(userA.getId()))
                .andExpect(jsonPath("$.data.name").value("User Alpha"))
                .andExpect(jsonPath("$.data.email").value("alpha@example.com"))
                .andExpect(jsonPath("$.data.phone").value("+919111111111"))
                // Ensure sensitive security fields are never exposed
                .andExpect(jsonPath("$.data.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.data.password").doesNotExist());
    }

    @Test
    @DisplayName("GET and PUT /api/v1/users/me without authentication return 401 Unauthorized")
    void unauthenticatedAccess_isRejected() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));

        mockMvc.perform(put("/api/v1/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(UpdateProfileRequest.builder().name("New Name").build())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("PUT /api/v1/users/me successfully updates legitimate profile fields")
    void updateProfile_success() throws Exception {
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .name("Alpha Updated")
                .phone("+919999999999")
                .build();

        mockMvc.perform(put("/api/v1/users/me")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Alpha Updated"))
                .andExpect(jsonPath("$.data.phone").value("+919999999999"));

        // Verify in database
        UserEntity updatedInDb = userRepository.findById(userA.getId()).orElseThrow();
        assertThat(updatedInDb.getName()).isEqualTo("Alpha Updated");
        assertThat(updatedInDb.getPhone()).isEqualTo("+919999999999");
    }

    @Test
    @DisplayName("PUT /api/v1/users/me rejects phone number that belongs to another user")
    void updateProfile_duplicatePhone_rejected() throws Exception {
        // User B has phone +919222222222, User A tries to claim it
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .name("Alpha Claiming Beta Phone")
                .phone("+91 92222 22222")
                .build();

        mockMvc.perform(put("/api/v1/users/me")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("phone number")));
    }

    @Test
    @DisplayName("User A updating profile does not affect User B (Cross-user isolation)")
    void crossUserManipulation_isPrevented() throws Exception {
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .name("Alpha Changed")
                .phone("+919000000000")
                .build();

        mockMvc.perform(put("/api/v1/users/me")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Verify User B is completely untouched
        UserEntity userBInDb = userRepository.findById(userB.getId()).orElseThrow();
        assertThat(userBInDb.getName()).isEqualTo("User Beta");
        assertThat(userBInDb.getPhone()).isEqualTo("+919222222222");
    }

    @Test
    @DisplayName("Role or status modification attempts via profile update payload are ignored")
    void roleAndStatusEscalationAttempt_isIgnored() throws Exception {
        // Attempting to send injected security fields in JSON payload
        Map<String, Object> maliciousPayload = Map.of(
                "name", "Alpha Trying Escalation",
                "phone", "+919888888888",
                "role", "ADMIN",
                "status", "SUSPENDED"
        );

        mockMvc.perform(put("/api/v1/users/me")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(maliciousPayload)))
                .andExpect(status().isOk());

        UserEntity userInDb = userRepository.findById(userA.getId()).orElseThrow();
        assertThat(userInDb.getName()).isEqualTo("Alpha Trying Escalation");
        assertThat(userInDb.getPhone()).isEqualTo("+919888888888");
        // Role and status MUST remain unchanged
        assertThat(userInDb.getRole()).isEqualTo(UserRole.USER);
        assertThat(userInDb.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }
}
