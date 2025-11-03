// MockUserManagementClient.java
package cm.agribind.auth.integration;

import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
 // ← This makes Spring use this mock instead of the Feign client
public class MockUserManagementClient implements UserManagementClient {

    @Override
    public UserDto getUserByUsername(String username) {
        // Create a mock user with hashed password for "password123"
        String hashedPassword = "$2a$12$KcA8AzH.6c1q6Q7q6Q7q6OQ7q6Q7q6Q7q6Q7q6Q7q6Q7q6Q7q6Q7q6";

        return UserDto.builder()
                .userId("mock-user-123")
                .username(username)
                .email(username + "@agribind.com")
                .passwordHash(hashedPassword)
                .role("FARMER")
                .cooperativeId("coop-001")
                .preferredLanguage("EN")
                .firstLogin(false)
                .accountLocked(false)
                .accountEnabled(true)
                .failedLoginAttempts(0)
                .build();
    }

    @Override
    public UserDto getUserByRegistrationNumber(String registrationNumber) {
        return getUserByUsername(registrationNumber);
    }

    @Override
    public UserDto getUserById(String userId) {
        return getUserByUsername("testuser");
    }

    @Override
    public void updatePassword(String userId, PasswordUpdateRequest request) {
        System.out.println("Mock: Password updated for user " + userId);
    }

    @Override
    public void markFirstLoginComplete(String userId) {
        System.out.println("Mock: First login completed for user " + userId);
    }

    @Override
    public void updateLanguage(String userId, LanguageUpdateRequest request) {
        System.out.println("Mock: Language updated to " + request.getLanguage() + " for user " + userId);
    }
}