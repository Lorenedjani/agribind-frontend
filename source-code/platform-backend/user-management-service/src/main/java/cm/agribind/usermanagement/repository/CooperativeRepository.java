package cm.agribind.usermanagement.repository;

import cm.agribind.usermanagement.entity.Cooperative;
import cm.agribind.usermanagement.enums.CooperativeType;
import cm.agribind.usermanagement.enums.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CooperativeRepository extends JpaRepository<Cooperative, Long>, JpaSpecificationExecutor<Cooperative> {

    Optional<Cooperative> findByLegalRegistrationNumber(String legalRegistrationNumber);

    List<Cooperative> findByCooperativeType(CooperativeType cooperativeType);

    List<Cooperative> findByOperatingRegion(Region region);

    List<Cooperative> findByEstablishmentYear(Integer establishmentYear);

    @Query("SELECT c FROM Cooperative c WHERE c.activeMemberCount >= :minMembers")
    List<Cooperative> findByMinimumMembers(@Param("minMembers") Integer minMembers);

    @Query("SELECT c.operatingRegion, COUNT(c) FROM Cooperative c GROUP BY c.operatingRegion")
    List<Object[]> countCooperativesByRegion();

    @Query("SELECT AVG(c.activeMemberCount) FROM Cooperative c")
    Double findAverageMemberCount();

    long countByCooperativeType(CooperativeType cooperativeType);
}