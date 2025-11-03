package cm.agribind.usermanagement.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${agribind.openapi.dev-url:http://localhost:8082}")
    private String devUrl;

    @Value("${agribind.openapi.prod-url:https://api.agribind.cm}")
    private String prodUrl;

    @Bean
    public OpenAPI customOpenAPI() {
        Server devServer = new Server()
                .url(devUrl)
                .description("Development Server");

        Server prodServer = new Server()
                .url(prodUrl)
                .description("Production Server");

        Contact contact = new Contact()
                .name("AgriBind Support")
                .email("support@agribind.cm")
                .url("https://agribind.cm");

        License license = new License()
                .name("AgriBind License")
                .url("https://agribind.cm/license");

        Info info = new Info()
                .title("AgriBind User Management API")
                .version("1.0.0")
                .contact(contact)
                .description("""
                    This API provides comprehensive user management capabilities for the AgriBind agricultural platform.
                    
                    **Key Features:**
                    - User registration and management for farmers, cooperatives, and government officials
                    - Advanced filtering and search capabilities
                    - QR code generation for registration and login
                    - Dashboard metrics and analytics
                    - Data export in multiple formats (CSV, Excel, PDF)
                    - Multi-language support
                    
                    **User Types:**
                    - **FARMER**: Individual farmers with crop and livestock details
                    - **COOPERATIVE**: Farmer cooperatives with member management
                    - **GOVERNMENT**: Government officials with regional assignments
                    
                    **Authentication:**
                    Most endpoints require JWT authentication. Use the Auth Service to obtain tokens.
                    """)
                .license(license);

        // Security scheme for JWT
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");

        Components components = new Components()
                .addSecuritySchemes("bearerAuth", securityScheme);

        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("bearerAuth");

        return new OpenAPI()
                .info(info)
                .servers(List.of(devServer, prodServer))
                .components(components)
                .addSecurityItem(securityRequirement);
    }
}