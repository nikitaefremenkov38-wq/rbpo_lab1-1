package com.example.demo.repository;

import com.example.demo.entity.Maintenance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface MaintenanceRepository extends JpaRepository<Maintenance, Long> {
    boolean existsByAttractionIdAndStartTimeLessThanAndEndTimeGreaterThan(
            Long attractionId,
            LocalDateTime endTime,
            LocalDateTime startTime
    );

    boolean existsByAttractionIdAndStartTimeLessThanAndEndTimeGreaterThanAndIdNot(
            Long attractionId,
            LocalDateTime endTime,
            LocalDateTime startTime,
            Long id
    );
}
