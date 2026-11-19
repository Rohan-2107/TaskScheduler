package com.assignment.taskscheduler.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tasks")
@Getter
@Setter
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TaskStatus status = TaskStatus.TODO;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Tasks that this task depends on
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "task_dependencies",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "depends_on_task_id"),
            uniqueConstraints = @UniqueConstraint(
                    columnNames = {"task_id", "depends_on_task_id"}
            )
    )
    private Set<Task> dependencies = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Task() {
    }

    public Task(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public void addDependency(Task dependency) {
        this.dependencies.add(dependency);
    }

    public void removeDependency(Task dependency) {
        this.dependencies.remove(dependency);
    }

}
