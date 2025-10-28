package cm.agribind.auth.service;

import cm.agribind.auth.entity.UserAccount;

public interface AuthService {

    /** Authenticate and return JWT token */
    String login(String username, String password);

    /** Create a cooperative manager or other web user (admin creates them) */
    UserAccount createManager(String username, String email, String phone, String rawPassword);

    /** Register a farmer (by cooperative manager/admin) and send SMS with registration number */
    UserAccount registerFarmer(String phone);

    /** Change password and mark first login complete */
    void changePassword(String userId, String newPass);
}
