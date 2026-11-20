
-- Sample Tasks
INSERT INTO tasks (id, title, description, status, created_at)
VALUES
    (1001, 'Design Database Schema', 'Create the initial database schema', 'DONE', CURRENT_TIMESTAMP),
    (1002, 'Implement Task Entity', 'Create Task JPA entity and repository', 'DONE', CURRENT_TIMESTAMP),
    (1003, 'Implement REST APIs', 'Develop CRUD endpoints for tasks', 'IN_PROGRESS', CURRENT_TIMESTAMP),
    (1004, 'Implement Dependency Management', 'Add task dependency functionality', 'TODO', CURRENT_TIMESTAMP),
    (1005, 'Write Unit Tests', 'Add service and controller unit tests', 'TODO', CURRENT_TIMESTAMP)
    ON CONFLICT (id) DO NOTHING;

-- Sample Task Dependencies
-- Task 1002 depends on 1001
-- Task 1003 depends on 1002
-- Task 1004 depends on 1003
-- Task 1005 depends on 1003

INSERT INTO task_dependencies (task_id, depends_on_task_id)
VALUES
    (1002, 1001),
    (1003, 1002),
    (1004, 1003),
    (1005, 1003)
    ON CONFLICT DO NOTHING;

-- Synchronize the generated ID sequence
SELECT setval(
               pg_get_serial_sequence('tasks', 'id'),
               COALESCE((SELECT MAX(id) FROM tasks), 1),
               true
       );
