package service.troubleticket.config.security;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtTokenFilterTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should set authentication when bearer token is valid")
    void shouldSetAuthenticationWhenTokenValid() throws ServletException, IOException {
        // Arrange
        JwtTokenFilter filter = new JwtTokenFilter(jwtTokenProvider);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer good-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        when(jwtTokenProvider.validateToken("good-token")).thenReturn(true);
        when(jwtTokenProvider.getTenantIdFromJWT("good-token")).thenReturn("tenant-1");

        // Act
        filter.doFilter(request, response, chain);

        // Assert
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("tenant-1", SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        verify(jwtTokenProvider).validateToken("good-token");
        verify(jwtTokenProvider).getTenantIdFromJWT("good-token");
    }

    @Test
    @DisplayName("Should skip authentication when authorization header is missing")
    void shouldSkipAuthenticationWhenHeaderMissing() throws ServletException, IOException {
        // Arrange
        JwtTokenFilter filter = new JwtTokenFilter(jwtTokenProvider);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        // Act
        filter.doFilter(request, response, chain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
