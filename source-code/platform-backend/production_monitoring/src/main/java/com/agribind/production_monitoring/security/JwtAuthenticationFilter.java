package com.agribind.production_monitoring.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${app.security.jwt.secret}")
    private String jwtSecret;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7).trim();
            if (!token.isEmpty()) {
                try {
                    SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
                    Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();

                    String userId = claims.get("userId", String.class);
                    if (userId == null || userId.isBlank()) {
                        userId = claims.getSubject();
                    }
                    String role = claims.get("role", String.class);
                    String cooperativeId = claims.get("cooperativeId", String.class);
                    String region = claims.get("region", String.class);

                    if (role != null) {
                        Map<String, Object> details = new HashMap<>();
                        if (cooperativeId != null) {
                            details.put("cooperativeId", cooperativeId);
                        }
                        if (region != null && !region.isBlank()) {
                            details.put("region", region.trim());
                        }
                        var authorities = List.of(new SimpleGrantedAuthority(role));
                        var authentication = new UsernamePasswordAuthenticationToken(
                                userId != null ? userId : claims.getSubject(),
                                null,
                                authorities
                        );
                        authentication.setDetails(details);
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                } catch (Exception e) {
                    log.debug("JWT validation failed: {}", e.getMessage());
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
