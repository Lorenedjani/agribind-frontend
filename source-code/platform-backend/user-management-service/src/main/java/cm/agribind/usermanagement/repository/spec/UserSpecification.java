package cm.agribind.usermanagement.repository.spec;

import cm.agribind.usermanagement.entity.User;
import cm.agribind.usermanagement.enums.Region;
import cm.agribind.usermanagement.enums.UserStatus;
import cm.agribind.usermanagement.enums.UserType;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {

    public static Specification<User> hasType(UserType type) {
        return (root, query, criteriaBuilder) ->
                type != null ? criteriaBuilder.equal(root.get("type"), type) : null;
    }

    public static Specification<User> hasStatus(UserStatus status) {
        return (root, query, criteriaBuilder) ->
                status != null ? criteriaBuilder.equal(root.get("status"), status) : null;
    }

    public static Specification<User> inRegion(Region region) {
        return (root, query, criteriaBuilder) ->
                region != null ? criteriaBuilder.equal(root.get("address").get("region"), region) : null;
    }

    public static Specification<User> nameContains(String name) {
        return (root, query, criteriaBuilder) ->
                name != null ? criteriaBuilder.like(criteriaBuilder.lower(root.get("name")),
                        "%" + name.toLowerCase() + "%") : null;
    }

    public static Specification<User> phoneContains(String phone) {
        return (root, query, criteriaBuilder) ->
                phone != null ? criteriaBuilder.like(root.get("phoneNumber"),
                        "%" + phone + "%") : null;
    }

    public static Specification<User> isActive() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), UserStatus.ACTIVE);
    }
}