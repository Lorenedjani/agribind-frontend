package cm.agribind.auth.exception;

public class InvalidTokenException extends BusinessException {
    public InvalidTokenException(String message) {
        super(message, "INVALID_TOKEN");
    }
}
