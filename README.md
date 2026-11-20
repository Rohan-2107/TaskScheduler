# Task Management System

A RESTful Task Management application built using Java, Spring Boot, Spring Data JPA, Hibernate, and PostgreSQL. The application supports task creation, updates, status management, task dependencies, circular dependency prevention, and dependency-aware execution ordering.

The application is containerized using Docker and Docker Compose for easy setup and deployment.

## Features

- Create, retrieve, and update tasks.
- Manage task statuses: `TODO`, `IN_PROGRESS`, and `DONE`.
- Define dependencies between tasks.
- Identify blocked tasks based on incomplete dependencies.
- Prevent tasks from moving to `IN_PROGRESS` or `DONE` when prerequisites are incomplete.
- Prevent self-dependencies and duplicate dependencies.
- Detect and prevent circular dependencies using Depth-First Search (DFS).
- Calculate a valid task execution order using topological sorting.
- Centralized exception handling and request validation.
- Dockerized application and PostgreSQL database.

## Technology Stack

| Component | Technology                  |
|---|-----------------------------|
| Language | Java 25                     |
| Backend Framework | Spring Boot 4               |
| Build Tool | Maven                       |
| ORM | Hibernate / Spring Data JPA |
| Database | PostgreSQL                  |
| API Style | REST                        |
| Containerization | Docker                      |
| Container Orchestration | Docker Compose              |
| Testing | JUnit 5, Mockito            |

## Architecture

The application follows a layered architecture:

```text
Client (Postman / REST Client)
             |
             v
       REST Controller
             |
             v
        Service Layer
             |
             v
       Repository Layer
             |
             v
         PostgreSQL
```

### Layer Responsibilities

- **Controller:** Exposes REST endpoints and handles HTTP requests and responses.
- **Service:** Implements business rules, task status validation, dependency management, and execution-order calculation.
- **Repository:** Provides database access using Spring Data JPA.
- **Entity:** Represents database entities and their relationships.
- **DTO:** Separates API request and response models from persistence entities.
- **Exception:** Provides centralized application error handling.

## Database Design

### Tasks Table

| Column | Description |
|---|---|
| id | Unique task identifier |
| title | Task title |
| description | Task description |
| status | TODO, IN_PROGRESS, or DONE |
| created_at | Task creation timestamp |

### Task Dependencies Table

| Column | Description |
|---|---|
| task_id | Task that has a prerequisite |
| depends_on_task_id | Prerequisite task |

The dependency relationship is represented as a self-referencing many-to-many relationship.

For example, if Task B depends on Task A, the relationship is stored as:

```text
task_id = B
depends_on_task_id = A
```

## Business Rules

1. A task is created with the default status `TODO`.
2. A task can have multiple dependencies.
3. A task cannot depend on itself.
4. Duplicate dependencies are rejected.
5. Circular dependencies are not allowed.
6. A task cannot transition to `IN_PROGRESS` or `DONE` while any dependency is incomplete.
7. A task is considered blocked if at least one dependency is not `DONE`.
8. A valid execution order must place every prerequisite before its dependent task.

## Algorithms

### Circular Dependency Detection

Depth-First Search (DFS) is used to determine whether adding a dependency would create a cycle.

Before adding a dependency, the application searches the existing dependency graph. If the proposed relationship creates a path back to the original task, the relationship is rejected.

Time complexity: O(V + E), where V is the number of tasks and E is the number of dependency relationships explored.

### Task Execution Order

Topological sorting is used to calculate a valid execution order.

Kahn's algorithm processes tasks with zero prerequisites first and then processes their dependent tasks.

Time complexity: O(V + E).

Multiple valid execution orders may exist when tasks are independent of one another.

## API Documentation

Base URL:

```text
http://localhost:8080/api/v1
```

### 1. Create Task

`POST /tasks`

Request:

```json
{
  "title": "Configure database",
  "description": "Set up PostgreSQL"
}
```

Response: `201 Created`

### 2. Retrieve All Tasks

`GET /tasks`

Response: `200 OK`

Returns a list of tasks.

### 3. Retrieve Task by ID

`GET /tasks/{id}`

Example:

```text
GET /tasks/1
```

Response: `200 OK`

### 4. Update Task

`PUT /tasks/{id}`

Request:

```json
{
  "title": "Configure PostgreSQL",
  "description": "Complete database configuration"
}
```

Response: `200 OK`

### 5. Update Task Status

`PATCH /tasks/{id}/status`

Request:

```json
{
  "status": "IN_PROGRESS"
}
```

Response: `200 OK`

A transition is rejected if the task has incomplete dependencies.

### 6. Add Task Dependency

`POST /tasks/{id}/dependencies`

Request:

```json
{
  "dependsOnTaskId": 1
}
```

This creates a relationship in which the specified task depends on task ID 1.

### 7. Retrieve Task Dependencies

`GET /tasks/{id}/dependencies`

Returns the prerequisite tasks associated with the specified task.

### 8. Execution Order

Document the execution-order endpoint implemented in the controller here, including its HTTP method and exact path.

The endpoint should return a valid topological ordering of tasks, with prerequisites appearing before dependent tasks.

## Error Handling

The application uses centralized exception handling through `@RestControllerAdvice`.

| HTTP Status | Description |
|---|---|
| 400 | Invalid request or business-rule violation |
| 404 | Requested task not found |
| 500 | Unexpected internal server error |

Example error response:

```json
{
  "timestamp": "2026-11-20T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "A task cannot depend on itself",
  "path": "/api/v1/tasks/1/dependencies"
}
```

Validation errors are returned with field-level details.

## Running the Application Locally

### Prerequisites

- Java 25
- Maven
- PostgreSQL

Configure the database connection in `application.yml`, then run:

```bash
mvn spring-boot:run
```

The application starts at:

```text
http://localhost:8080
```

## Running with Docker

### Prerequisites

- Docker Desktop or Docker Engine
- Docker Compose

### 1. Configure environment variables

Create a `.env` file in the project root:

```env
POSTGRES_DB=taskDB
POSTGRES_USER=rohan
POSTGRES_PASSWORD=12345
```

### 2. Build and start containers

```bash
docker compose up --build
```

This starts the Spring Boot application and PostgreSQL database.

### 3. Stop containers

```bash
docker compose down
```

PostgreSQL data is persisted using a Docker volume.

To remove containers and the database volume:

```bash
docker compose down -v
```

**Warning:** Removing the volume deletes the stored database data.

## Project Structure

```text
task-management/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/taskmanagement/
│   │   │       ├── controller/
│   │   │       ├── dto/
│   │   │       │   ├── request/
│   │   │       │   └── response/
│   │   │       ├── entity/
│   │   │       ├── exception/
│   │   │       ├── repository/
│   │   │       └── service/
│   │   └── resources/
│   │       └── application.yml
│   └── test/
├── .dockerignore
├── .env.example
├── .gitignore
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── README.md
```

## Design Decisions and Assumptions

- Task dependencies are represented using a self-referencing many-to-many relationship.
- Task status is stored as a string-backed enum.
- DTOs are used to separate API contracts from database entities.
- Business validation is implemented in the service layer.
- DFS is used for cycle detection.
- Kahn's algorithm is used for topological sorting.
- Docker Compose provides service discovery and networking between the application and database.
- A named Docker volume is used to persist PostgreSQL data.
- Hibernate schema updates are configured for development. A migration tool such as Flyway or Liquibase would be preferable for production schema management.

## Future Improvements

- Comprehensive JUnit 5 and Mockito unit tests.
- Controller integration tests using MockMvc.
- API documentation using Swagger / OpenAPI.
- Database migrations using Flyway.
- CI/CD pipeline using GitHub Actions.
- Pagination and filtering for task retrieval.
- Authentication and authorization.

### Optional: Load Sample Data

The project includes an optional SQL seed script to populate the database with sample tasks and task dependencies.

**Seed file:** `database/seed.sql`

You can execute it using either of the following approaches.

**Local PostgreSQL:**

```bash
psql -U postgres -d taskDB -f database/seed.sql
```

**Docker Compose:**

```bash
docker compose exec -T postgres psql -U rohan -d taskDB < database/seed.sql
```

Alternatively, open the SQL file in pgAdmin's Query Tool and execute it.

The seed script creates sample tasks with different statuses and demonstrates task dependency relationships. It is not executed automatically when the application starts.

## Author

**Rohan Chowdhury**

