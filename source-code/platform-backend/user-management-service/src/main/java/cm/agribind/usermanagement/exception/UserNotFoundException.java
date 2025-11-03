package cm.agribind.usermanagement.exception;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public static UserNotFoundException byId(String userId) {
        return new UserNotFoundException("User not found with ID: " + userId);
    }

    public static UserNotFoundException byPhone(String phone) {
        return new UserNotFoundException("User not found with phone: " + phone);
    }

    public static UserNotFoundException byRegistration(String registration) {
        return new UserNotFoundException("User not found with registration: " + registration);
    }
}