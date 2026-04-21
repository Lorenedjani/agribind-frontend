package cm.agribind.usermanagement.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

public class PaginationUtil {

    public static Pageable createPageable(Integer page, Integer size, String sortBy, String sortDirection) {
        if (page == null) page = 0;
        if (size == null) size = 20;
        if (sortBy == null) sortBy = "createdAt";
        if (sortDirection == null) sortDirection = "DESC";

        // Fixed: Use proper PageRequest creation
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Sort sort = Sort.by(direction, sortBy);
        return PageRequest.of(page, size, sort);
    }

    public static <T> List<T> getPageContent(List<T> fullList, int page, int size) {
        if (fullList == null || fullList.isEmpty()) {
            return List.of();
        }

        int start = page * size;
        if (start >= fullList.size()) {
            return List.of();
        }

        int end = Math.min(start + size, fullList.size());
        return fullList.subList(start, end);
    }

    public static int calculateTotalPages(long totalElements, int pageSize) {
        if (pageSize <= 0) return 0;
        return (int) Math.ceil((double) totalElements / pageSize);
    }

    public static boolean isValidPage(int page, int totalPages) {
        return page >= 0 && page < totalPages;
    }

    // Helper method to create Pageable with default values
    public static Pageable defaultPageable() {
        return PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
    }
}