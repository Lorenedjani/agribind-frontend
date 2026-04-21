# Swagger/OpenAPI Documentation Setup

This document describes the Swagger/OpenAPI documentation setup for the AgriBind Platform backend services.

## Overview

Swagger (OpenAPI 3.0) has been integrated into the following services:
- **Communication Service** (Spring Boot 3.2.5) ✅
- **Microcredit Service** (Spring Boot 2.7.0) ✅
- **Notification Service** (Spring Boot 3.2.0) ✅
- **Inventory Service** (Spring Boot 3.2.0 - WebFlux) ✅
- **Plant Monitoring Service** (Spring Boot 3.1.5) ✅
- **Production Monitoring Service** (Spring Boot 3.2.5) ✅
- **User Management Service** (Spring Boot 3.2.0) ✅

## Accessing Swagger UI

After starting each service, you can access the Swagger UI at:

### Communication Service
- **Swagger UI**: http://localhost:8084/swagger-ui.html
- **API Docs (JSON)**: http://localhost:8084/api-docs

### Microcredit Service
- **Swagger UI**: http://localhost:8091/microcredit/swagger-ui.html
- **API Docs (JSON)**: http://localhost:8091/microcredit/api-docs

### Notification Service
- **Swagger UI**: http://localhost:8083/swagger-ui.html
- **API Docs (JSON)**: http://localhost:8083/api-docs

### Inventory Service
- **Swagger UI**: http://localhost:8085/swagger-ui.html
- **API Docs (JSON)**: http://localhost:8085/api-docs

### User Management Service
- **Swagger UI**: http://localhost:8080/user-management/swagger-ui.html
- **API Docs (JSON)**: http://localhost:8080/user-management/api-docs

### Production Monitoring Service
- **Swagger UI**: http://localhost:8085/swagger-ui.html (if configured)
- **API Docs (JSON)**: http://localhost:8085/api-docs

### Plant Monitoring Service
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs (JSON)**: http://localhost:8080/api-docs

### Production Monitoring Service
- **Swagger UI**: http://localhost:8085/swagger-ui.html
- **API Docs (JSON)**: http://localhost:8085/api-docs

## Configuration

### Dependencies

#### Spring Boot 3.x Services
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>
```

#### Spring Boot 2.x Services
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-ui</artifactId>
    <version>1.7.0</version>
</dependency>
```

### Application Properties

Add the following to `application.yaml`:

```yaml
springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
    operations-sorter: method
    tags-sorter: alpha
    try-it-out-enabled: true
  show-actuator: false
```

### Swagger Configuration Class

Each service has a `SwaggerConfig.java` class that configures:
- API title and description
- Version information
- Contact information
- Server URLs (local and production)

## Adding Swagger Annotations

### Controller Level
```java
@Tag(name = "Service Name", description = "API description")
@RestController
@RequestMapping("/api/endpoint")
public class MyController {
    // ...
}
```

### Method Level
```java
@Operation(summary = "Operation summary", description = "Detailed description")
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success",
        content = @Content(schema = @Schema(implementation = ResponseDTO.class))),
    @ApiResponse(responseCode = "400", description = "Bad Request"),
    @ApiResponse(responseCode = "500", description = "Internal Server Error")
})
@GetMapping("/endpoint")
public ResponseEntity<ResponseDTO> getData(
    @Parameter(description = "Parameter description", required = true)
    @RequestParam String param) {
    // ...
}
```

## Features

- **Interactive API Testing**: Try out API endpoints directly from Swagger UI
- **Request/Response Schemas**: View detailed request and response models
- **Authentication Support**: Configure JWT/Bearer token authentication
- **Grouped Endpoints**: Endpoints are organized by tags
- **Search Functionality**: Search for specific endpoints or models

## Security

For services with authentication, you can configure security schemes in the SwaggerConfig:

```java
@Bean
public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .info(...)
        .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
        .components(new Components()
            .addSecuritySchemes("Bearer Authentication", 
                new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")));
}
```

## Next Steps

To add Swagger to other services:
1. Add the appropriate dependency to `pom.xml`
2. Create a `SwaggerConfig.java` class
3. Add Swagger properties to `application.yaml`
4. Add `@Tag` and `@Operation` annotations to controllers
5. Restart the service and access Swagger UI

## Troubleshooting

- **404 on Swagger UI**: Check that the path in `application.yaml` matches the context path
- **Missing endpoints**: Ensure controllers are in the component scan path
- **Authentication issues**: Configure security schemes in SwaggerConfig if using JWT

