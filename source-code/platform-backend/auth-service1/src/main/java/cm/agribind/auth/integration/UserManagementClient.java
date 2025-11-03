package cm.agribind.auth.integration;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

// UserManagementClient.java
//@FeignClient(name = "user-management-service", url = "${services.user-management}")
public interface UserManagementClient {

    @GetMapping("/username/{username}")
    UserDto getUserByUsername(@PathVariable String username);

    @GetMapping("/registration/{registrationNumber}")
    UserDto getUserByRegistrationNumber(@PathVariable String registrationNumber);

    @GetMapping("/{userId}")
    UserDto getUserById(@PathVariable String userId);

    @PutMapping("/{userId}/password")
    void updatePassword(@PathVariable String userId, @RequestBody PasswordUpdateRequest request);

    @PutMapping("/{userId}/first-login")
    void markFirstLoginComplete(@PathVariable String userId);

    @PutMapping("/{userId}/language")
    void updateLanguage(@PathVariable String userId, @RequestBody LanguageUpdateRequest request);
}