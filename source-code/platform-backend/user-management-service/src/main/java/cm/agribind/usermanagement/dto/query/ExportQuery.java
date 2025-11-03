package cm.agribind.usermanagement.dto.query;

import cm.agribind.usermanagement.enums.Region;
import cm.agribind.usermanagement.enums.UserStatus;
import cm.agribind.usermanagement.enums.UserType;
import lombok.Data;

@Data
public class ExportQuery {

    private UserType type;
    private UserStatus status;
    private Region region;
    private String format; // CSV, EXCEL, PDF
    private Boolean includeProfile = false;
    private Boolean includeAgriculturalData = true;
    private String[] columns; // Specific columns to export
}