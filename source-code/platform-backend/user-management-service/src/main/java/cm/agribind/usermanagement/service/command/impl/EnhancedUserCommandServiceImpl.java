
package cm.agribind.usermanagement.service.command.impl;

import cm.agribind.usermanagement.dto.command.CreateUserCommand;
import cm.agribind.usermanagement.dto.command.UpdateUserCommand;
import cm.agribind.usermanagement.dto.event.*;
import cm.agribind.usermanagement.dto.response.UserResponse;
import cm.agribind.usermanagement.entity.*;
import cm.agribind.usermanagement.enums.UserStatus;
import cm.agribind.usermanagement.enums.UserType;
import cm.agribind.usermanagement.exception.UserNotFoundException;
import cm.agribind.usermanagement.integration.client.AuthServiceClient;
import cm.agribind.usermanagement.integration.client.NotificationServiceClient;
import cm.agribind.usermanagement.integration.event.UserEventPublisher;
import cm.agribind.usermanagement.mapper.UserMapper;
import cm.agribind.usermanagement.repository.UserRepository;
import cm.agribind.usermanagement.service.GoogleMapsService;
import cm.agribind.usermanagement.service.PasswordGenerationService;
import cm.agribind.usermanagement.service.QRCodeService;
import cm.agribind.usermanagement.service.command.UserCommandService;
import cm.agribind.usermanagement.util.FileStorageUtil;
import cm.agribind.usermanagement.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EnhancedUserCommandServiceImpl implements UserCommandService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserEventPublisher userEventPublisher;
    private final QRCodeService qrCodeService;
    private final FileStorageUtil fileStorageUtil;
    private final SecurityUtil securityUtil;
    private final AuthServiceClient authServiceClient;
    private final NotificationServiceClient notificationServiceClient;
    private final PasswordGenerationService passwordGenerationService;
    private final GoogleMapsService googleMapsService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse createUser(CreateUserCommand command) {
        log.info("Creating user of type: {} with name: {}", command.getType(), command.getName());

        // 1. Validate phone number uniqueness
        if (userRepository.existsByPhoneNumber(command.getPhoneNumber())) {
            throw new IllegalArgumentException("Phone number already exists: " + command.getPhoneNumber());
        }

        // 2. Geocode address if GPS coordinates not provided
        if (command.getGpsCoordinates() == null && command.getRegion() != null) {
            String fullAddress = buildFullAddress(command);
            GoogleMapsService.GeocodingResult geocoding = googleMapsService.geocodeAddress(fullAddress);
            if (geocoding != null) {
                command.setGpsCoordinates(
                        googleMapsService.formatGpsCoordinates(
                                geocoding.getLatitude(),
                                geocoding.getLongitude()
                        )
                );
                log.info("Geocoded address for {}: {}", command.getName(), command.getGpsCoordinates());
            }
        }

        // 3. Create user entity based on type
        User user = createUserByType(command);

        // 4. Generate user ID and registration number
        String userId = generateUserId(command.getType());
        user.setUserId(userId);
        user.setRegistrationNumber(generateRegistrationNumber());

        // 5. Create profile
        Profile profile = new Profile();
        profile.setPreferredLanguage(command.getPreferredLanguage());
        user.setProfile(profile);

        // 6. Generate QR code data
        String qrData = qrCodeService.generateRegistrationData(user);
        user.setQrCodeData(qrData);

        // 7. Save user
        User savedUser = userRepository.save(user);
        log.info("User created successfully: {}", savedUser.getUserId());

        // 8. Create auth user with generated password
        String defaultPassword = createAuthUserAccount(savedUser, command);

        // 9. Send notifications
        sendUserCreationNotifications(savedUser, defaultPassword, command);

        // 10. Publish user created event
        publishUserCreatedEvent(savedUser);

        return userMapper.toResponse(savedUser);
    }

    private String createAuthUserAccount(User user, CreateUserCommand command) {
        try {
            // Generate default password
            String defaultPassword = passwordGenerationService.generateDefaultPassword();
            String passwordHash = passwordEncoder.encode(defaultPassword);

            // Create auth user request
            AuthServiceClient.CreateAuthUserRequest authRequest = new AuthServiceClient.CreateAuthUserRequest();
            authRequest.setUserId(user.getUserId());
            authRequest.setUsername(user.getPhoneNumber()); // Use phone as username
            authRequest.setEmail(user.getEmail());
            authRequest.setPhoneNumber(user.getPhoneNumber());
            authRequest.setPasswordHash(passwordHash);
            authRequest.setRole(user.getType().name());

            if (user instanceof Farmer) {
                Farmer farmer = (Farmer) user;
                authRequest.setCooperativeId(
                        farmer.getCooperative() != null ?
                                farmer.getCooperative().getId().toString() : null
                );
            }

            authRequest.setPreferredLanguage(command.getPreferredLanguage());
            authRequest.setFirstLogin(true);

            // Call auth service
            AuthServiceClient.CreateAuthUserResponse authResponse =
                    authServiceClient.createAuthUser(authRequest);

            if (authResponse.isSuccess()) {
                log.info("Auth user created successfully for: {}", user.getUserId());
            } else {
                log.warn("Auth user creation failed: {}", authResponse.getMessage());
            }

            return defaultPassword;

        } catch (Exception e) {
            log.error("Failed to create auth user for: {}", user.getUserId(), e);
            // Don't fail the entire operation
            return null;
        }
    }

    private void sendUserCreationNotifications(User user, String defaultPassword, CreateUserCommand command) {
        try {
            String message = buildWelcomeMessage(user, defaultPassword, command);

            NotificationServiceClient.SmsNotificationRequest smsRequest =
                    new NotificationServiceClient.SmsNotificationRequest();
            smsRequest.setUserId(user.getUserId());
            smsRequest.setPhoneNumber(user.getPhoneNumber());
            smsRequest.setMessage(message);
            smsRequest.setType("WELCOME");
            smsRequest.setPriority("HIGH");

            notificationServiceClient.sendSMS(smsRequest);
            log.info("Welcome SMS sent to: {}", user.getPhoneNumber());

            // For farmers, send QR code details
            if (user instanceof Farmer) {
                sendFarmerQRCodeNotification((Farmer) user);
            }

        } catch (Exception e) {
            log.error("Failed to send notifications for user: {}", user.getUserId(), e);
            // Don't fail the operation
        }
    }

    private void sendFarmerQRCodeNotification(Farmer farmer) {
        try {
            String qrMessage = buildQRCodeMessage(farmer);

            NotificationServiceClient.SmsNotificationRequest qrSmsRequest =
                    new NotificationServiceClient.SmsNotificationRequest();
            qrSmsRequest.setUserId(farmer.getUserId());
            qrSmsRequest.setPhoneNumber(farmer.getPhoneNumber());
            qrSmsRequest.setMessage(qrMessage);
            qrSmsRequest.setType("QR_CODE");
            qrSmsRequest.setPriority("HIGH");

            notificationServiceClient.sendSMS(qrSmsRequest);
            log.info("QR code SMS sent to farmer: {}", farmer.getPhoneNumber());

        } catch (Exception e) {
            log.error("Failed to send QR code notification", e);
        }
    }

    private String buildWelcomeMessage(User user, String defaultPassword, CreateUserCommand command) {
        String language = command.getPreferredLanguage() != null ?
                command.getPreferredLanguage() : "fr";

        return switch (language.toLowerCase()) {
            case "fr" -> String.format(
                    "Bienvenue à AgriBind, %s! " +
                            "Votre compte a été créé avec succès.\n" +
                            "Numéro d'inscription: %s\n" +
                            "Mot de passe temporaire: %s\n" +
                            "Veuillez le changer lors de votre première connexion.",
                    user.getName(),
                    user.getRegistrationNumber(),
                    defaultPassword
            );
            case "en" -> String.format(
                    "Welcome to AgriBind, %s! " +
                            "Your account has been created successfully.\n" +
                            "Registration Number: %s\n" +
                            "Temporary Password: %s\n" +
                            "Please change it on first login.",
                    user.getName(),
                    user.getRegistrationNumber(),
                    defaultPassword
            );
            default -> String.format(
                    "Bienvenue à AgriBind! " +
                            "No. Inscription: %s\n" +
                            "Mot de passe: %s",
                    user.getRegistrationNumber(),
                    defaultPassword
            );
        };
    }

    private String buildQRCodeMessage(Farmer farmer) {
        String language = farmer.getProfile() != null &&
                farmer.getProfile().getPreferredLanguage() != null ?
                farmer.getProfile().getPreferredLanguage() : "fr";

        return switch (language.toLowerCase()) {
            case "fr" -> String.format(
                    "AgriBind - Code QR généré!\n" +
                            "Votre code QR personnel a été créé.\n" +
                            "Numéro: %s\n" +
                            "Utilisez-le pour une connexion rapide.\n" +
                            "Téléchargez via l'application mobile.",
                    farmer.getRegistrationNumber()
            );
            case "en" -> String.format(
                    "AgriBind - QR Code Generated!\n" +
                            "Your personal QR code has been created.\n" +
                            "Number: %s\n" +
                            "Use it for quick login.\n" +
                            "Download via mobile app.",
                    farmer.getRegistrationNumber()
            );
            default -> String.format(
                    "AgriBind QR Code: %s\n" +
                            "Utiliser pour connexion rapide.",
                    farmer.getRegistrationNumber()
            );
        };
    }

    private String buildFullAddress(CreateUserCommand command) {
        StringBuilder address = new StringBuilder();

        if (command.getVillage() != null) {
            address.append(command.getVillage()).append(", ");
        }
        if (command.getDistrict() != null) {
            address.append(command.getDistrict()).append(", ");
        }
        if (command.getDepartment() != null) {
            address.append(command.getDepartment()).append(", ");
        }
        if (command.getRegion() != null) {
            address.append(command.getRegion().name()).append(", Cameroon");
        }

        return address.toString();
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

    @Override
    public UserResponse updateUser(String userId, UpdateUserCommand command) {
        log.info("Updating user: {}", userId);

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        UserStatus previousStatus = user.getStatus();

        // Update GPS coordinates if address changed
        if (addressChanged(user, command)) {
            updateGpsCoordinates(user, command);
        }

        // Update user fields
        userMapper.updateEntityFromCommand(command, user);

        // Handle status change
        if (command.getStatus() != null && command.getStatus() != previousStatus) {
            user.setStatus(command.getStatus());
            publishStatusChangeEvent(user, previousStatus, command.getStatus());
        }

        User updatedUser = userRepository.save(user);

        // Publish update event
        publishUserUpdatedEvent(updatedUser, previousStatus);

        return userMapper.toResponse(updatedUser);
    }

    private boolean addressChanged(User user, UpdateUserCommand command) {
        if (user.getAddress() == null) return false;

        return (command.getRegion() != null && !command.getRegion().equals(user.getAddress().getRegion())) ||
                (command.getDepartment() != null && !command.getDepartment().equals(user.getAddress().getDepartment())) ||
                (command.getDistrict() != null && !command.getDistrict().equals(user.getAddress().getDistrict())) ||
                (command.getVillage() != null && !command.getVillage().equals(user.getAddress().getVillage()));
    }

    private void updateGpsCoordinates(User user, UpdateUserCommand command) {
        try {
            String fullAddress = buildFullAddressFromUpdate(user, command);
            GoogleMapsService.GeocodingResult geocoding = googleMapsService.geocodeAddress(fullAddress);

            if (geocoding != null && user.getAddress() != null) {
                user.getAddress().setGpsCoordinates(
                        googleMapsService.formatGpsCoordinates(
                                geocoding.getLatitude(),
                                geocoding.getLongitude()
                        )
                );
                log.info("Updated GPS coordinates for user: {}", user.getUserId());
            }
        } catch (Exception e) {
            log.error("Failed to update GPS coordinates", e);
        }
    }

    private String buildFullAddressFromUpdate(User user, UpdateUserCommand command) {
        Address address = user.getAddress();
        StringBuilder fullAddress = new StringBuilder();

        String village = command.getVillage() != null ? command.getVillage() :
                (address != null ? address.getVillage() : null);
        String district = command.getDistrict() != null ? command.getDistrict() :
                (address != null ? address.getDistrict() : null);
        String department = command.getDepartment() != null ? command.getDepartment() :
                (address != null ? address.getDepartment() : null);
        String region = command.getRegion() != null ? command.getRegion().name() :
                (address != null && address.getRegion() != null ? address.getRegion().name() : null);

        if (village != null) fullAddress.append(village).append(", ");
        if (district != null) fullAddress.append(district).append(", ");
        if (department != null) fullAddress.append(department).append(", ");
        if (region != null) fullAddress.append(region).append(", Cameroon");

        return fullAddress.toString();
    }

    private void publishStatusChangeEvent(User user, UserStatus previousStatus, UserStatus newStatus) {
        UserStatusChangedEvent event = new UserStatusChangedEvent(
                user.getUserId(),
                user.getType(),
                user.getName(),
                previousStatus,
                newStatus
        );
        event.setChangedBy(securityUtil.getCurrentUsername());
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
        event.setUpdatedBy(securityUtil.getCurrentUsername());
        userEventPublisher.publishUserUpdated(event);
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

        publishStatusChangeEvent(user, previousStatus, newStatus);

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
                    cm.agribind.usermanagement.enums.AgriculturalType.valueOf(
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
                    cm.agribind.usermanagement.enums.CooperativeType.valueOf(
                            command.getCooperativeType().toUpperCase()
                    )
            );
        }

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
                    cm.agribind.usermanagement.enums.GovernmentRole.valueOf(
                            command.getGovernmentRole().toUpperCase()
                    )
            );
        }

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