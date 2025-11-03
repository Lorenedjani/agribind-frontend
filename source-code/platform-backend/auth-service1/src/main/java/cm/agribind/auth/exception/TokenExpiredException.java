package cm.agribind.auth.exception;

public class TokenExpiredException extends BusinessException {
    public TokenExpiredException(String message) {
        super(message, "TOKEN_EXPIRED");
    }
}
