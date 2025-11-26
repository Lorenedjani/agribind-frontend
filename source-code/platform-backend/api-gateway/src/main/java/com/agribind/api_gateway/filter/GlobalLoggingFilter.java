package com.agribind.api_gateway.filters;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Component
public class GlobalLoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        long startTime = System.currentTimeMillis();
        ServerHttpRequest request = exchange.getRequest();

        System.out.println("🌐 GATEWAY REQUEST INBOUND:");
        System.out.println("   📍 ID: " + request.getId());
        System.out.println("   🔗 Method: " + request.getMethod());
        System.out.println("   🛣️  Path: " + request.getPath());
        System.out.println("   🌐 Remote Address: " + request.getRemoteAddress());
        System.out.println("   🎯 Route ID: " + exchange.getAttributeOrDefault("org.springframework.cloud.gateway.support.gatewayRoute", "UNKNOWN"));

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            long duration = System.currentTimeMillis() - startTime;
            System.out.println("✅ GATEWAY RESPONSE OUTBOUND:");
            System.out.println("   ⏱️  Processing Time: " + duration + "ms");
            System.out.println("   📊 Status: " + Objects.requireNonNull(exchange.getResponse().getStatusCode()).value());
            System.out.println("────────────────────────────────────────");
        }));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}