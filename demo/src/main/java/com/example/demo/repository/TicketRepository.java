package com.example.demo.repository;

import com.example.demo.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    long countByScheduleId(Long scheduleId);

    boolean existsByVisitorIdAndScheduleId(Long visitorId, Long scheduleId);

    boolean existsByScheduleIdIn(Collection<Long> scheduleIds);

    long deleteByVisitorIdAndScheduleStartTimeAfter(Long visitorId, LocalDateTime startTime);

    List<Ticket> findByVisitorIdAndScheduleStartTimeGreaterThanEqualAndScheduleEndTimeLessThanEqualOrderByScheduleStartTimeAsc(
            Long visitorId,
            LocalDateTime from,
            LocalDateTime to
    );
}
