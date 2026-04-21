package com.agribind.api_gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class RequestLoggingFilter extends AbstractGatewayFilterFactory<RequestLoggingFilter.Config> {

    public RequestLoggingFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            System.out.println("🚀 GATEWAY REQUEST INBOUND:");
            System.out.println("   → Method: " + exchange.getRequest().getMethod());
            System.out.println("   → Path: " + exchange.getRequest().getPath());
            System.out.println("   → URI: " + exchange.getRequest().getURI());
            System.out.println("   → Headers: " + exchange.getRequest().getHeaders().getOrigin());
            System.out.println("   → Query Params: " + exchange.getRequest().getQueryParams());

            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                System.out.println("✅ GATEWAY RESPONSE OUTBOUND:");
                System.out.println("   ← Status: " + (exchange.getResponse().getStatusCode() != null ?
                        exchange.getResponse().getStatusCode().value() : "UNKNOWN"));
                System.out.println("   ← CORS Headers: " + exchange.getResponse().getHeaders().get("Access-Control-Allow-Origin"));
            }));
        };
    }

    public static class Config {
        // Configuration properties if needed
    }
}