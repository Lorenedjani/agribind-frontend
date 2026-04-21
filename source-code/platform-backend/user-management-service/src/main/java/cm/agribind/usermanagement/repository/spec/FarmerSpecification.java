package cm.agribind.usermanagement.repository.spec;

import cm.agribind.usermanagement.entity.Farmer;
import cm.agribind.usermanagement.enums.AgriculturalType;
import cm.agribind.usermanagement.enums.CropType;
import cm.agribind.usermanagement.enums.LivestockType;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

public class FarmerSpecification {

    public static Specification<Farmer> hasAgriculturalType(AgriculturalType type) {
        return (root, query, criteriaBuilder) ->
                type != null ? criteriaBuilder.equal(root.get("agriculturalType"), type) : null;
    }

    public static Specification<Farmer> hasCropType(CropType cropType) {
        return (root, query, criteriaBuilder) ->
                cropType != null ? criteriaBuilder.isMember(cropType, root.get("cropTypes")) : null;
    }

    public static Specification<Farmer> hasLivestockType(LivestockType livestockType) {
        return (root, query, criteriaBuilder) ->
                livestockType != null ? criteriaBuilder.isMember(livestockType, root.get("livestockTypes")) : null;
    }

    public static Specification<Farmer> inCooperative(Long cooperativeId) {
        return (root, query, criteriaBuilder) ->
                cooperativeId != null ? criteriaBuilder.equal(root.get("cooperative").get("id"), cooperativeId) : null;
    }

    public static Specification<Farmer> hasLandAreaBetween(Double minArea, Double maxArea) {
        return (root, query, criteriaBuilder) -> {
            if (minArea == null && maxArea == null) return null;
            if (minArea == null) return criteriaBuilder.lessThanOrEqualTo(root.get("farmDetails").get("totalLandArea"), maxArea);
            if (maxArea == null) return criteriaBuilder.greaterThanOrEqualTo(root.get("farmDetails").get("totalLandArea"), minArea);
            return criteriaBuilder.between(root.get("farmDetails").get("totalLandArea"), minArea, maxArea);
        };
    }

    public static Specification<Farmer> hasBankAccount(Boolean hasBankAccount) {
        return (root, query, criteriaBuilder) ->
                hasBankAccount != null ? criteriaBuilder.equal(root.get("hasBankAccount"), hasBankAccount) : null;
    }

    public static Specification<Farmer> hasMobileMoney(Boolean hasMobileMoney) {
        return (root, query, criteriaBuilder) ->
                hasMobileMoney != null ? criteriaBuilder.equal(root.get("hasMobileMoney"), hasMobileMoney) : null;
    }
}