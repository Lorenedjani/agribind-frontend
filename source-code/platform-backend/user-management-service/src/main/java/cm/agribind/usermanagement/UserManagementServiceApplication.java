package cm.agribind.usermanagement;

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

    public UserManagementServiceApplication(FileStorageUtil fileStorageUtil,
                                            UserCommandService userCommandService) {
        this.fileStorageUtil = fileStorageUtil;
        this.userCommandService = userCommandService;
    }

    public static void main(String[] args) {
        SpringApplication.run(UserManagementServiceApplication.class, args);
    }

    @Override
    public void run(String... args) {
        log.info("AgriBind User Management Service starting...");

        // Initialize file storage
        fileStorageUtil.initializeStorage();

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
}