// JwtService.java
package cm.agribind.auth.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

@Component
@Slf4j
public class JwtService {
    private final SecretKey secretKey;
    private final long accessTokenExpirationMs;
    private final long refreshTokenExpirationMs;

    public JwtService(@Value("${jwt.secret}") String secret,
                      @Value("${jwt.access-token-expiration-ms:900000}") long accessTokenExpirationMs,
                      @Value("${jwt.refresh-token-expiration-ms:2592000000}") long refreshTokenExpirationMs) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    public String generateAccessToken(String userId, String role, Map<String, Object> claims) {
        return buildToken(userId, claims, accessTokenExpirationMs);
    }

    public String generateRefreshToken(String userId, String deviceId) {
        Map<String, Object> claims = Map.of("type", "refresh", "deviceId", deviceId != null ? deviceId : "default");
        return buildToken(userId, claims, refreshTokenExpirationMs);
    }

    private String buildToken(String subject, Map<String, Object> claims, long expirationMs) {
        var builder = Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(secretKey, SignatureAlgorithm.HS256);

        if (claims != null) {
            builder.setClaims(claims);
        }

        return builder.compact();
    }

    public Jws<Claims> parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token);
    }

    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("Token expired: {}", e.getMessage());
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid token: {}", e.getMessage());
            return false;
        }
    }

    public String getUserIdFromToken(String token) {
        return parseToken(token).getBody().getSubject();
    }

    public String getRoleFromToken(String token) {
        return parseToken(token).getBody().get("role", String.class);
    }

    public long getAccessTokenExpiration() {
        return accessTokenExpirationMs;
    }

    public long getRefreshTokenExpiration() {
        return refreshTokenExpirationMs;
    }
}