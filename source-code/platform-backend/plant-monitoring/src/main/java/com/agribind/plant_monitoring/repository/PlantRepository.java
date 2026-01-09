package com.agribind.plant_monitoring.repository;

import com.agribind.plant_monitoring.model.Plant;
import com.agribind.plant_monitoring.model.PlantPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PlantRepository extends JpaRepository<Plant, Long> {
    List<Plant> findByNameContainingIgnoreCase(String name);
}
