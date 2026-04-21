package cm.agribind.auth.integration;

import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("test")  // Only active when profile=test
public class MockUserManagementClient implements UserManagementClient {

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    // Pre-hashed password for "password123"
    // Generated with: new BCryptPasswordEncoder(12).encode("password123")
    private static final String HASHED_PASSWORD = "$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYIq0z8aU9K";

    @Override
    public UserDto getUserByUsername(String username) {
        // Support multiple test users
        return UserDto.builder()
                .userId("TEST-USER-001")
                .username(username)
                .email(username.contains("@") ? username : username + "@agribind.cm")
                .phoneNumber("+237670123456")
                .passwordHash(HASHED_PASSWORD)
                .role(determineRole(username))
                .cooperativeId("COOP-001")
                .region("CENTRE")
                .preferredLanguage("fr")
                .firstLogin(false)
                .accountLocked(false)
                .accountEnabled(true)
                .failedLoginAttempts(0)
                .registrationNumber("REG-" + username.toUpperCase())
                .build();
    }

    @Override
    public UserDto getUserByPhone(String phoneNumber) {
        return null;
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

    private String determineRole(String username) {
        if (username.toLowerCase().contains("farmer")) {
            return "FARMER";
        } else if (username.toLowerCase().contains("coop")) {
            return "COOPERATIVE";
        } else if (username.toLowerCase().contains("gov")) {
            return "GOVERNMENT";
        }
        return "FARMER"; // Default role
    }

    // Method to verify password (for testing)
    public static boolean verifyPassword(String rawPassword) {
        return encoder.matches(rawPassword, HASHED_PASSWORD);
    }
}