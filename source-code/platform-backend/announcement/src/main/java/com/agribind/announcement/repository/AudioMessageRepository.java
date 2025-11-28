package com.agribind.announcement.repository;

import com.agribind.announcement.model.AudioMessage;
import com.agribind.announcement.model.Language;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AudioMessageRepository extends JpaRepository<AudioMessage, Long> {

    List<AudioMessage> findByAnnouncementId(Long announcementId);

    AudioMessage findByAnnouncementIdAndLanguage(Long announcementId, Language language);

    List<AudioMessage> findByLanguage(Language language);
}
