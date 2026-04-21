package cm.agribind.usermanagement.mapper;

import cm.agribind.usermanagement.dto.query.UserFilterQuery;
import cm.agribind.usermanagement.dto.query.UserQuery;
import cm.agribind.usermanagement.enums.UserStatus;
import cm.agribind.usermanagement.enums.UserType;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Mapper(componentModel = "spring")
public interface QueryMapper {

    @Named("toPageable")
    default Pageable toPageable(UserFilterQuery query) {
        // Fixed: Use proper PageRequest creation
        Sort.Direction direction = Sort.Direction.fromString(query.getSortDirection());
        Sort sort = Sort.by(direction, query.getSortBy());
        return PageRequest.of(query.getPage(), query.getSize(), sort);
    }

    @Named("toUserQuery")
    default UserQuery toUserQuery(UserFilterQuery filterQuery) {
        UserQuery userQuery = new UserQuery();
        userQuery.setType(filterQuery.getType());
        userQuery.setStatus(filterQuery.getStatus());
        userQuery.setRegion(filterQuery.getRegion());
        userQuery.setSearchTerm(filterQuery.getSearchTerm());
        userQuery.setPage(filterQuery.getPage());
        userQuery.setSize(filterQuery.getSize());
        userQuery.setSortBy(filterQuery.getSortBy());

        // Fixed: Handle direction properly
        try {
            userQuery.setDirection(Sort.Direction.fromString(filterQuery.getSortDirection()));
        } catch (IllegalArgumentException e) {
            userQuery.setDirection(Sort.Direction.ASC);
        }

        return userQuery;
    }

    @Named("stringToUserType")
    default UserType stringToUserType(String type) {
        if (type == null) return null;
        try {
            return UserType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Named("stringToUserStatus")
    default UserStatus stringToUserStatus(String status) {
        if (status == null) return null;
        try {
            return UserStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}