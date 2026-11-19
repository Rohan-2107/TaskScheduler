package com.assignment.taskscheduler.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateTaskRequest {
    @NotBlank(message = "Task title is required")
    @Size(max = 255, message = "Title cannot exceed 255 characters")
    private String title;

    private String description;

    public CreateTaskRequest() {
    }
}
