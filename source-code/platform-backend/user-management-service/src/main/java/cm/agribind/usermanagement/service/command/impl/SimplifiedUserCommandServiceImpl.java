package cm.agribind.usermanagement.service.command.impl;

import ch.qos.logback.core.encoder.EchoEncoder;
import cm.agribind.usermanagement.dto.command.CreateUserCommand;
import cm.agribind.usermanagement.dto.command.UpdateUserCommand;
import cm.agribind.usermanagement.dto.event.*;
import cm.agribind.usermanagement.dto.response.UserResponse;
import cm.agribind.usermanagement.entity.*;
import cm.agribind.usermanagement.enums.*;
import cm.agribind.usermanagement.exception.UserNotFoundException;
import cm.agribind.usermanagement.integration.event.UserEventPublisher;
import cm.agribind.usermanagement.mapper.UserMapper;
import cm.agribind.usermanagement.repository.UserRepository;
import cm.agribind.usermanagement.service.QRCodeService;
import cm.agribind.usermanagement.service.command.UserCommandService;
import cm.agribind.usermanagement.util.FileStorageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.Arrays;
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

    @Override
    public UserResponse createUser(CreateUserCommand command) {
        log.info("Creating user of type: {} with name: {}", command.getType(), command.getName());

        try {
            // 1. Validate phone number uniqueness
            if (userRepository.existsByPhoneNumber(command.getPhoneNumber())) {
                throw new IllegalArgumentException("Phone number already exists: " + command.getPhoneNumber());
            }

            // Create user entity based on type
            User user = createUserByType(command);

            // Generate user ID and registration number
            String userId = generateUserId(command.getType());
            user.setUserId(userId);
            user.setRegistrationNumber(generateRegistrationNumber());

            // ✅ NEW: Generate and set default password
            String defaultPassword = generateDefaultPassword(command.getType());
            EchoEncoder<String> passwordEncoder = new EchoEncoder<>();
            String passwordHash = Arrays.toString(passwordEncoder.encode(defaultPassword));
            user.setPasswordHash(passwordHash);
            user.setFirstLogin(true);

            // Log the password (only in development!)
            log.info("Generated password for {}: {}", userId, defaultPassword);

            // Create profile
            Profile profile = new Profile();
            profile.setPreferredLanguage(command.getPreferredLanguage());
            user.setProfile(profile);

            // Save user
            User savedUser = userRepository.save(user);

            // TODO: Send password via SMS to user
            sendPasswordSMS(savedUser, defaultPassword);

            return userMapper.toResponse(savedUser);

        } catch (Exception e) {
            log.error("Failed to create user: {}", e.getMessage(), e);
            throw new RuntimeException("User creation failed: " + e.getMessage(), e);
        }
    }

    private String generateDefaultPassword(UserType type) {
        // Generate passwords like: Coop2025@Agribind, Farmer2025@Agribind
        String prefix = switch (type) {
            case COOPERATIVE -> "Coop";
            case FARMER -> "Farmer";
            case GOVERNMENT -> "Gov";
        };
        int year = Year.now().getValue();
        return prefix + year + "@Agribind";
    }

    private void sendPasswordSMS(User user, String password) {
        // TODO: Integrate with notification service
        log.info("Send SMS to {}: Your password is {}", user.getPhoneNumber(), password);
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

    private Farmer createFarmer(CreateUserCommand command) {
        Farmer farmer = new Farmer();
        populateCommonFields(farmer, command);

        if (command.getAgriculturalType() != null) {
            farmer.setAgriculturalType(
                    AgriculturalType.valueOf(
                            command.getAgriculturalType().toUpperCase()
                    )
            );
        }

        FarmDetails farmDetails = new FarmDetails();
        farmDetails.setTotalLandArea(command.getLandArea());
        farmDetails.setCultivatedArea(command.getLandArea());
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

    private void populateCommonFields(User user, CreateUserCommand command) {
        user.setType(command.getType());
        user.setName(command.getName());
        user.setEmail(command.getEmail());
        user.setPhoneNumber(command.getPhoneNumber());
        user.setStatus(UserStatus.ACTIVE);
        user.setNotes(command.getNotes());

        // Create address
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
}