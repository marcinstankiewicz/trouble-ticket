package service.troubleticket.config.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Component
public class JwtTokenProvider {
    @Value("${jwt.secret:your-secret-key-for-dev-only}")
    private String jwtSecret;
    
    @Value("${jwt.expiration:86400000}") // 24h
    private long jwtExpirationInMs;

    public String getTenantIdFromJWT(String token) {
        SecretKey key = new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), 0, 
            jwtSecret.getBytes(StandardCharsets.UTF_8).length, "HmacSHA256");
        Claims claims = Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .getPayload();
        return claims.getSubject();
    }

    public boolean validateToken(String authToken) {
        try {
            SecretKey key = new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), 0, 
                jwtSecret.getBytes(StandardCharsets.UTF_8).length, "HmacSHA256");
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(authToken);
            return true;
        } catch (SignatureException ex) {
            System.err.println("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            System.err.println("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            System.err.println("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            System.err.println("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            System.err.println("JWT claims string is empty");
        } catch (Exception ex) {
            System.err.println("JWT validation error: " + ex.getMessage());
        }
        return false;
    }
}
