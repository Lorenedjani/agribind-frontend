package cm.agribind.usermanagement.exception;

public class FarmerNotFoundException extends RuntimeException {

    public FarmerNotFoundException(String message) {
        super(message);
    }

    public FarmerNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public static FarmerNotFoundException byId(String farmerId) {
        return new FarmerNotFoundException("Farmer not found with ID: " + farmerId);
    }
}