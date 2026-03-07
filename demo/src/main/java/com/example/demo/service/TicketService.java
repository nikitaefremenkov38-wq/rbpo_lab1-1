package com.example.demo.service;

import com.example.demo.dto.TicketRequest;
import com.example.demo.entity.Schedule;
import com.example.demo.entity.Ticket;
import com.example.demo.entity.Visitor;
import com.example.demo.repository.MaintenanceRepository;
import com.example.demo.repository.ScheduleRepository;
import com.example.demo.repository.TicketRepository;
import com.example.demo.repository.VisitorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final VisitorRepository visitorRepository;
    private final ScheduleRepository scheduleRepository;
    private final MaintenanceRepository maintenanceRepository;

    public TicketService(
            TicketRepository ticketRepository,
            VisitorRepository visitorRepository,
            ScheduleRepository scheduleRepository,
            MaintenanceRepository maintenanceRepository
    ) {
        this.ticketRepository = ticketRepository;
        this.visitorRepository = visitorRepository;
        this.scheduleRepository = scheduleRepository;
        this.maintenanceRepository = maintenanceRepository;
    }

    @Transactional
    public Ticket create(TicketRequest request) {
        Visitor visitor = findVisitor(request.getVisitorId());
        Schedule schedule = findSchedule(request.getScheduleId());

        validateTicketRules(visitor, schedule, null);

        Ticket ticket = new Ticket();
        ticket.setVisitor(visitor);
        ticket.setSchedule(schedule);

        return ticketRepository.save(ticket);
    }

    @Transactional
    public Ticket update(Long id, TicketRequest request) {
        Ticket existing = findTicket(id);
        Visitor visitor = findVisitor(request.getVisitorId());
        Schedule schedule = findSchedule(request.getScheduleId());

        validateTicketRules(visitor, schedule, existing);

        existing.setVisitor(visitor);
        existing.setSchedule(schedule);

        return ticketRepository.save(existing);
    }

    public Ticket findTicket(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found: " + id));
    }

    @Transactional
    public void delete(Long id) {
        Ticket ticket = findTicket(id);
        ticketRepository.delete(ticket);
    }

    public Visitor findVisitor(Long id) {
        return visitorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Visitor not found: " + id));
    }

    public Schedule findSchedule(Long id) {
        return scheduleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Schedule not found: " + id));
    }

    public void validateTicketRules(Visitor visitor, Schedule schedule, Ticket existingTicket) {
        if (!visitor.isActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Inactive visitors cannot buy tickets");
        }

        if (!schedule.getStartTime().isBefore(schedule.getEndTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Schedule time range is invalid");
        }

        boolean onMaintenance = maintenanceRepository.existsByAttractionIdAndStartTimeLessThanAndEndTimeGreaterThan(
                schedule.getAttraction().getId(),
                schedule.getEndTime(),
                schedule.getStartTime()
        );

        if (onMaintenance) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ticket sales are blocked during maintenance");
        }

        boolean sameSchedule = existingTicket != null && existingTicket.getSchedule().getId().equals(schedule.getId());
        if (!sameSchedule) {
            validateCapacity(schedule);
        }

        validateDuplicate(visitor.getId(), schedule.getId(), existingTicket);
    }

    private void validateCapacity(Schedule schedule) {
        long sold = ticketRepository.countByScheduleId(schedule.getId());
        if (sold >= schedule.getAttraction().getCapacity()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Schedule capacity limit reached");
        }
    }

    private void validateDuplicate(Long visitorId, Long scheduleId, Ticket existingTicket) {
        boolean alreadyExists = ticketRepository.existsByVisitorIdAndScheduleId(visitorId, scheduleId);
        if (!alreadyExists) {
            return;
        }

        if (existingTicket != null
                && existingTicket.getVisitor().getId().equals(visitorId)
                && existingTicket.getSchedule().getId().equals(scheduleId)) {
            return;
        }

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Visitor already has ticket for this schedule");
    }
}
