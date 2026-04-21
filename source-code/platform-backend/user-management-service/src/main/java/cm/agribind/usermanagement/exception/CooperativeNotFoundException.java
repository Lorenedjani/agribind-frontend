package cm.agribind.usermanagement.exception;

public class CooperativeNotFoundException extends RuntimeException {

    public CooperativeNotFoundException(String message) {
        super(message);
    }

    public CooperativeNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public static CooperativeNotFoundException byId(String cooperativeId) {
        return new CooperativeNotFoundException("Cooperative not found with ID: " + cooperativeId);
    }
}