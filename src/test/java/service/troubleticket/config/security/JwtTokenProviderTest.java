package service.troubleticket.config.security;

import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtTokenProviderTest {

    private static final String SECRET = "my-very-strong-dev-secret-for-tests-123456";

    @Test
    @DisplayName("validateToken should return true and subject should be extracted for valid token")
    void validateAndExtractSubjectFromValidToken() {
        // Arrange
        JwtTokenProvider provider = new JwtTokenProvider();
        ReflectionTestUtils.setField(provider, "jwtSecret", SECRET);
        SecretKey key = new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), 0,
                SECRET.getBytes(StandardCharsets.UTF_8).length, "HmacSHA256");

        // Act
        String token = Jwts.builder()
                .subject("tenant-1")
                .signWith(key)
                .compact();

        // Assert
        assertTrue(provider.validateToken(token));
        assertEquals("tenant-1", provider.getTenantIdFromJWT(token));
    }

    @Test
    @DisplayName("validateToken should return false for malformed token")
    void validateMalformedToken() {
        // Arrange
        JwtTokenProvider provider = new JwtTokenProvider();

        // Act
        ReflectionTestUtils.setField(provider, "jwtSecret", SECRET);

        // Assert
        assertFalse(provider.validateToken("not-a-jwt"));
    }
}
