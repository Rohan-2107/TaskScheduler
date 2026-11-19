package com.assignment.taskscheduler.dto.request;

import com.assignment.taskscheduler.entity.TaskStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateTaskStatusRequest {
    @NotNull(message = "Status is required")
    private TaskStatus status;
}
