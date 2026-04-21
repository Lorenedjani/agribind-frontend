package com.agribind.notification_service.repository;

import com.agribind.notification_service.model.entity.NotificationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationHistoryRepository extends JpaRepository<NotificationHistory, Long> {
    
    // Fetch notifications for a specific user, ordered by newest first
    List<NotificationHistory> findByUserIdOrderByCreatedAtDesc(String userId);

    // Count unread notifications
    long countByUserIdAndIsReadFalse(String userId);
}
