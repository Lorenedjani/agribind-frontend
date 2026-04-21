package cm.agribind.usermanagement.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
@Configuration
public class WebConfig implements WebMvcConfigurer {

    // ✅ REMOVED: CORS configuration (API Gateway handles CORS)
    // @Override
    // public void addCorsMappings(CorsRegistry registry) { ... }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve uploaded files
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:./uploads/");

        // Serve export files
        registry.addResourceHandler("/exports/**")
                .addResourceLocations("file:./exports/");
    }
}