package service.troubleticket.config.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.web.AuthenticationEntryPoint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @Mock
    private JwtTokenFilter jwtTokenFilter;

    @Test
    @DisplayName("AuthenticationEntryPoint should return 401 JSON payload")
    void authenticationEntryPointShouldReturnUnauthorized() throws Exception {
        // Arrange
        SecurityConfig securityConfig = new SecurityConfig(jwtTokenFilter);
        AuthenticationEntryPoint entryPoint = securityConfig.authenticationEntryPoint();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        // Act
        entryPoint.commence(request, response, new BadCredentialsException("bad credentials"));

        // Assert
        assertEquals(401, response.getStatus());
        assertEquals("application/json", response.getContentType());
        JsonNode json = new ObjectMapper().readTree(response.getContentAsString());
        assertEquals("UNAUTHORIZED", json.get("code").asText());
        assertTrue(json.get("requestId").asText().startsWith("req-"));
    }
}
