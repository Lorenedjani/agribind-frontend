package com.agribind.production_monitoring.repository;

import com.agribind.production_monitoring.model.MaturityUpdate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaturityUpdateRepository extends JpaRepository<MaturityUpdate, Long> {

    List<MaturityUpdate> findByProductionRecordId(Long productionRecordId);

    List<MaturityUpdate> findByProductionRecordIdOrderByUpdateDateDesc(Long productionRecordId);

    List<MaturityUpdate> findByUpdatedByFarmerId(Long farmerId);
}
