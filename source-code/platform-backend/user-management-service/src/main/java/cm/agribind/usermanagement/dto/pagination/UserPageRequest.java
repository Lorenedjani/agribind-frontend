package cm.agribind.usermanagement.dto.pagination;

import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Data
public class UserPageRequest {

    private Integer page = 0;
    private Integer size = 20;
    private String sortBy = "createdAt";
    private String sortDirection = "DESC";

    public Pageable toPageable() {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        return PageRequest.of(page, size, sort);
    }

    public Pageable toPageable(String defaultSortField) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), defaultSortField);
        return PageRequest.of(page, size, sort);
    }
}