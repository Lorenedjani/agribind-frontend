package com.agribind.communication.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI communicationServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AgriBind Communication Service API")
                        .description("API documentation for the Communication Service - handles SMS, audio messages, alerts, templates, and resource requests")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("AgriBind Development Team")
                                .email("support@agribind.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8084")
                                .description("Local Development Server"),
                        new Server()
                                .url("https://api.agribind.com/communication")
                                .description("Production Server")
                ));
    }
}

