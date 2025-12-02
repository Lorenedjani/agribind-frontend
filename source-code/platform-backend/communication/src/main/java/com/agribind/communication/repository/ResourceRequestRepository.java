package com.agribind.communication.repository;

import com.agribind.communication.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ResourceRequestRepository extends JpaRepository<ResourceRequest, Long> {
    List<ResourceRequest> findByStatusOrderByRequestDateDesc(RequestStatus status);

    Optional<ResourceRequest> findByRequestId(String requestId);

    List<ResourceRequest> findByRequestedByZone(String zone);
}