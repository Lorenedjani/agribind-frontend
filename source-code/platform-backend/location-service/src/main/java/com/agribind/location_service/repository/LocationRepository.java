package com.agribind.location_service.repository;

import com.agribind.location_service.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {

    Optional<Location> findByRegion(String region);

    List<Location> findByRegionContainingIgnoreCase(String region);

    @Query("SELECT l FROM Location l WHERE " +
           "(:region IS NULL OR LOWER(l.region) LIKE LOWER(CONCAT('%', :region, '%')))")
    List<Location> searchLocations(@Param("region") String region);

    @Query("SELECT l FROM Location l WHERE " +
           "l.latitude BETWEEN :minLat AND :maxLat AND " +
           "l.longitude BETWEEN :minLng AND :maxLng")
    List<Location> findByBoundingBox(@Param("minLat") Double minLat,
                                   @Param("maxLat") Double maxLat,
                                   @Param("minLng") Double minLng,
                                   @Param("maxLng") Double maxLng);
}