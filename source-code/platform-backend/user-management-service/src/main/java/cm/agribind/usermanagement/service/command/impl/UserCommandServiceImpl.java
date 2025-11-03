package cm.agribind.usermanagement.service.command.impl;

import cm.agribind.usermanagement.dto.command.CreateUserCommand;
import cm.agribind.usermanagement.dto.command.UpdateUserCommand;
import cm.agribind.usermanagement.dto.event.UserCreatedEvent;
import cm.agribind.usermanagement.dto.event.UserStatusChangedEvent;
import cm.agribind.usermanagement.dto.event.UserUpdatedEvent;
import cm.agribind.usermanagement.dto.response.UserResponse;
import cm.agribind.usermanagement.entity.*;
import cm.agribind.usermanagement.enums.UserStatus;
import cm.agribind.usermanagement.enums.UserType;
import cm.agribind.usermanagement.exception.UserNotFoundException;
import cm.agribind.usermanagement.integration.event.UserEventPublisher;
import cm.agribind.usermanagement.mapper.UserMapper;
import cm.agribind.usermanagement.repository.UserRepository;
import cm.agribind.usermanagement.service.command.UserCommandService;
import cm.agribind.usermanagement.util.FileStorageUtil;
import cm.agribind.usermanagement.util.QRCodeGenerator;
import cm.agribind.usermanagement.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserCommandServiceImpl implements UserCommandService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserEventPublisher userEventPublisher;
    private final QRCodeGenerator qrCodeGenerator;
    private final FileStorageUtil fileStorageUtil;
    private final SecurityUtil securityUtil;

    @Override
    public UserResponse createUser(CreateUserCommand command) {
        log.info("Creating user of type: {}", command.getType());

        // Validate phone number uniqueness
        if (userRepository.existsByPhoneNumber(command.getPhoneNumber())) {
            throw new IllegalArgumentException("Phone number already exists: " + command.getPhoneNumber());
        }

        // Create user entity based on type
        User user = createUserByType(command);

        // Generate user ID and registration number
        String userId = generateUserId(command.getType());
        user.setUserId(userId);
        user.setRegistrationNumber(generateRegistrationNumber());

        // Create profile
        Profile profile = new Profile();
        profile.setPreferredLanguage(command.getPreferredLanguage());
        user.setProfile(profile);

        // Generate QR code data
        String qrData = qrCodeGenerator.generateRegistrationData(user);
        user.setQrCodeData(qrData);

        // Save user
        User savedUser = userRepository.save(user);
        log.info("User created successfully: {}", savedUser.getUserId());

        // Publish user created event
        UserCreatedEvent event = new UserCreatedEvent(
                savedUser.getUserId(),
                savedUser.getType(),
                savedUser.getName(),
                savedUser.getPhoneNumber()
        );
        event.setRegion(savedUser.getAddress() != null ? savedUser.getAddress().getRegion().name() : null);
        event.setPreferredLanguage(command.getPreferredLanguage());
        userEventPublisher.publishUserCreated(event);

        return userMapper.toResponse(savedUser);
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

            // Publish status change event
            UserStatusChangedEvent statusEvent = new UserStatusChangedEvent(
                    user.getUserId(),
                    user.getType(),
                    user.getName(),
                    previousStatus,
                    command.getStatus()
            );
            statusEvent.setChangedBy(securityUtil.getCurrentUsername());
            userEventPublisher.publishUserStatusChanged(statusEvent);
        }

        User updatedUser = userRepository.save(user);

        // Publish user updated event
        UserUpdatedEvent updateEvent = new UserUpdatedEvent(
                updatedUser.getUserId(),
                updatedUser.getType(),
                updatedUser.getName()
        );
        updateEvent.setPreviousStatus(previousStatus);
        updateEvent.setNewStatus(updatedUser.getStatus());
        updateEvent.setUpdatedBy(securityUtil.getCurrentUsername());
        userEventPublisher.publishUserUpdated(updateEvent);

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

        // Publish status change event
        UserStatusChangedEvent event = new UserStatusChangedEvent(
                userId,
                user.getType(),
                user.getName(),
                previousStatus,
                newStatus
        );
        event.setChangedBy(securityUtil.getCurrentUsername());
        userEventPublisher.publishUserStatusChanged(event);

        return userMapper.toResponse(updatedUser);
    }

    @Override
    public void deleteUser(String userId) {
        log.info("Deleting user: {}", userId);

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        // Soft delete by setting status to DELETED
        user.setStatus(UserStatus.DELETED);
        userRepository.save(user);

        log.info("User soft deleted: {}", userId);
    }

    @Override
    public UserResponse generateQRCode(String userId, String purpose) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        String qrData = qrCodeGenerator.generateQRData(user, purpose);
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

    private User createUserByType(CreateUserCommand command) {
        return switch (command.getType()) {
            case FARMER -> createFarmer(command);
            case COOPERATIVE -> createCooperative(command);
            case GOVERNMENT -> createGovernmentOfficial(command);
        };
    }

    private Farmer createFarmer(CreateUserCommand command) {
        Farmer farmer = new Farmer();
        userMapper.toEntity(command); // This will set common fields

        // Set farmer-specific fields
        if (command.getAgriculturalType() != null) {
            farmer.setAgriculturalType(cm.agribind.usermanagement.enums.AgriculturalType.valueOf(
                    command.getAgriculturalType().toUpperCase()));
        }

        // Additional farmer initialization would go here

        return farmer;
    }

    private Cooperative createCooperative(CreateUserCommand command) {
        Cooperative cooperative = new Cooperative();
        userMapper.toEntity(command); // This will set common fields

        // Set cooperative-specific fields
        if (command.getCooperativeType() != null) {
            cooperative.setCooperativeType(cm.agribind.usermanagement.enums.CooperativeType.valueOf(
                    command.getCooperativeType().toUpperCase()));
        }
        cooperative.setLegalRegistrationNumber(command.getLegalRegistrationNumber());
        cooperative.setEstablishmentYear(command.getEstablishmentYear());
        cooperative.setContactPerson(command.getContactPerson());

        return cooperative;
    }

    private GovernmentOfficial createGovernmentOfficial(CreateUserCommand command) {
        GovernmentOfficial government = new GovernmentOfficial();
        userMapper.toEntity(command); // This will set common fields

        // Set government-specific fields
        if (command.getGovernmentRole() != null) {
            government.setRole(cm.agribind.usermanagement.enums.GovernmentRole.valueOf(
                    command.getGovernmentRole().toUpperCase()));
        }
        government.setDepartment(command.getDepartmentName());
        government.setEmployeeId(command.getEmployeeId());

        return government;
    }

    private String generateUserId(UserType type) {
        String prefix = switch (type) {
            case FARMER -> "F";
            case COOPERATIVE -> "C";
            case GOVERNMENT -> "G";
        };

        // In a real implementation, this would query the database for the next sequence
        String timestamp = String.valueOf(System.currentTimeMillis() % 100000);
        return prefix + timestamp;
    }

    private String generateRegistrationNumber() {
        return "REG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}