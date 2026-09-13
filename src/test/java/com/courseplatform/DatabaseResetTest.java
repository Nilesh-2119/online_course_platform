package com.courseplatform;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

@SpringBootTest
@ActiveProfiles("dev")
@TestPropertySource(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=none"
})
public class DatabaseResetTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void resetAllUserData() throws Exception {
        System.out.println(">>> 1. DROPPING ALL EXISTING TABLES & HISTORY...");
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            // Kill any sleeping connections
            try (ResultSet rs = stmt.executeQuery("SELECT ID FROM INFORMATION_SCHEMA.PROCESSLIST WHERE ID != CONNECTION_ID() AND COMMAND = 'Sleep'")) {
                while (rs.next()) {
                    long pid = rs.getLong("ID");
                    try (Statement killStmt = conn.createStatement()) {
                        killStmt.execute("KILL " + pid);
                    } catch (Exception ignored) {}
                }
            } catch (Exception ignored) {}

            stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
            stmt.execute("DROP TABLE IF EXISTS verification_otps");
            stmt.execute("DROP TABLE IF EXISTS user_auth_providers");
            stmt.execute("DROP TABLE IF EXISTS refresh_tokens");
            stmt.execute("DROP TABLE IF EXISTS video_progress");
            stmt.execute("DROP TABLE IF EXISTS course_purchases");
            stmt.execute("DROP TABLE IF EXISTS videos");
            stmt.execute("DROP TABLE IF EXISTS course_sections");
            stmt.execute("DROP TABLE IF EXISTS courses");
            stmt.execute("DROP TABLE IF EXISTS free_resources");
            stmt.execute("DROP TABLE IF EXISTS users");
            stmt.execute("DROP TABLE IF EXISTS flyway_schema_history");
            stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
        }

        System.out.println(">>> 2. RUNNING CLEAN FLYWAY MIGRATIONS (V1, V2, V3)...");
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .cleanDisabled(false)
                .load();

        flyway.migrate();
        System.out.println(">>> DATABASE RESET & MIGRATION COMPLETE! Schema is fresh, streamlined, and clean.");
    }
}
