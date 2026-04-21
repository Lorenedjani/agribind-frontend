package com.agribind.communication.repository;

import com.agribind.communication.model.Broadcast;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BroadcastRepository extends JpaRepository<Broadcast, Long> {
    List<Broadcast> findAllByOrderByCreatedAtDesc();
}

