// source-code/platform-backend/api-gateway/src/main/java/com/agribind/api_gateway/config/SecurityConfig.java
package com.agribind.api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        // ✅ CRITICAL: Allow auth endpoints without authentication
                        .pathMatchers(
                                "/api/v1/auth/**",           // All auth endpoints
                                "/api/v1/users/exists/**",   // User existence checks
                                "/actuator/health",          // Health checks
                                "/actuator/info"             // Info endpoint
                        ).permitAll()
                        // All other requests require authentication
                        .anyExchange().authenticated()
                )
                // ✅ IMPORTANT: Disable HTTP Basic authentication
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .build();
    }
}