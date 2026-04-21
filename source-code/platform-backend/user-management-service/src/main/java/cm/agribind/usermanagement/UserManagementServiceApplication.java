package cm.agribind.usermanagement;

import cm.agribind.usermanagement.dto.command.CreateUserCommand;
import cm.agribind.usermanagement.dto.response.UserResponse;
import cm.agribind.usermanagement.enums.CooperativeType;
import cm.agribind.usermanagement.enums.Region;
import cm.agribind.usermanagement.enums.UserType;
import cm.agribind.usermanagement.repository.UserRepository;
import cm.agribind.usermanagement.service.command.UserCommandService;
import cm.agribind.usermanagement.util.FileStorageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
// Exclude Kafka unless you enable agribind.notifications.use-kafka and register Kafka beans manually.
@SpringBootApplication(exclude = KafkaAutoConfiguration.class)
@EnableJpaAuditing
@EnableFeignClients
@EnableAsync
@EnableScheduling
public class UserManagementServiceApplication implements CommandLineRunner {

    private final FileStorageUtil fileStorageUtil;
    private final UserCommandService userCommandService;
    private final UserRepository userRepository;

    public UserManagementServiceApplication(FileStorageUtil fileStorageUtil,
                                            UserCommandService userCommandService,
                                            UserRepository userRepository) {
        this.fileStorageUtil = fileStorageUtil;
        this.userCommandService = userCommandService;
        this.userRepository = userRepository;
    }

    public static void main(String[] args) {
        SpringApplication.run(UserManagementServiceApplication.class, args);
    }

    @Override
    public void run(String... args) {
        log.info("AgriBind User Management Service starting...");

        // Initialize file storage
        fileStorageUtil.initializeStorage();

        // Create default cooperative manager
        createDefaultCooperativeAdminAndManager();
        // create defaulte government admin
        createDefaultGovernmentOfficialWithEmail();

        log.info("""
            ----------------------------------------------------------
            AgriBind User Management Service Started Successfully!
            Port: 8082
            Database: MySQL
            Features:
            - User Management (Farmers, Cooperatives, Government)
            - QR Code Registration & Login
            - Advanced Filtering & Search
            - Dashboard Analytics
            - Multi-format Export (CSV, Excel, PDF)
            - Event-driven Architecture
            ----------------------------------------------------------
            """);
    }

    private void createDefaultCooperativeAdminAndManager() {
        String adminPhone = "+237694334198";
        String adminEmail = "paulelorene@gmail.com";
        String adminName = "Lorene Djani (Coop Admin)";

        UserResponse adminResponse = null;

        if (!userRepository.existsByPhoneNumber(adminPhone)) {
            CreateUserCommand command = new CreateUserCommand();
            command.setType(UserType.COOPERATIVE);
            command.setName(adminName);
            command.setEmail(adminEmail);
            command.setPhoneNumber(adminPhone);
            command.setRegion(Region.CENTRE);
            command.setDepartment("Mfoundi");
            command.setDistrict("Yaoundé");
            command.setVillage("Nkolbisson");
            command.setCooperativeType("PRODUCTION");
            command.setLegalRegistrationNumber("COOP-DEFAULT-001");
            command.setEstablishmentYear(2024);
            command.setContactPerson(adminName);
            command.setPreferredLanguage("fr");

            try {
                adminResponse = userCommandService.createUser(command);
                log.info("✅ Default cooperative admin created and email notification sent: {}", adminName);
            } catch (Exception e) {
                log.error("❌ Failed to create/send credentials for default cooperative admin: {}", e.getMessage());
                log.debug("Detailed error:", e);
            }
        } else {
            log.info("ℹ️ Default cooperative admin already exists.");
            userRepository.findByPhoneNumber(adminPhone).ifPresent(user -> {
                // To fetch ID for the manager creation step
                var mockCommand = new CreateUserCommand();
                mockCommand.setCooperativeId(user.getId());
                createDefaultManagerUnderCooperative(user.getId());
            });
            return;
        }

        if (adminResponse != null && adminResponse.getId() != null) {
            createDefaultManagerUnderCooperative(adminResponse.getId());
        }
    }

    private void createDefaultManagerUnderCooperative(Long cooperativeId) {
        String managerPhone = "+237699999999";
        String managerEmail = "manager.coop@agribind.cm";
        String managerName = "Default Coop Manager";

        if (!userRepository.existsByPhoneNumber(managerPhone)) {
            CreateUserCommand mCommand = new CreateUserCommand();
            mCommand.setType(UserType.COOPERATIVE_MANAGER);
            mCommand.setName(managerName);
            mCommand.setEmail(managerEmail);
            mCommand.setPhoneNumber(managerPhone);
            mCommand.setRegion(Region.CENTRE);
            mCommand.setPreferredLanguage("en");
            mCommand.setCooperativeId(cooperativeId);

            try {
                userCommandService.createUser(mCommand);
                log.info("✅ Default cooperative manager created: {}", managerName);
            } catch (Exception e) {
                log.error("❌ Failed to create default cooperative manager: {}", e.getMessage());
            }
        } else {
            log.info("ℹ️ Default cooperative manager already exists.");
        }
    }

    private void createDefaultGovernmentOfficialWithEmail() {
        String phone = "+237699000001";
        String email = "gov.admin@agribind.cm";
        String name = "Admin Government";

        if (!userRepository.existsByPhoneNumber(phone)) {
            CreateUserCommand command = new CreateUserCommand();
            command.setType(UserType.GOVERNMENT);
            command.setName(name);
            command.setEmail(email);
            command.setPhoneNumber(phone);
            command.setRegion(Region.CENTRE);
            command.setDepartment("Ministry of Agriculture");
            command.setDistrict("Yaoundé");
            command.setVillage("Centre Administratif");
            command.setGovernmentRole("REGIONAL_COORDINATOR");
            command.setEmployeeId("GOV-ADMIN-001");
            command.setDepartmentName("Ministry of Agriculture");
            command.setPreferredLanguage("fr");

            try {
                userCommandService.createUser(command);
                log.info("✅ Default government official created: {}", name);
            } catch (Exception e) {
                log.error("❌ Failed to create default government official: {}", e.getMessage());
            }
        } else {
            log.info("ℹ️ Default government official already exists.");
        }
    }
}