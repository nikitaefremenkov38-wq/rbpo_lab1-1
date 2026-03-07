package com.example.demo.service;

import com.example.demo.dto.BulkTicketPurchaseRequest;
import com.example.demo.dto.MaintenanceRequest;
import com.example.demo.dto.OperationResultResponse;
import com.example.demo.dto.RescheduleTicketRequest;
import com.example.demo.dto.VisitorItineraryItemResponse;
import com.example.demo.entity.Attraction;
import com.example.demo.entity.Maintenance;
import com.example.demo.entity.Schedule;
import com.example.demo.entity.Ticket;
import com.example.demo.entity.Visitor;
import com.example.demo.repository.AttractionRepository;
import com.example.demo.repository.MaintenanceRepository;
import com.example.demo.repository.ScheduleRepository;
import com.example.demo.repository.TicketRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class BusinessOperationService {

    private final TicketService ticketService;
    private final TicketRepository ticketRepository;
    private final AttractionRepository attractionRepository;
    private final ScheduleRepository scheduleRepository;
    private final MaintenanceRepository maintenanceRepository;

    public BusinessOperationService(
            TicketService ticketService,
            TicketRepository ticketRepository,
            AttractionRepository attractionRepository,
            ScheduleRepository scheduleRepository,
            MaintenanceRepository maintenanceRepository
    ) {
        this.ticketService = ticketService;
        this.ticketRepository = ticketRepository;
        this.attractionRepository = attractionRepository;
        this.scheduleRepository = scheduleRepository;
        this.maintenanceRepository = maintenanceRepository;
    }

    @Transactional
    public List<Ticket> bulkPurchaseTickets(BulkTicketPurchaseRequest request) {
        Visitor visitor = ticketService.findVisitor(request.getVisitorId());

        Set<Long> uniqueScheduleIds = new LinkedHashSet<>(request.getScheduleIds());
        if (uniqueScheduleIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "scheduleIds must contain at least one element");
        }

        List<Ticket> createdTickets = new ArrayList<>();
        for (Long scheduleId : uniqueScheduleIds) {
            Schedule schedule = ticketService.findSchedule(scheduleId);
            ticketService.validateTicketRules(visitor, schedule, null);

            Ticket ticket = new Ticket();
            ticket.setVisitor(visitor);
            ticket.setSchedule(schedule);
            createdTickets.add(ticketRepository.save(ticket));
        }

        return createdTickets;
    }

    @Transactional
    public Ticket rescheduleTicket(Long ticketId, RescheduleTicketRequest request) {
        Ticket ticket = ticketService.findTicket(ticketId);
        Schedule newSchedule = ticketService.findSchedule(request.getNewScheduleId());

        ticketService.validateTicketRules(ticket.getVisitor(), newSchedule, ticket);
        ticket.setSchedule(newSchedule);

        return ticketRepository.save(ticket);
    }

    @Transactional
    public Maintenance planMaintenance(MaintenanceRequest request) {
        validateTimeRange(request.getStartTime(), request.getEndTime());
        Attraction attraction = findAttraction(request.getAttractionId());

        boolean hasOverlappingMaintenance = maintenanceRepository
                .existsByAttractionIdAndStartTimeLessThanAndEndTimeGreaterThan(
                        attraction.getId(),
                        request.getEndTime(),
                        request.getStartTime()
                );

        if (hasOverlappingMaintenance) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Maintenance interval overlaps existing maintenance");
        }

        List<Schedule> overlappingSchedules = scheduleRepository
                .findByAttractionIdAndStartTimeLessThanAndEndTimeGreaterThan(
                        attraction.getId(),
                        request.getEndTime(),
                        request.getStartTime()
                );

        List<Long> overlappingScheduleIds = overlappingSchedules.stream().map(Schedule::getId).toList();
        if (!overlappingScheduleIds.isEmpty() && ticketRepository.existsByScheduleIdIn(overlappingScheduleIds)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot plan maintenance: there are sold tickets for overlapping schedules"
            );
        }

        Maintenance maintenance = new Maintenance();
        maintenance.setAttraction(attraction);
        maintenance.setStartTime(request.getStartTime());
        maintenance.setEndTime(request.getEndTime());
        maintenance.setReason(request.getReason());
        return maintenanceRepository.save(maintenance);
    }

    @Transactional
    public OperationResultResponse deactivateVisitor(Long visitorId) {
        Visitor visitor = ticketService.findVisitor(visitorId);
        long cancelledTickets = ticketRepository.deleteByVisitorIdAndScheduleStartTimeAfter(visitorId, LocalDateTime.now());

        visitor.setActive(false);

        return new OperationResultResponse("Visitor deactivated and future tickets cancelled", cancelledTickets);
    }

    @Transactional(readOnly = true)
    public List<VisitorItineraryItemResponse> getVisitorItinerary(Long visitorId, LocalDateTime from, LocalDateTime to) {
        if (!from.isBefore(to)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "from must be before to");
        }

        ticketService.findVisitor(visitorId);

        List<Ticket> tickets = ticketRepository
                .findByVisitorIdAndScheduleStartTimeGreaterThanEqualAndScheduleEndTimeLessThanEqualOrderByScheduleStartTimeAsc(
                        visitorId,
                        from,
                        to
                );

        return tickets.stream()
                .map(ticket -> new VisitorItineraryItemResponse(
                        ticket.getId(),
                        ticket.getSchedule().getId(),
                        ticket.getSchedule().getAttraction().getName(),
                        ticket.getSchedule().getStartTime(),
                        ticket.getSchedule().getEndTime(),
                        ticket.getCreatedAt()
                ))
                .toList();
    }

    private Attraction findAttraction(Long id) {
        return attractionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Attraction not found: " + id));
    }

    private void validateTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        if (!startTime.isBefore(endTime)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "startTime must be before endTime");
        }
    }
}
