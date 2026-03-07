package com.example.demo.repository;

import com.example.demo.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByAttractionIdAndStartTimeLessThanAndEndTimeGreaterThan(
            Long attractionId,
            LocalDateTime endTime,
            LocalDateTime startTime
    );

    List<Schedule> findByAttractionIdAndStartTimeGreaterThanEqualAndEndTimeLessThanEqualOrderByStartTimeAsc(
            Long attractionId,
            LocalDateTime from,
            LocalDateTime to
    );
}
