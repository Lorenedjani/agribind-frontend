package cm.agribind.usermanagement.util;

import cm.agribind.usermanagement.enums.Region;
import lombok.experimental.UtilityClass;

import java.util.regex.Pattern;

@UtilityClass
public class ValidationUtil {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9. ()-]{7,25}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-ZÀ-ÿ\\s.'-]{2,100}$");

    public static boolean isValidPhoneNumber(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    public static boolean isValidEmail(String email) {
        return email == null || email.trim().isEmpty() || EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidName(String name) {
        return name != null && NAME_PATTERN.matcher(name).matches();
    }

    public static boolean isValidRegion(String region) {
        if (region == null) return true;
        try {
            Region.valueOf(region.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static boolean isValidLandArea(Double area) {
        return area == null || (area >= 0.1 && area <= 1000.0); // 0.1 to 1000 hectares
    }

    public static String sanitizePhoneNumber(String phone) {
        if (phone == null) return null;
        return phone.replaceAll("[^0-9+]", "");
    }

    public static String sanitizeName(String name) {
        if (name == null) return null;
        return name.trim().replaceAll("\\s+", " ");
    }
}