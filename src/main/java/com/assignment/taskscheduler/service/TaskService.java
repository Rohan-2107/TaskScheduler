package com.assignment.taskscheduler.service;

import com.assignment.taskscheduler.dto.request.AddDependencyRequest;
import com.assignment.taskscheduler.dto.request.CreateTaskRequest;
import com.assignment.taskscheduler.dto.request.UpdateTaskRequest;
import com.assignment.taskscheduler.dto.request.UpdateTaskStatusRequest;
import com.assignment.taskscheduler.dto.response.TaskResponse;

import java.util.List;

public interface TaskService {
    TaskResponse createTask(CreateTaskRequest request);

    List<TaskResponse> getAllTasks();

    TaskResponse getTaskById(Long id);

    TaskResponse updateTask(Long id, UpdateTaskRequest request);

    TaskResponse updateTaskStatus(Long id, UpdateTaskStatusRequest request);

    TaskResponse addDependency(Long taskId, AddDependencyRequest request);

    List<TaskResponse> getDependencies(Long taskId);

    List<TaskResponse> getExecutionOrder();
}
