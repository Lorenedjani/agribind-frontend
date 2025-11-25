// source-code/platform-backend/api-gateway/src/main/java/com/agribind/api_gateway/config/CorsConfig.java
package com.agribind.api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();

        // ✅ Allow Angular dev server
        corsConfig.setAllowedOrigins(Arrays.asList(
                "http://localhost:4200",
                "http://localhost:4201",
                "http://127.0.0.1:4200",
                "http://localhost:3000",
                "https://cooperative-platform.gov.cm"
        ));

        // ✅ Allow credentials (cookies, authorization headers)
        corsConfig.setAllowCredentials(true);

        // ✅ Cache preflight response for 1 hour
        corsConfig.setMaxAge(3600L);

        // ✅ Allow all HTTP methods
        corsConfig.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));

        // ✅ Allow all headers
        corsConfig.setAllowedHeaders(List.of("*"));

        // ✅ Expose Authorization header to client
        corsConfig.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-User-Id",
                "X-User-Role"
        ));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}