package com.agribind.announcement.repository;

import com.agribind.announcement.model.Announcement;
import com.agribind.announcement.model.Status;
import com.agribind.announcement.model.Priority;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    List<Announcement> findByCooperativeId(Long cooperativeId);

    List<Announcement> findByStatus(Status status);

    @Query("SELECT a FROM Announcement a WHERE a.status = 'PUBLISHED' " +
           "AND a.publishDate <= :now AND (a.expirationDate IS NULL OR a.expirationDate > :now)")
    List<Announcement> findActiveAnnouncements(LocalDateTime now);

    @Query("SELECT a FROM Announcement a WHERE a.cooperativeId = :cooperativeId " +
           "AND a.status = 'PUBLISHED' AND a.publishDate <= :now " +
           "AND (a.expirationDate IS NULL OR a.expirationDate > :now)")
    List<Announcement> findActiveAnnouncementsByCooperative(Long cooperativeId, LocalDateTime now);

    List<Announcement> findByPriorityOrderByCreatedAtDesc(Priority priority);
}
