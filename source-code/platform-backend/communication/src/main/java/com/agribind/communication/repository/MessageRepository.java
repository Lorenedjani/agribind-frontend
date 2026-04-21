package com.agribind.communication.repository;

import com.agribind.communication.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByStatusOrderByCreatedAtDesc(MessageStatus status);

    List<Message> findByTypeOrderByCreatedAtDesc(MessageType type);

    List<Message> findByScheduledAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.createdAt >= :weekStart")
    Integer countMessagesThisWeek(LocalDateTime weekStart);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.type = 'AUDIO'")
    Integer countAudioMessages();

    @Query("SELECT SUM(m.deliveredCount) FROM Message m")
    Long getTotalDeliveredMessages();
}