package cm.agribind.auth.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

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
        // Include a UUID jti so that no two refresh tokens can ever be identical,
        // even if two users share the same deviceId and log in within the same second.
        Map<String, Object> claims = Map.of(
                "type",     "refresh",
                "deviceId", deviceId != null ? deviceId : "default",
                "jti",      UUID.randomUUID().toString()   // ← guarantees uniqueness
        );
        return buildToken(userId, claims, refreshTokenExpirationMs);
    }

    private String buildToken(String subject, Map<String, Object> claims, long expirationMs) {
        Date now = new Date();
        Date expiry = new Date(System.currentTimeMillis() + expirationMs);

        var builder = Jwts.builder()
                // ── IMPORTANT: addClaims() must come BEFORE setSubject / setIssuedAt /
                // setExpiration.  The older setClaims() call would REPLACE the entire
                // payload map, silently discarding the subject and expiry that were set
                // before it.  addClaims() merges into the existing map instead.
                .addClaims(claims != null ? claims : Map.of())
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(secretKey, SignatureAlgorithm.HS256);

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