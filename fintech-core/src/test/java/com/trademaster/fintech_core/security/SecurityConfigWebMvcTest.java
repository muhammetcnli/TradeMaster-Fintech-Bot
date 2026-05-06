package com.trademaster.fintech_core.security;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityConfigWebMvcTest {

    @Test
    void marketEndpointShouldRequireBearerAuth() throws IOException {
        String source = Files.readString(securityConfigPath(), StandardCharsets.UTF_8);

        assertTrue(source.contains(".requestMatchers(\"/api/v1/market/**\").authenticated()"),
                "Market endpoints should be secured with bearer auth");
        assertFalse(source.contains(".requestMatchers(\"/api/v1/market/**\").permitAll()"),
                "Market endpoints must not be publicly exposed");
    }

    @Test
    void telegramAndAuthBootstrapsShouldStayPublic() throws IOException {
        String source = Files.readString(securityConfigPath(), StandardCharsets.UTF_8);

        assertTrue(source.contains(".requestMatchers(\"/api/v1/auth/**\").permitAll()"));
        assertTrue(source.contains(".requestMatchers(\"/api/v1/telegram/webhook\").permitAll()"));
    }

    private Path securityConfigPath() {
        return Path.of("src", "main", "java", "com", "trademaster", "fintech_core", "security", "SecurityConfig.java");
    }
}


