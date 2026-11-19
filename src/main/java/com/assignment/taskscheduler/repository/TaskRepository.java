package com.assignment.taskscheduler.repository;

import com.assignment.taskscheduler.entity.Task;
import com.assignment.taskscheduler.entity.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByStatus(TaskStatus status);
}
