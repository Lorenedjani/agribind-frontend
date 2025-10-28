package cm.agribind.auth.serviceimpl;

import cm.agribind.auth.config.JwtService;
import cm.agribind.auth.entity.Role;
import cm.agribind.auth.entity.UserAccount;
import cm.agribind.auth.repository.UserAccountRepository;
import cm.agribind.auth.service.AuthService;
import cm.agribind.auth.service.EventPublisher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserAccountRepository repo;
    private final JwtService jwt;
    private final BCryptPasswordEncoder encoder;
    private final EventPublisher events;

    public AuthServiceImpl(UserAccountRepository repo,
                           JwtService jwt,
                           BCryptPasswordEncoder encoder,
                           EventPublisher events) {
        this.repo = repo;
        this.jwt = jwt;
        this.encoder = encoder;
        this.events = events;
    }

    @Override
    public String login(String username, String password) {
        UserAccount u = repo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!encoder.matches(password, u.getPasswordHash()))
            throw new RuntimeException("Invalid credentials");
        return jwt.generateToken(u.getId(), u.getRole().name());
    }

    @Override
    public UserAccount createManager(String username, String email, String phone, String rawPassword) {
        UserAccount u = new UserAccount();
        u.setUsername(username);
        u.setEmail(email);
        u.setPhone(phone);
        u.setRole(Role.COOPERATIVE_MANAGER);
        u.setPasswordHash(encoder.encode(rawPassword));
        u.setIsFirstLogin(true);

        UserAccount saved = repo.save(u);
        events.publishEmail(saved, rawPassword);
        return saved;
    }

    @Override
    public UserAccount registerFarmer(String phone) {
        String regNum = "F-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        UserAccount farmer = new UserAccount();
        farmer.setUsername(regNum);
        farmer.setPhone(phone);
        farmer.setRole(Role.FARMER);
        farmer.setIsFirstLogin(false);

        UserAccount saved = repo.save(farmer);
        events.publishSms(saved, regNum);
        return saved;
    }

    @Override
    public void changePassword(String userId, String newPass) {
        UserAccount user = repo.findById(userId).orElseThrow();
        user.setPasswordHash(encoder.encode(newPass));
        user.setIsFirstLogin(false);
        repo.save(user);
    }
}
