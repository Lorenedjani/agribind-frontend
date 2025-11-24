package cm.agribind.usermanagement;

import cm.agribind.usermanagement.dto.command.CreateUserCommand;
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
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@SpringBootApplication
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
        createDefaultCooperativeManagerWithEmail();

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

    private void createDefaultCooperativeManagerWithEmail() {
        String phone = "+237694334198";
        String email = "paulelorene@gmail.com";
        String name = "Lorene Djani";

        if (!userRepository.existsByPhoneNumber(phone)) {
            CreateUserCommand command = new CreateUserCommand();
            command.setType(UserType.COOPERATIVE);
            command.setName(name);
            command.setEmail(email);
            command.setPhoneNumber(phone);
            command.setRegion(Region.CENTRE);
            command.setDepartment("Mfoundi");
            command.setDistrict("Yaoundé");
            command.setVillage("Nkolbisson");
            command.setCooperativeType("PRODUCTION");
            command.setLegalRegistrationNumber("COOP-DEFAULT-001");
            command.setEstablishmentYear(2024);
            command.setContactPerson(name);
            command.setPreferredLanguage("fr");

            try {
                userCommandService.createUser(command);
                log.info("✅ Default cooperative manager created and email notification sent: {}", name);
            } catch (Exception e) {
                log.error("❌ Failed to create/send credentials for default cooperative manager: {}", e.getMessage());
                log.debug("Detailed error:", e);
            }
        } else {
            log.info("ℹ️ Default cooperative manager already exists.");
        }
    }
}