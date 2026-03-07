package com.example.demo.dto;

import java.time.LocalDateTime;

public class VisitorItineraryItemResponse {

    private final Long ticketId;
    private final Long scheduleId;
    private final String attractionName;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final LocalDateTime purchasedAt;

    public VisitorItineraryItemResponse(
            Long ticketId,
            Long scheduleId,
            String attractionName,
            LocalDateTime startTime,
            LocalDateTime endTime,
            LocalDateTime purchasedAt
    ) {
        this.ticketId = ticketId;
        this.scheduleId = scheduleId;
        this.attractionName = attractionName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.purchasedAt = purchasedAt;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public Long getScheduleId() {
        return scheduleId;
    }

    public String getAttractionName() {
        return attractionName;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public LocalDateTime getPurchasedAt() {
        return purchasedAt;
    }
}
