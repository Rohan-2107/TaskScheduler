package com.assignment.taskscheduler.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private Map<String, String> validationErrors;

    public ErrorResponse() {
    }

    public ErrorResponse(
            LocalDateTime timestamp,
            int status,
            String error,
            String message,
            String path,
            Map<String, String> validationErrors) {

        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.validationErrors = validationErrors;
    }
}
