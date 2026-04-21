package cm.agribind.usermanagement.dto.query;

import cm.agribind.usermanagement.enums.Region;
import cm.agribind.usermanagement.enums.UserStatus;
import cm.agribind.usermanagement.enums.UserType;
import lombok.Data;
import org.springframework.data.domain.Sort;

@Data
public class UserQuery {

    private UserType type;
    private UserStatus status;
    private Region region;
    private String searchTerm;
    private Integer page = 0;
    private Integer size = 20;
    private String sortBy = "createdAt";
    private Sort.Direction direction = Sort.Direction.DESC;
}