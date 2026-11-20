package com.assignment.taskscheduler.service;

import com.assignment.taskscheduler.dto.request.CreateTaskRequest;
import com.assignment.taskscheduler.dto.request.UpdateTaskStatusRequest;
import com.assignment.taskscheduler.dto.response.TaskResponse;
import com.assignment.taskscheduler.entity.Task;
import com.assignment.taskscheduler.entity.TaskStatus;
import com.assignment.taskscheduler.exception.BusinessException;
import com.assignment.taskscheduler.exception.ResourceNotFoundException;
import com.assignment.taskscheduler.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskServiceImpl taskService;

    @Test
    void shouldCreateTaskSuccessfully() {

        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("Complete database setup");
        request.setDescription("Configure PostgreSQL");

        Task savedTask = new Task();
        savedTask.setId(1L);
        savedTask.setTitle(request.getTitle());
        savedTask.setDescription(request.getDescription());
        savedTask.setStatus(TaskStatus.TODO);

        when(taskRepository.save(any(Task.class)))
                .thenReturn(savedTask);

        TaskResponse response = taskService.createTask(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Complete database setup", response.getTitle());
        assertEquals(TaskStatus.TODO, response.getStatus());

        verify(taskRepository, times(1))
                .save(any(Task.class));
    }

    @Test
    void shouldThrowExceptionWhenTaskDoesNotExist() {

        Long taskId = 999L;

        when(taskRepository.findById(taskId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> taskService.getTaskById(taskId)
        );

        verify(taskRepository, times(1))
                .findById(taskId);
    }

    @Test
    void shouldNotAllowTaskToStartWhenDependencyIsIncomplete() {

        Task dependency = new Task();
        dependency.setId(1L);
        dependency.setStatus(TaskStatus.IN_PROGRESS);

        Task task = new Task();
        task.setId(2L);
        task.setStatus(TaskStatus.TODO);
        task.addDependency(dependency);

        when(taskRepository.findById(2L))
                .thenReturn(Optional.of(task));

        UpdateTaskStatusRequest request =
                new UpdateTaskStatusRequest();

        request.setStatus(TaskStatus.IN_PROGRESS);

        assertThrows(
                BusinessException.class,
                () -> taskService.updateTaskStatus(2L, request)
        );
    }

    @Test
    void shouldAllowTaskToStartWhenDependencyIsCompleted() {

        Task dependency = new Task();
        dependency.setId(1L);
        dependency.setStatus(TaskStatus.DONE);

        Task task = new Task();
        task.setId(2L);
        task.setStatus(TaskStatus.TODO);
        task.addDependency(dependency);

        when(taskRepository.findById(2L))
                .thenReturn(Optional.of(task));

        UpdateTaskStatusRequest request =
                new UpdateTaskStatusRequest();

        request.setStatus(TaskStatus.IN_PROGRESS);

        TaskResponse response =
                taskService.updateTaskStatus(2L, request);

        assertEquals(TaskStatus.IN_PROGRESS, response.getStatus());
    }
}
