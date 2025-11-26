/*package com.agribind.api_gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.util.List;

@Slf4j
@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    @Value("${jwt.secret:defaultSecretKeyForDevelopmentOnly}")
    private String jwtSecret;

    private static final List<String> PUBLIC_ENDPOINTS = List.of(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/refresh",
            "/api/v1/auth/qr-login",
            "/api/v1/auth/health",
            "/api/v1/users/exists/**",
            "/actuator/health"
    );

    public JwtAuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getPath().value();
            String method = request.getMethod().name();

            // 🎯 DEBUG LOGGING
            System.out.println("=".repeat(60));
            System.out.println("🔐 JWT FILTER DEBUG INFO:");
            System.out.println("   📍 Path: " + path);
            System.out.println("   🛠️  Method: " + method);
            System.out.println("   🌐 Origin: " + request.getHeaders().getFirst(HttpHeaders.ORIGIN));
            System.out.println("   🔗 Headers: " + request.getHeaders().keySet());

            boolean isPublic = isPublicEndpoint(path);
            System.out.println("   ✅ Public Endpoint: " + isPublic);

            if (isPublic) {
                System.out.println("   🟢 SKIPPING JWT validation for public endpoint");
                System.out.println("=".repeat(60));
                return chain.filter(exchange);
            }

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            System.out.println("   🔑 Authorization Header: " +
                    (authHeader != null ? authHeader.substring(0, Math.min(authHeader.length(), 20)) + "..." : "NULL"));

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                System.out.println("   ❌ MISSING or INVALID Authorization header");
                System.out.println("=".repeat(60));
                return onError(exchange, "Missing or invalid authorization header", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);
            System.out.println("   🪙 Token length: " + token.length() + " characters");

            try {
                if (!isTokenValid(token)) {
                    System.out.println("   ❌ INVALID token");
                    System.out.println("=".repeat(60));
                    return onError(exchange, "Invalid token", HttpStatus.UNAUTHORIZED);
                }

                Claims claims = getClaimsFromToken(token);
                String username = claims.getSubject();
                String role = claims.get("role", String.class);
                String userId = claims.get("userId", String.class);

                System.out.println("   👤 User: " + username);
                System.out.println("   🎭 Role: " + role);
                System.out.println("   🆔 UserId: " + userId);
                System.out.println("   🟢 JWT VALIDATION SUCCESSFUL");

                ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                        .header("X-User-Id", userId)
                        .header("X-User-Name", username)
                        .header("X-User-Role", role)
                        .build();

                System.out.println("=".repeat(60));
                return chain.filter(exchange.mutate().request(modifiedRequest).build());

            } catch (Exception e) {
                System.out.println("   💥 TOKEN VALIDATION EXCEPTION: " + e.getMessage());
                System.out.println("=".repeat(60));
                log.error("Token validation failed", e);
                return onError(exchange, "Token validation failed", HttpStatus.UNAUTHORIZED);
            }
        };
    }

    private boolean isPublicEndpoint(String path) {
        boolean isPublic = PUBLIC_ENDPOINTS.stream().anyMatch(pattern -> {
            if (pattern.endsWith("/**")) {
                String basePath = pattern.substring(0, pattern.length() - 3);
                return path.startsWith(basePath);
            } else {
                return path.equals(pattern);
            }
        });

        System.out.println("   🔍 Checking if public - Path: " + path + ", Result: " + isPublic);
        return isPublic;
    }

    private boolean isTokenValid(String token) {
        try {
            getClaimsFromToken(token);
            return true;
        } catch (Exception e) {
            System.out.println("   🚫 Token validation error: " + e.getMessage());
            return false;
        }
    }

    private Claims getClaimsFromToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Mono<Void> onError(ServerWebExchange exchange, String error, HttpStatus status) {
        System.out.println("   🚨 JWT FILTER ERROR: " + status + " - " + error);
        exchange.getResponse().setStatusCode(status);
        return exchange.getResponse().setComplete();
    }

    public static class Config {
        // Configuration properties if needed
    }
}

 */