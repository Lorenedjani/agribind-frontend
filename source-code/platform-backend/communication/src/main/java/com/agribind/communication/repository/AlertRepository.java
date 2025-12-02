package com.agribind.communication.repository;

import com.agribind.communication.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByStatusOrderByCreatedAtDesc(AlertStatus status);

    Optional<Alert> findByAlertId(String alertId);

    @Query("SELECT COUNT(a) FROM Alert a WHERE a.status = 'ACTIVE'")
    Integer countActiveAlerts();

    @Query("SELECT COUNT(a) FROM Alert a WHERE a.priority = 'CRITICAL' AND a.status = 'ACTIVE'")
    Integer countCriticalAlerts();
}
