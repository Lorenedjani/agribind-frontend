package cm.agribind.auth.exception;

public class AccountLockedException extends BusinessException {
    public AccountLockedException(String message) {
        super(message, "ACCOUNT_LOCKED");
    }
}
