package com.assignment.taskscheduler.dto.response;


import com.assignment.taskscheduler.entity.TaskStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
public class TaskResponse {
    private Long id;

    private String title;

    private String description;

    private TaskStatus status;

    private LocalDateTime createdAt;

    private boolean blocked;

    private Set<Long> dependencyIds;

    public TaskResponse() {
    }

    public TaskResponse(
            Long id,
            String title,
            String description,
            TaskStatus status,
            LocalDateTime createdAt,
            boolean blocked,
            Set<Long> dependencyIds) {

        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
        this.blocked = blocked;
        this.dependencyIds = dependencyIds;
    }
}
