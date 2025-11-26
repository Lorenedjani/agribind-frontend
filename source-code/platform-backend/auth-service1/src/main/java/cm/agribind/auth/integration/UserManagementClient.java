// SOLUTION: Update Auth Service to use internal endpoint
// File: source-code/platform-backend/auth-service1/src/main/java/cm/agribind/auth/integration/UserManagementClient.java

package cm.agribind.auth.integration;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "user-management-service",
        url = "${services.user-management:http://localhost:8082}"
)
public interface UserManagementClient {

    // ✅ NEW: Use internal auth endpoint that includes password hash
    @GetMapping("/api/v1/users/internal/auth/{username}")
    UserDto getUserByUsername(@PathVariable("username") String username);

    // ✅ Keep other endpoints unchanged
    @GetMapping("/api/v1/users/phone/{phoneNumber}")
    UserDto getUserByPhone(@PathVariable("phoneNumber") String phoneNumber);

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