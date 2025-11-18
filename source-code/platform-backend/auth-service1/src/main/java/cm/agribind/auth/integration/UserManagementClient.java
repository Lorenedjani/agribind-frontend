package cm.agribind.auth.integration;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

// ✅ FIXED: Proper Feign client that calls real User Management Service
@FeignClient(
        name = "user-management-service",
        url = "${services.user-management:http://localhost:8082}"
)
public interface UserManagementClient {

    @GetMapping("/api/v1/users/username/{username}")
    UserDto getUserByUsername(@PathVariable("username") String username);

    UserDto getUserByPhone(String phoneNumber);

    @GetMapping("/api/v1/users/registration/{registrationNumber}")
    UserDto getUserByRegistrationNumber(@PathVariable("registrationNumber") String registrationNumber);

    @GetMapping("/api/v1/users/{userId}")
    UserDto getUserById(@PathVariable("userId") String userId);

    @PutMapping("/api/v1/users/{userId}/password")
    void updatePassword(@PathVariable("userId") String userId, @RequestBody PasswordUpdateRequest request);

    @PutMapping("/api/v1/users/{userId}/first-login")
    void markFirstLoginComplete(@PathVariable("userId") String userId);

    @PutMapping("/api/v1/users/{userId}/language")
    void updateLanguage(@PathVariable("userId") String userId, @RequestBody LanguageUpdateRequest request);
}
