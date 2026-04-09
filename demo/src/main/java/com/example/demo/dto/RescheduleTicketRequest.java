package com.example.demo.dto;

import jakarta.validation.constraints.NotNull;

public class RescheduleTicketRequest {

    @NotNull
    private Long newScheduleId;

    public Long getNewScheduleId() {
        return newScheduleId;
    }

    public void setNewScheduleId(Long newScheduleId) {
        this.newScheduleId = newScheduleId;
    }
}
