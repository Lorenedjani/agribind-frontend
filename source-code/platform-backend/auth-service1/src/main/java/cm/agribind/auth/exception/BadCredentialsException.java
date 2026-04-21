package cm.agribind.auth.exception;

public class BadCredentialsException extends BusinessException {
    public BadCredentialsException(String message) {
        super(message, "BAD_CREDENTIALS");
    }
}
