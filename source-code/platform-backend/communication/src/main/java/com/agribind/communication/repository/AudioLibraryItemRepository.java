package com.agribind.communication.repository;

import com.agribind.communication.model.AudioLibraryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AudioLibraryItemRepository extends JpaRepository<AudioLibraryItem, Long> {

    List<AudioLibraryItem> findAllByOrderByCreatedAtDesc();

    /**
     * Count the number of distinct languages present in the audio library.
     * Used by updateStatistics() to populate audioLanguagesSupported.
     */
    @Query("SELECT COUNT(DISTINCT a.language) FROM AudioLibraryItem a")
    int countDistinctLanguages();
}