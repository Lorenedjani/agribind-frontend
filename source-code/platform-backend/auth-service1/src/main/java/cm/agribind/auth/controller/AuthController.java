package cm.agribind.auth.controller;

import cm.agribind.auth.entity.UserAccount;
import cm.agribind.auth.service.AuthService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password) {
        return auth.login(username, password);
    }

    @PostMapping("/create-manager")
    public UserAccount createManager(@RequestParam String username,
                                     @RequestParam String email,
                                     @RequestParam String phone,
                                     @RequestParam String password) {
        return auth.createManager(username, email, phone, password);
    }

    @PostMapping("/register-farmer")
    public UserAccount registerFarmer(@RequestParam String phone) {
        return auth.registerFarmer(phone);
    }

    @PostMapping("/change-password/{userId}")
    public void changePassword(@PathVariable String userId, @RequestParam String newPassword) {
        auth.changePassword(userId, newPassword);
    }
}
