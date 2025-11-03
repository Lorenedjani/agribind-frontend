package cm.agribind.auth.exception;

public class PasswordMismatchException extends BusinessException {
    public PasswordMismatchException(String message) {
        super(message, "PASSWORD_MISMATCH");
    }
}
