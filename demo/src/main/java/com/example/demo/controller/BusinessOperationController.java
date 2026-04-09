package com.example.demo.controller;

import com.example.demo.dto.BulkTicketPurchaseRequest;
import com.example.demo.dto.MaintenanceRequest;
import com.example.demo.dto.OperationResultResponse;
import com.example.demo.dto.RescheduleTicketRequest;
import com.example.demo.dto.VisitorItineraryItemResponse;
import com.example.demo.entity.Maintenance;
import com.example.demo.entity.Ticket;
import com.example.demo.service.BusinessOperationService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/operations")
public class BusinessOperationController {

    private final BusinessOperationService businessOperationService;

    public BusinessOperationController(BusinessOperationService businessOperationService) {
        this.businessOperationService = businessOperationService;
    }

    @PostMapping("/tickets/bulk-purchase")
    @ResponseStatus(HttpStatus.CREATED)
    public List<Ticket> bulkPurchase(@Valid @RequestBody BulkTicketPurchaseRequest request) {
        return businessOperationService.bulkPurchaseTickets(request);
    }

    @PostMapping("/tickets/{ticketId}/reschedule")
    public Ticket reschedule(@PathVariable Long ticketId, @Valid @RequestBody RescheduleTicketRequest request) {
        return businessOperationService.rescheduleTicket(ticketId, request);
    }

    @PostMapping("/maintenances/plan")
    @ResponseStatus(HttpStatus.CREATED)
    public Maintenance planMaintenance(@Valid @RequestBody MaintenanceRequest request) {
        return businessOperationService.planMaintenance(request);
    }

    @PostMapping("/visitors/{visitorId}/deactivate")
    public OperationResultResponse deactivateVisitor(@PathVariable Long visitorId) {
        return businessOperationService.deactivateVisitor(visitorId);
    }

    @PostMapping("/visitors/{visitorId}/activate")
    public OperationResultResponse activateVisitor(@PathVariable Long visitorId) {
        return businessOperationService.activateVisitor(visitorId);
    }

    @GetMapping("/visitors/{visitorId}/itinerary")
    public List<VisitorItineraryItemResponse> getItinerary(
            @PathVariable Long visitorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        return businessOperationService.getVisitorItinerary(visitorId, from, to);
    }
}
