package cm.agribind.usermanagement.repository;

import cm.agribind.usermanagement.entity.GovernmentOfficial;
import cm.agribind.usermanagement.enums.GovernmentRole;
import cm.agribind.usermanagement.enums.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GovernmentRepository extends JpaRepository<GovernmentOfficial, Long>, JpaSpecificationExecutor<GovernmentOfficial> {

    Optional<GovernmentOfficial> findByEmployeeId(String employeeId);

    List<GovernmentOfficial> findByRole(GovernmentRole role);

    List<GovernmentOfficial> findByAssignedRegion(Region region);

    List<GovernmentOfficial> findByDepartment(String department);

    List<GovernmentOfficial> findByCanApproveLoansTrue();

    List<GovernmentOfficial> findByCanViewStatisticsTrue();

    @Query("SELECT g.assignedRegion, COUNT(g) FROM GovernmentOfficial g GROUP BY g.assignedRegion")
    List<Object[]> countOfficialsByRegion();

    @Query("SELECT g.role, COUNT(g) FROM GovernmentOfficial g GROUP BY g.role")
    List<Object[]> countOfficialsByRole();
}