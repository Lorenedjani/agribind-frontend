package cm.agribind.usermanagement.integration.client;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceClient {

    private final RestTemplate restTemplate;

    @Value("${services.auth-service.url}")
    private String authServiceUrl;

    public CreateAuthUserResponse createAuthUser(CreateAuthUserRequest request) {
        try {
            String url = authServiceUrl + "/api/v1/auth/users/create";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<CreateAuthUserRequest> entity = new HttpEntity<>(request, headers);

            return restTemplate.postForObject(url, entity, CreateAuthUserResponse.class);
        } catch (Exception e) {
            log.error("Failed to create auth user", e);
            throw new RuntimeException("Auth service communication failed", e);
        }
    }

    @Data
    public static class CreateAuthUserRequest {
        private String userId;
        private String username;
        private String email;
        private String phoneNumber;
        private String passwordHash;
        private String role;
        private String cooperativeId;
        private String preferredLanguage;
        private Boolean firstLogin = true;
    }

    @Data
    public static class CreateAuthUserResponse {
        private String userId;
        private String username;
        private boolean success;
        private String message;
    }
}
