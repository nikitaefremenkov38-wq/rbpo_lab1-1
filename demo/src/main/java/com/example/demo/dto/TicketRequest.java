package com.example.demo.dto;

import jakarta.validation.constraints.NotNull;

public class TicketRequest {

    @NotNull
    private Long visitorId;

    @NotNull
    private Long scheduleId;

    public Long getVisitorId() {
        return visitorId;
    }

    public void setVisitorId(Long visitorId) {
        this.visitorId = visitorId;
    }

    public Long getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(Long scheduleId) {
        this.scheduleId = scheduleId;
    }
}
