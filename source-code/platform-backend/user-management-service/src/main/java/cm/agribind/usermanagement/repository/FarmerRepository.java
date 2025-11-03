package cm.agribind.usermanagement.repository;

import cm.agribind.usermanagement.entity.Farmer;
import cm.agribind.usermanagement.enums.AgriculturalType;
import cm.agribind.usermanagement.enums.CropType;
import cm.agribind.usermanagement.enums.LivestockType;
import cm.agribind.usermanagement.enums.Region;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FarmerRepository extends JpaRepository<Farmer, Long>, JpaSpecificationExecutor<Farmer> {

    List<Farmer> findByAgriculturalType(AgriculturalType agriculturalType);

    List<Farmer> findByCooperativeId(Long cooperativeId);

    List<Farmer> findByAddressRegion(Region region);

    @Query("SELECT f FROM Farmer f WHERE :cropType MEMBER OF f.cropTypes")
    List<Farmer> findByCropType(@Param("cropType") CropType cropType);

    @Query("SELECT f FROM Farmer f WHERE :livestockType MEMBER OF f.livestockTypes")
    List<Farmer> findByLivestockType(@Param("livestockType") LivestockType livestockType);

    @Query("SELECT f FROM Farmer f WHERE f.farmDetails.totalLandArea BETWEEN :minArea AND :maxArea")
    List<Farmer> findByLandAreaRange(@Param("minArea") Double minArea, @Param("maxArea") Double maxArea);

    long countByAgriculturalType(AgriculturalType agriculturalType);

    long countByCooperativeId(Long cooperativeId);

    @Query("SELECT COUNT(f) FROM Farmer f WHERE :cropType MEMBER OF f.cropTypes")
    long countByCropType(@Param("cropType") CropType cropType);

    @Query("SELECT f.address.region, COUNT(f) FROM Farmer f GROUP BY f.address.region")
    List<Object[]> countFarmersByRegion();

    Page<Farmer> findByCooperativeId(Long cooperativeId, Pageable pageable);
}