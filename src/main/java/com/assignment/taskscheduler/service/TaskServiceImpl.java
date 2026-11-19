package com.assignment.taskscheduler.service;

import com.assignment.taskscheduler.dto.request.AddDependencyRequest;
import com.assignment.taskscheduler.dto.request.CreateTaskRequest;
import com.assignment.taskscheduler.dto.request.UpdateTaskRequest;
import com.assignment.taskscheduler.dto.request.UpdateTaskStatusRequest;
import com.assignment.taskscheduler.dto.response.TaskResponse;
import com.assignment.taskscheduler.entity.Task;
import com.assignment.taskscheduler.entity.TaskStatus;
import com.assignment.taskscheduler.exception.BusinessException;
import com.assignment.taskscheduler.exception.ResourceNotFoundException;
import com.assignment.taskscheduler.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;

    public TaskServiceImpl(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }


    // 1. Create Task
    @Override
    public TaskResponse createTask(CreateTaskRequest request) {

        Task task = new Task();

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());

        Task savedTask = taskRepository.save(task);

        return mapToResponse(savedTask);
    }

    // 2. Get All Tasks
    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> getAllTasks() {

        return taskRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // 3. Get Task By ID
    @Override
    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long id) {

        Task task = getTaskEntityById(id);

        return mapToResponse(task);
    }

    // 4. Update Task
    @Override
    public TaskResponse updateTask(
            Long id,
            UpdateTaskRequest request) {

        Task task = getTaskEntityById(id);

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());

        Task updatedTask = taskRepository.save(task);

        return mapToResponse(updatedTask);
    }

    // Helper method to retrieve a task
    private Task getTaskEntityById(Long id) {

        return taskRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Task not found with ID: " + id
                        )
                );
    }

    // Convert Entity into Response DTO
    private TaskResponse mapToResponse(Task task) {

        boolean blocked = task.getDependencies()
                .stream()
                .anyMatch(dependency ->
                        dependency.getStatus() != TaskStatus.DONE
                );

        Set<Long> dependencyIds = task.getDependencies()
                .stream()
                .map(Task::getId)
                .collect(Collectors.toSet());

        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getCreatedAt(),
                blocked,
                dependencyIds
        );
    }

    @Override
    public TaskResponse updateTaskStatus(
            Long id,
            UpdateTaskStatusRequest request) {

        Task task = getTaskEntityById(id);

        TaskStatus newStatus = request.getStatus();

        boolean hasIncompleteDependencies =
                task.getDependencies().stream()
                        .anyMatch(dependency ->
                                dependency.getStatus() != TaskStatus.DONE);

        if ((newStatus == TaskStatus.IN_PROGRESS ||
                newStatus == TaskStatus.DONE)
                && hasIncompleteDependencies) {

            throw new BusinessException(
                    "Task cannot be moved to " + newStatus
                            + " because one or more dependencies are incomplete"
            );
        }

        task.setStatus(newStatus);

        return mapToResponse(task);
    }

    private boolean hasPath(
            Task current,
            Long targetTaskId,
            Set<Long> visited) {

        if (current.getId().equals(targetTaskId)) {
            return true;
        }

        if (!visited.add(current.getId())) {
            return false;
        }

        for (Task dependency : current.getDependencies()) {

            if (hasPath(dependency, targetTaskId, visited)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public TaskResponse addDependency(
            Long taskId,
            AddDependencyRequest request) {

        Task task = getTaskEntityById(taskId);

        Long dependencyId = request.getDependsOnTaskId();
        Task dependency = getTaskEntityById(dependencyId);

        // A task cannot depend on itself
        if (taskId.equals(dependencyId)) {
            throw new BusinessException(
                    "A task cannot depend on itself"
            );
        }

        // Prevent duplicate dependencies
        boolean alreadyExists = task.getDependencies()
                .stream()
                .anyMatch(existing ->
                        existing.getId().equals(dependencyId));

        if (alreadyExists) {
            throw new BusinessException(
                    "This dependency already exists"
            );
        }

        // Prevent adding an incomplete dependency to an active or completed task
        if (task.getStatus() != TaskStatus.TODO
                && dependency.getStatus() != TaskStatus.DONE) {
            throw new BusinessException(
                    "Cannot add an incomplete dependency to a task "
                            + "that is already IN_PROGRESS or DONE"
            );
        }

        // Check whether this dependency would create a cycle
        Set<Long> visited = new HashSet<>();

        if (hasPath(dependency, taskId, visited)) {
            throw new BusinessException(
                    "Adding this dependency would create a circular dependency"
            );
        }

        task.addDependency(dependency);

        return mapToResponse(task);
    }

    @Override
    public List<TaskResponse> getDependencies(Long taskId) {

        Task task = getTaskEntityById(taskId);

        return task.getDependencies()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> getExecutionOrder() {

        List<Task> tasks = taskRepository.findAll();

        // Store tasks by ID for quick lookup
        Map<Long, Task> taskMap = new HashMap<>();

        // Store the number of incomplete prerequisites for each task
        Map<Long, Integer> inDegree = new HashMap<>();

        // Reverse adjacency: prerequisite ID -> tasks depending on it
        Map<Long, List<Long>> dependents = new HashMap<>();

        for (Task task : tasks) {
            Long taskId = task.getId();

            taskMap.put(taskId, task);
            inDegree.put(taskId, task.getDependencies().size());

            dependents.put(taskId, new ArrayList<>());
        }

        // Build reverse adjacency
        for (Task task : tasks) {

            Long taskId = task.getId();

            for (Task dependency : task.getDependencies()) {

                Long dependencyId = dependency.getId();

                dependents.get(dependencyId).add(taskId);
            }
        }

        // Tasks with zero prerequisites are ready first
        Queue<Long> queue = new PriorityQueue<>();

        for (Map.Entry<Long, Integer> entry : inDegree.entrySet()) {

            if (entry.getValue() == 0) {
                queue.offer(entry.getKey());
            }
        }

        List<TaskResponse> executionOrder = new ArrayList<>();

        while (!queue.isEmpty()) {

            Long currentTaskId = queue.poll();

            Task currentTask = taskMap.get(currentTaskId);

            executionOrder.add(mapToResponse(currentTask));

            // Process tasks that depend on the current task
            for (Long dependentId : dependents.get(currentTaskId)) {

                int updatedInDegree = inDegree.get(dependentId) - 1;

                inDegree.put(dependentId, updatedInDegree);

                if (updatedInDegree == 0) {
                    queue.offer(dependentId);
                }
            }
        }

        // If not every task was processed, a cycle exists
        if (executionOrder.size() != tasks.size()) {
            throw new BusinessException(
                    "Cannot calculate execution order because circular dependencies exist"
            );
        }

        return executionOrder;
    }

}
