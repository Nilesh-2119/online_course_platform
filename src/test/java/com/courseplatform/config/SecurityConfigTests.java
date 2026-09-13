package com.courseplatform.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityConfigTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Public Actuator health endpoint is accessible without authentication")
    void actuatorHealth_isPublic() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("Protected endpoint returns 401 with structured ErrorResponse for unauthenticated request")
    void protectedEndpoint_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/users/me").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    @DisplayName("CORS preflight for www.adfixstudio.com on /api/v1/auth/register returns allowed headers")
    void corsPreflight_wwwAdfixstudio_allowed() throws Exception {
        mockMvc.perform(options("/api/v1/auth/register")
                        .header("Origin", "https://www.adfixstudio.com")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "https://www.adfixstudio.com"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }

    @Test
    @DisplayName("CORS preflight for apex domain adfixstudio.com on /api/v1/auth/register returns allowed headers")
    void corsPreflight_apexAdfixstudio_allowed() throws Exception {
        mockMvc.perform(options("/api/v1/auth/register")
                        .header("Origin", "https://adfixstudio.com")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "https://adfixstudio.com"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }

    @Test
    @DisplayName("CORS preflight for vercel preview domain on /api/v1/auth/register returns allowed headers")
    void corsPreflight_vercelPreview_allowed() throws Exception {
        mockMvc.perform(options("/api/v1/auth/register")
                        .header("Origin", "https://adfixstudio-preview.vercel.app")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "https://adfixstudio-preview.vercel.app"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }
}
