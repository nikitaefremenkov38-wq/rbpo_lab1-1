package com.example.demo.dto;

public class OperationResultResponse {

    private String message;
    private long affectedRecords;

    public OperationResultResponse(String message, long affectedRecords) {
        this.message = message;
        this.affectedRecords = affectedRecords;
    }

    public String getMessage() {
        return message;
    }

    public long getAffectedRecords() {
        return affectedRecords;
    }
}
