package com.courseplatform;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class CoursePlatformApplicationTests {

    @Test
    @DisplayName("Context Loads Successfully")
    void contextLoads() {
        // Verifies that the Spring application context starts up cleanly
    }
}
