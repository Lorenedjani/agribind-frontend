package cm.agribind.usermanagement.service.command.impl;

import cm.agribind.usermanagement.dto.command.CreateUserCommand;
import cm.agribind.usermanagement.dto.command.UpdateUserCommand;
import cm.agribind.usermanagement.dto.event.*;
import cm.agribind.usermanagement.dto.response.UserResponse;
import cm.agribind.usermanagement.entity.*;
import cm.agribind.usermanagement.enums.*;
import cm.agribind.usermanagement.exception.UserNotFoundException;
import cm.agribind.usermanagement.integration.client.NotificationServiceClient;
import cm.agribind.usermanagement.integration.dto.WelcomeNotificationRequest;
import cm.agribind.usermanagement.integration.event.UserEventPublisher;
import cm.agribind.usermanagement.mapper.UserMapper;
import cm.agribind.usermanagement.repository.UserRepository;
import cm.agribind.usermanagement.service.QRCodeService;
import cm.agribind.usermanagement.service.command.UserCommandService;
import cm.agribind.usermanagement.util.FileStorageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Simplified User Command Service - Removes optional dependencies
 * that might cause startup failures
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SimplifiedUserCommandServiceImpl implements UserCommandService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserEventPublisher userEventPublisher;
    private final QRCodeService qrCodeService;
    private final FileStorageUtil fileStorageUtil;
    private final NotificationServiceClient notificationServiceClient;
    private final PasswordEncoder passwordEncoder; // ✅ SECURE PASSWORD ENCODER

    // CRITICAL BACKEND FIX: SimplifiedUserCommandServiceImpl.java
// Replace the createUser method with this fixed version

    @Override
    public UserResponse createUser(CreateUserCommand command) {
        log.info("Creating user of type: {} with name: {}", command.getType(), command.getName());

        try {
            // ✅ 1. Validate required fields
            validateCreateUserCommand(command);

            // ✅ 2. Check phone number uniqueness
            if (userRepository.existsByPhoneNumber(command.getPhoneNumber())) {
                throw new IllegalArgumentException("Phone number already exists: " + command.getPhoneNumber());
            }

            // ✅ 3. Check email uniqueness if provided
            if (command.getEmail() != null && !command.getEmail().trim().isEmpty()) {
                if (userRepository.existsByEmail(command.getEmail())) {
                    throw new IllegalArgumentException("Email already exists: " + command.getEmail());
                }
            }

            // ✅ 4. Create user entity based on type
            User user = createUserByType(command);

            // ✅ 5. Generate IDs
            String userId = generateUserId(command.getType());
            user.setUserId(userId);
            user.setRegistrationNumber(generateRegistrationNumber());

            // ✅ 6. Generate and encrypt password
            String defaultPassword = generateDefaultPassword(command.getType());
            String passwordHash = passwordEncoder.encode(defaultPassword);
            user.setPasswordHash(passwordHash);
            user.setFirstLogin(true);
            user.setAccountLocked(false); // ✅ Initialize
            user.setFailedLoginAttempts(0); // ✅ Initialize

            // ✅ 7. Create profile
            Profile profile = new Profile();
            profile.setPreferredLanguage(
                    command.getPreferredLanguage() != null ?
                            command.getPreferredLanguage() : "fr"
            );
            profile.setReceiveSmsNotifications(true);
            profile.setReceiveEmailNotifications(command.getEmail() != null);
            user.setProfile(profile);

            // ✅ 8. Save user
            User savedUser = userRepository.save(user);
            log.info("✅ User saved with ID: {}", savedUser.getUserId());

            // ✅ 9. Send welcome notifications (async - don't block)
            try {
                sendWelcomeNotifications(savedUser, defaultPassword);
            } catch (Exception e) {
                log.error("⚠️ Failed to send notifications, but user created: {}",
                        savedUser.getUserId(), e);
                // Don't throw - user creation succeeded
            }

            // ✅ 10. Publish events (async - don't block)
            try {
                publishUserCreatedEvent(savedUser);
            } catch (Exception e) {
                log.warn("⚠️ Failed to publish event: {}", e.getMessage());
            }

            log.info("✅ User creation completed: {}", savedUser.getUserId());
            return userMapper.toResponse(savedUser);

        } catch (IllegalArgumentException e) {
            log.error("❌ Validation error: {}", e.getMessage());
            throw e; // Re-throw validation errors
        } catch (Exception e) {
            log.error("❌ Failed to create user: {}", e.getMessage(), e);
            throw new RuntimeException("User creation failed: " + e.getMessage(), e);
        }
    }

    // ✅ NEW: Validation method
    private void validateCreateUserCommand(CreateUserCommand command) {
        if (command.getType() == null) {
            throw new IllegalArgumentException("User type is required");
        }
        if (command.getName() == null || command.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (command.getPhoneNumber() == null || command.getPhoneNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number is required");
        }

        // Type-specific validation
        if (command.getType() == UserType.FARMER) {
            if (command.getAgriculturalType() == null) {
                throw new IllegalArgumentException("Agricultural type is required for farmers");
            }
            if (command.getLandArea() == null || command.getLandArea() <= 0) {
                throw new IllegalArgumentException("Valid land area is required for farmers");
            }
        }

        if (command.getType() == UserType.COOPERATIVE) {
            if (command.getCooperativeType() == null) {
                throw new IllegalArgumentException("Cooperative type is required");
            }
            if (command.getLegalRegistrationNumber() == null) {
                throw new IllegalArgumentException("Legal registration number is required");
            }
            if (command.getContactPerson() == null) {
                throw new IllegalArgumentException("Contact person is required");
            }
        }
    }





    @Override
    public UserResponse updateUser(String userId, UpdateUserCommand command) {
        log.info("Updating user: {}", userId);

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        UserStatus previousStatus = user.getStatus();

        // Update user fields
        userMapper.updateEntityFromCommand(command, user);

        // Handle status change
        if (command.getStatus() != null && command.getStatus() != previousStatus) {
            user.setStatus(command.getStatus());
            try {
                publishStatusChangeEvent(user, previousStatus, command.getStatus());
            } catch (Exception e) {
                log.warn("Failed to publish status change event: {}", e.getMessage());
            }
        }

        User updatedUser = userRepository.save(user);

        // Publish update event
        try {
            publishUserUpdatedEvent(updatedUser, previousStatus);
        } catch (Exception e) {
            log.warn("Failed to publish user updated event: {}", e.getMessage());
        }

        return userMapper.toResponse(updatedUser);
    }

    @Override
    public UserResponse updateUserStatus(String userId, String status) {
        log.info("Updating status for user: {} to {}", userId, status);

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        UserStatus previousStatus = user.getStatus();
        UserStatus newStatus = UserStatus.valueOf(status.toUpperCase());

        user.setStatus(newStatus);
        User updatedUser = userRepository.save(user);

        try {
            publishStatusChangeEvent(user, previousStatus, newStatus);
        } catch (Exception e) {
            log.warn("Failed to publish status change event: {}", e.getMessage());
        }

        return userMapper.toResponse(updatedUser);
    }

    @Override
    public void deleteUser(String userId) {
        log.info("Deleting user: {}", userId);

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        user.setStatus(UserStatus.DELETED);
        userRepository.save(user);

        log.info("User soft deleted: {}", userId);
    }

    @Override
    public UserResponse generateQRCode(String userId, String purpose) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        String qrData = qrCodeService.generateQRData(user, purpose);
        user.setQrCodeData(qrData);

        User updatedUser = userRepository.save(user);
        return userMapper.toResponse(updatedUser);
    }

    @Override
    public UserResponse uploadProfilePicture(String userId, byte[] imageData, String contentType) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        String filePath = fileStorageUtil.storeProfilePicture(userId, imageData, contentType);

        if (user.getProfile() == null) {
            user.setProfile(new Profile());
        }
        user.getProfile().setProfilePicturePath(filePath);

        User updatedUser = userRepository.save(user);
        return userMapper.toResponse(updatedUser);
    }

    // ===== PRIVATE HELPER METHODS =====

    private String generateDefaultPassword(UserType type) {
        String prefix = switch (type) {
            case COOPERATIVE -> "Coop";
            case FARMER -> "Farmer";
            case GOVERNMENT -> "Gov";
        };
        int year = Year.now().getValue();
        return prefix + year + "@Agribind";
    }

    private void sendWelcomeNotifications(User user, String password) {
        try {
            WelcomeNotificationRequest request = WelcomeNotificationRequest.builder()
                    .userId(user.getUserId())
                    .userType(user.getType().name())
                    .name(user.getName())
                    .username(user.getEmail()) // Use email as username
                    .phoneNumber(user.getPhoneNumber())
                    .email(user.getEmail())
                    .temporaryPassword(password) // Send plain password for notification only
                    .preferredLanguage(user.getProfile() != null ? user.getProfile().getPreferredLanguage() : "fr")
                    .timestamp(LocalDateTime.now())
                    .sendSms(true)
                    .sendEmail(true)
                    .priority("HIGH")
                    .build();

            notificationServiceClient.sendWelcomeNotification(request);

            log.info("Welcome notification sent for user: {}", user.getUserId());

        } catch (Exception e) {
            log.error("Failed to send welcome notifications for user: {}", user.getUserId(), e);
            // Don't throw exception - notification failure shouldn't block user creation
        }
    }

    private void publishUserCreatedEvent(User user) {
        UserCreatedEvent event = new UserCreatedEvent(
                user.getUserId(),
                user.getType(),
                user.getName(),
                user.getPhoneNumber()
        );
        event.setEmail(user.getEmail());
        event.setRegion(user.getAddress() != null ? user.getAddress().getRegion().name() : null);
        event.setPreferredLanguage(
                user.getProfile() != null ? user.getProfile().getPreferredLanguage() : "fr"
        );
        userEventPublisher.publishUserCreated(event);
    }

    private void publishStatusChangeEvent(User user, UserStatus previousStatus, UserStatus newStatus) {
        UserStatusChangedEvent event = new UserStatusChangedEvent(
                user.getUserId(),
                user.getType(),
                user.getName(),
                previousStatus,
                newStatus
        );
        event.setChangedBy("system");
        userEventPublisher.publishUserStatusChanged(event);
    }

    private void publishUserUpdatedEvent(User user, UserStatus previousStatus) {
        UserUpdatedEvent event = new UserUpdatedEvent(
                user.getUserId(),
                user.getType(),
                user.getName()
        );
        event.setPreviousStatus(previousStatus);
        event.setNewStatus(user.getStatus());
        event.setUpdatedBy("system");
        userEventPublisher.publishUserUpdated(event);
    }

    private User createUserByType(CreateUserCommand command) {
        return switch (command.getType()) {
            case FARMER -> createFarmer(command);
            case COOPERATIVE -> createCooperative(command);
            case GOVERNMENT -> createGovernmentOfficial(command);
        };
    }

    // ✅ FIXED: Farmer creation with proper null handling
    private Farmer createFarmer(CreateUserCommand command) {
        Farmer farmer = new Farmer();
        populateCommonFields(farmer, command);

        // ✅ Agricultural type (required)
        if (command.getAgriculturalType() != null) {
            try {
                farmer.setAgriculturalType(
                        AgriculturalType.valueOf(command.getAgriculturalType().toUpperCase())
                );
            } catch (IllegalArgumentException e) {
                log.error("Invalid agricultural type: {}", command.getAgriculturalType());
                throw new IllegalArgumentException("Invalid agricultural type");
            }
        }

        // ✅ Crop types (optional)
        if (command.getCropTypes() != null && command.getCropTypes().length > 0) {
            Set<CropType> crops = new HashSet<>();
            for (String cropStr : command.getCropTypes()) {
                try {
                    crops.add(CropType.valueOf(cropStr.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid crop type: {}", cropStr);
                }
            }
            farmer.setCropTypes(crops);
        }

        // ✅ Livestock types (optional)
        if (command.getLivestockTypes() != null && command.getLivestockTypes().length > 0) {
            Set<LivestockType> livestock = new HashSet<>();
            for (String livestockStr : command.getLivestockTypes()) {
                try {
                    livestock.add(LivestockType.valueOf(livestockStr.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid livestock type: {}", livestockStr);
                }
            }
            farmer.setLivestockTypes(livestock);
        }

        // ✅ Farm details
        FarmDetails farmDetails = new FarmDetails();
        farmDetails.setTotalLandArea(command.getLandArea());
        farmDetails.setCultivatedArea(command.getLandArea()); // Default to total
        farmDetails.setOwnsLand(true); // Default
        farmer.setFarmDetails(farmDetails);

        return farmer;
    }

    private Cooperative createCooperative(CreateUserCommand command) {
        Cooperative cooperative = new Cooperative();
        populateCommonFields(cooperative, command);

        if (command.getCooperativeType() != null) {
            cooperative.setCooperativeType(
                    CooperativeType.valueOf(
                            command.getCooperativeType().toUpperCase()
                    )
            );
        }

        cooperative.setOperatingRegion(command.getRegion());
        cooperative.setLegalRegistrationNumber(command.getLegalRegistrationNumber());
        cooperative.setEstablishmentYear(command.getEstablishmentYear());
        cooperative.setContactPerson(command.getContactPerson());

        CooperativeDetails details = new CooperativeDetails();
        cooperative.setCooperativeDetails(details);

        return cooperative;
    }

    private GovernmentOfficial createGovernmentOfficial(CreateUserCommand command) {
        GovernmentOfficial government = new GovernmentOfficial();
        populateCommonFields(government, command);

        if (command.getGovernmentRole() != null) {
            government.setRole(
                    GovernmentRole.valueOf(
                            command.getGovernmentRole().toUpperCase()
                    )
            );
        }

        government.setAssignedRegion(command.getRegion());
        government.setDepartment(command.getDepartmentName());
        government.setEmployeeId(command.getEmployeeId());

        GovernmentDetails details = new GovernmentDetails();
        government.setGovernmentDetails(details);

        return government;
    }

    // ✅ FIXED: Common fields population with null safety
    private void populateCommonFields(User user, CreateUserCommand command) {
        user.setType(command.getType());
        user.setName(command.getName());
        user.setEmail(command.getEmail());
        user.setPhoneNumber(command.getPhoneNumber());
        user.setStatus(UserStatus.ACTIVE);
        user.setNotes(command.getNotes());

        // ✅ Create address ONLY if region is provided
        if (command.getRegion() != null) {
            Address address = new Address();
            address.setRegion(command.getRegion());
            address.setDepartment(command.getDepartment());
            address.setDistrict(command.getDistrict());
            address.setVillage(command.getVillage());
            address.setGpsCoordinates(command.getGpsCoordinates());
            user.setAddress(address);
        }
    }

    private String generateUserId(UserType type) {
        String prefix = switch (type) {
            case FARMER -> "F";
            case COOPERATIVE -> "C";
            case GOVERNMENT -> "G";
        };

        String timestamp = String.valueOf(System.currentTimeMillis() % 100000);
        return prefix + timestamp;
    }

    private String generateRegistrationNumber() {
        return "REG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    // ✅ ADDITIONAL SECURITY METHODS

    /**
     * Verify if provided password matches the stored hash
     */
    public boolean verifyPassword(String userId, String plainPassword) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        return passwordEncoder.matches(plainPassword, user.getPasswordHash());
    }

    /**
     * Change user password with proper encryption
     */
    public UserResponse changePassword(String userId, String newPassword) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        String encryptedPassword = passwordEncoder.encode(newPassword);
        user.setPasswordHash(encryptedPassword);
        user.setFirstLogin(false); // User has changed their password

        User updatedUser = userRepository.save(user);
        log.info("Password changed successfully for user: {}", userId);

        return userMapper.toResponse(updatedUser);
    }

    /**
     * Reset user password to a new temporary password
     */
    public UserResponse resetPassword(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        String temporaryPassword = generateTemporaryPassword();
        String encryptedPassword = passwordEncoder.encode(temporaryPassword);
        user.setPasswordHash(encryptedPassword);
        user.setFirstLogin(true); // Force password change on next login

        User updatedUser = userRepository.save(user);

        // Send the temporary password via notification
        try {
            sendPasswordResetNotification(updatedUser, temporaryPassword);
        } catch (Exception e) {
            log.error("Failed to send password reset notification for user: {}", userId, e);
        }

        log.info("Password reset successfully for user: {}", userId);
        return userMapper.toResponse(updatedUser);
    }

    private String generateTemporaryPassword() {
        // Generate a secure random temporary password
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            int index = (int) (Math.random() * chars.length());
            sb.append(chars.charAt(index));
        }
        return "Temp" + sb.toString() + "!";
    }

    private void sendPasswordResetNotification(User user, String temporaryPassword) {
        try {
            WelcomeNotificationRequest request = WelcomeNotificationRequest.builder()
                    .userId(user.getUserId())
                    .userType(user.getType().name())
                    .name(user.getName())
                    .username(user.getEmail())
                    .phoneNumber(user.getPhoneNumber())
                    .email(user.getEmail())
                    .temporaryPassword(temporaryPassword)
                    .preferredLanguage(user.getProfile() != null ? user.getProfile().getPreferredLanguage() : "fr")
                    .timestamp(LocalDateTime.now())
                    .sendSms(true)
                    .sendEmail(true)
                    .priority("HIGH")
                    .build();

            notificationServiceClient.sendWelcomeNotification(request);

            log.info("Password reset notification sent for user: {}", user.getUserId());

        } catch (Exception e) {
            log.error("Failed to send password reset notification for user: {}", user.getUserId(), e);
            throw e; // Re-throw for password reset operation
        }
    }
}