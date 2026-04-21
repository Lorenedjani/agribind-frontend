package cm.agribind.usermanagement.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    /**
     * Create a LoadBalanced RestTemplate for Eureka service discovery
     * This will be used for calls like http://NOTIFICATION-SERVICE/...
     */
    @Bean
    @LoadBalanced
    public RestTemplate loadBalancedRestTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    /**
     * Create a standard RestTemplate WITHOUT LoadBalancer for direct HTTP calls
     * This will be used for calls to localhost URLs like http://localhost:8083/...
     */
    @Bean
    @Primary
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }
}