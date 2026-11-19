package com.assignment.taskscheduler.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddDependencyRequest {
    @NotNull(message = "Dependency task ID is required")
    @Positive(message = "Dependency task ID must be positive")
    private Long dependsOnTaskId;
}
