package com.assignment.taskscheduler.controller;

import com.assignment.taskscheduler.dto.request.AddDependencyRequest;
import com.assignment.taskscheduler.dto.request.CreateTaskRequest;
import com.assignment.taskscheduler.dto.request.UpdateTaskRequest;
import com.assignment.taskscheduler.dto.request.UpdateTaskStatusRequest;
import com.assignment.taskscheduler.dto.response.TaskResponse;
import com.assignment.taskscheduler.service.TaskService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // 1. Create Task
    @PostMapping
    public ResponseEntity<TaskResponse> createTask(
            @Valid @RequestBody CreateTaskRequest request) {

        TaskResponse response = taskService.createTask(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // 2. Get All Tasks
    @GetMapping
    public ResponseEntity<List<TaskResponse>> getAllTasks() {

        List<TaskResponse> tasks = taskService.getAllTasks();

        return ResponseEntity.ok(tasks);
    }

    // 3. Get Task By ID
    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTaskById(
            @PathVariable Long id) {

        TaskResponse response = taskService.getTaskById(id);

        return ResponseEntity.ok(response);
    }

    // 4. Update Task
    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTaskRequest request) {

        TaskResponse response =
                taskService.updateTask(id, request);

        return ResponseEntity.ok(response);
    }

    // 5. Update Task status
    @PatchMapping("/{id}/status")
    public ResponseEntity<TaskResponse> updateTaskStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTaskStatusRequest request) {

        return ResponseEntity.ok(
                taskService.updateTaskStatus(id, request)
        );
    }

    @PostMapping("/{id}/dependencies")
    public ResponseEntity<TaskResponse> addDependency(
            @PathVariable Long id,
            @Valid @RequestBody AddDependencyRequest request) {

        return ResponseEntity.ok(
                taskService.addDependency(id, request)
        );
    }

    @GetMapping("/{id}/dependencies")
    public ResponseEntity<List<TaskResponse>> getDependencies(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                taskService.getDependencies(id)
        );
    }

    @GetMapping("/execution-order")
    public ResponseEntity<List<TaskResponse>> getExecutionOrder() {

        return ResponseEntity.ok(
                taskService.getExecutionOrder()
        );
    }
}
