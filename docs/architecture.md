# Architecture

[← Back to README](../README.md)

This document describes the internal structure, domain model, persistence strategy, and main design decisions of the Fitness Tracker API.

The application is a modular monolith built with Java 25, Spring Boot 4.1, Spring Web MVC, Spring Security, Spring Data JPA, Hibernate, Flyway, and MySQL 8.

## Table of Contents

- [Architectural Style](#architectural-style)
- [Request Lifecycle](#request-lifecycle)
- [Layer Responsibilities](#layer-responsibilities)
- [Authenticated-User Boundary](#authenticated-user-boundary)
- [Mapping Boundary](#mapping-boundary)
- [Domain Model](#domain-model)
- [Aggregate Boundaries](#aggregate-boundaries)
- [Exercise Ownership Model](#exercise-ownership-model)
- [Ordered Nested Data](#ordered-nested-data)
- [Training-Goal Lifecycle](#training-goal-lifecycle)
- [Workout-Template Workflow](#workout-template-workflow)
- [Progress Analytics](#progress-analytics)
- [Persistence and Schema Management](#persistence-and-schema-management)
- [JPA Loading and Query Strategy](#jpa-loading-and-query-strategy)
- [Transactions and Scheduled Work](#transactions-and-scheduled-work)
- [Error Boundary](#error-boundary)
- [Project Structure](#project-structure)
- [Architectural Rules](#architectural-rules)

## Architectural Style

The Fitness Tracker API is intentionally implemented as a modular monolith.

All business capabilities are deployed as one Spring Boot application and use one MySQL database, while the codebase is separated into clear layers and domain-focused components. This keeps deployment and local development straightforward without mixing HTTP handling, business rules, persistence, security, and response mapping in the same classes.

The main architectural goals are:

- Keep the public API contract separate from JPA entities.
- Keep controllers thin.
- Centralize business rules and transaction boundaries in services.
- Resolve the authenticated user through one abstraction.
- Enforce ownership for every user-specific operation.
- Keep entity-to-DTO mapping outside services.
- Preserve deterministic ordering for nested resources.
- Use database-level operations for pagination, ranking, and bulk updates where appropriate.
- Reproduce the same schema through Flyway in local, Docker, test, and CI environments.
- Keep read-only workflows free of persistence side effects.

The application does not currently require microservices, Kafka, or Kubernetes. Those technologies would add operational complexity without solving a demonstrated requirement of the current system.

## Request Lifecycle

The normal path of a protected request is:

~~~mermaid
flowchart TD
    A[API client] -->|HTTP request + JWT| B[JWT security filter]
    B -->|Authorized request| C[Controller]
    C -->|Calls| D[Service]
    D -->|Query| E[Repository]
    E -->|SQL| F[(MySQL)]

    F -->|Rows| E
    E -->|Entities| D
    D -->|Entity| G[Mapper]
    G -->|Response DTO| D
    D -->|DTO| C
    C -->|HTTP response| B
    B -->|HTTP response| A
~~~

1. The client sends an HTTP request and, for protected endpoints, a bearer access token.
2. The JWT filter validates the token and populates the Spring Security context.
3. The controller validates request parameters and DTOs.
4. The controller delegates the use case to a service.
5. The service obtains the current username through `CurrentUserProvider`.
6. The service applies business rules, validates ownership, and coordinates repositories.
7. Repositories load or modify data in MySQL.
8. A dedicated mapper converts entities, projections, or calculated values into response DTOs.
9. The controller returns the response and the appropriate HTTP status.
10. If an exception escapes the use case, the global exception handler converts it into a standardized `ProblemDetail` response.

## Layer Responsibilities

| Layer | Responsibilities | Must not do |
| --- | --- | --- |
| Controllers | Expose REST endpoints, validate input, delegate use cases, select HTTP status | Implement domain rules or query JPA directly |
| Services | Enforce business rules, validate ownership, coordinate repositories, manage transactions, create and update aggregates | Build HTTP responses or access `SecurityContextHolder` directly |
| Mappers | Convert entities and projections into response DTOs, preserve nested ordering, create workout drafts | Query repositories, persist entities, resolve authentication, decide ownership |
| Repositories | Execute derived queries, JPQL, native SQL, projections, entity graphs, and bulk updates | Contain HTTP or presentation logic |
| DTOs | Define the public request and response contract and Bean Validation rules | Expose persistence behavior |
| Projections | Represent optimized read-only query results | Replace domain entities for write operations |
| Security components | Validate tokens, populate authentication, expose the current principal | Implement workout, goal, template, or analytics rules |
| Exception handler | Translate known failures into stable API errors | Contain use-case logic |
| Scheduled components | Run time-based maintenance operations | Change state through read endpoints |

## Authenticated-User Boundary

Core domain services for workouts, exercises, progress, goals, and templates do not read Spring Security internals directly. They depend on:

~~~java
public interface CurrentUserProvider {
    String getCurrentUsername();
}
~~~

The production implementation reads the authenticated principal from Spring Security. Unit tests replace the interface with a mock.

The account password-change service is a remaining exception in the current code and should be migrated to `CurrentUserProvider` so the same rule applies consistently across the application.

This boundary provides:

- One consistent way to resolve the current username.
- Less duplicated security-context code.
- Explicit service dependencies.
- Easier unit testing.
- Lower coupling between domain logic and Spring Security.

Ownership-aware repository methods combine the resource identifier with the authenticated username. Examples of this pattern include:

~~~text
workout ID + username
template ID + username
training-goal ID + username
exercise accessibility + username
~~~

An identifier owned by another user is treated as a missing resource. This avoids exposing whether another user's resource exists.

## Mapping Boundary

DTO construction is delegated to dedicated components:

- `ExerciseDefinitionMapper`
- `WorkoutMapper`
- `ProgressMapper`
- `TrainingGoalMapper`
- `WorkoutTemplateMapper`

Services decide what data should be loaded and whether a change is valid. Mappers decide how already-available data is represented in the API contract.

This separation is important because a mapper must not hide repository access or business decisions. A mapping call should be deterministic and should not produce persistence side effects.

The mappers currently handle:

- Ordered workout exercises and sets.
- Exercise-definition responses.
- Training-goal responses.
- Native projection results used by progress endpoints.
- Nested workout-template responses.
- Conversion of a template into an editable `WorkoutRequest` draft.

## Domain Model

~~~mermaid
erDiagram
    USER ||--o{ WORKOUT : owns
    USER ||--o{ REFRESH_TOKEN : receives
    USER ||--o{ RESET_TOKEN : receives
    USER ||--o{ TRAINING_GOAL : sets
    USER ||--o{ WORKOUT_TEMPLATE : owns
    USER ||--o{ EXERCISE_DEFINITION : creates

    WORKOUT ||--|{ WORKOUT_EXERCISE : contains
    WORKOUT_EXERCISE ||--|{ EXERCISE_SET : contains

    EXERCISE_DEFINITION ||--o{ WORKOUT_EXERCISE : identifies
    EXERCISE_DEFINITION ||--o{ TRAINING_GOAL : targets
    EXERCISE_DEFINITION ||--o{ WORKOUT_TEMPLATE_EXERCISE : identifies

    WORKOUT_TEMPLATE ||--|{ WORKOUT_TEMPLATE_EXERCISE : contains
    WORKOUT_TEMPLATE_EXERCISE ||--|{ WORKOUT_TEMPLATE_SET : contains
~~~

The principal domain relationships are:

- A user owns workouts, refresh tokens, reset tokens, training goals, custom exercise definitions, and workout templates.
- A system exercise has no user owner and is globally accessible.
- A custom exercise belongs to exactly one user.
- A workout contains ordered workout exercises.
- A workout exercise references one reusable exercise definition and contains ordered sets.
- A set records weight, repetitions, and optional repetitions in reserve.
- A training goal links a user to an exercise definition and stores target values, dates, and lifecycle status.
- A workout template contains ordered template exercises and target sets.
- A template can be converted into a workout draft without creating workout history.

## Aggregate Boundaries

### Workout Aggregate

The workout is the aggregate root for recorded training data.

It owns:

- Workout metadata such as name and date.
- Ordered workout exercises.
- Ordered exercise sets.

Operations that replace, duplicate, or delete a workout operate on the complete nested structure. Adding or deleting an exercise or set also updates the aggregate's ordering.

The referenced `ExerciseDefinition` is not owned by the workout. Deleting a workout removes its nested workout data but must not delete reusable exercise definitions.

### Exercise-Definition Catalog

Exercise definitions are reusable references shared by:

- Recorded workout exercises.
- Training goals.
- Workout-template exercises.

The catalog combines global system entries with private user-created entries. Custom definitions are archived instead of being physically deleted so historical workout data can preserve its reference.

### Training-Goal Aggregate

A training goal belongs to one user and targets one exercise definition.

The aggregate contains:

- Target weight.
- Target repetitions.
- Creation date.
- Target date.
- Lifecycle status.

Only one `ACTIVE` goal is allowed per user and exercise according to the current business rule.

### Workout-Template Aggregate

The template is the aggregate root for reusable training plans.

It owns:

- A normalized template name.
- Ordered template exercises.
- Ordered target sets.

Deleting a template cascades to its template exercises and template sets, while referenced exercise definitions remain intact.

### Authentication Data

Users own persistent refresh tokens and reset tokens. Refresh tokens support rotation, revocation, logout, and expiration independently from the stateless access token.

## Exercise Ownership Model

Exercise definitions have one of two types:

| Type | Owner | Visibility | Mutation |
| --- | --- | --- | --- |
| `SYSTEM` | No owner | Every authenticated user | Cannot be modified or archived by users |
| `CUSTOM` | Exactly one user | Only the owner | Can be updated or archived by the owner |

The accessible catalog for a user contains:

~~~text
all active SYSTEM exercises
+ the authenticated user's active CUSTOM exercises
~~~

Name rules are applied to normalized values:

- Leading and trailing whitespace is removed.
- Repeated internal spaces are collapsed.
- Comparison is case-insensitive.
- A user cannot create two custom exercises with the same normalized name.
- A custom name cannot conflict with a system exercise.

Archiving is used instead of deletion because exercise definitions may already be referenced by historical workouts.

## Ordered Nested Data

Workouts and templates both contain ordered child collections.

| Aggregate | First-level order | Second-level order |
| --- | --- | --- |
| Workout | `exerciseNumber` | `setNumber` |
| Workout template | Request order mapped to exercise numbers | Request order mapped to set numbers |

The API preserves this ordering in both persistence and response mapping.

When a workout exercise or set is deleted, the remaining elements are renumbered so the sequence stays continuous. Bidirectional JPA relationships are maintained on both sides when nested entities are created or removed.

Deterministic ordering is also used for paginated histories and tie-breaking in progress calculations. This prevents the same data from appearing in an unpredictable order between requests.

## Training-Goal Lifecycle

A goal starts in `ACTIVE` status and can reach one terminal state:

~~~mermaid
stateDiagram-v2
    [*] --> ACTIVE
    ACTIVE --> COMPLETED: qualifying workout set
    ACTIVE --> CANCELLED: owner cancellation
    ACTIVE --> EXPIRED: target date passed
    COMPLETED --> [*]
    CANCELLED --> [*]
    EXPIRED --> [*]
~~~

A goal is completed only when:

1. It belongs to the workout owner.
2. The workout contains the targeted exercise.
3. The workout was recorded after the goal was created.
4. One set reaches both the target weight and target repetitions.
5. The target date has not passed.

Target values from different sets are never combined.

A goal remains active throughout its target date. Only an active goal whose target date is earlier than the current date is expired.

Expiration is performed by a scheduled component at midnight through one transactional JPQL bulk update. Read endpoints remain read-only and do not silently change goal state.

Workout creation checks relevant active goals after assembling the workout aggregate. The create-workout response reports how many goals were completed.

## Workout-Template Workflow

A workout template stores a reusable structure independently from workout history.

~~~mermaid
flowchart TD
    A[Create template] --> B[Persist ordered targets]
    B --> C[Request workout draft]
    C --> D[Edit draft on client]
    D --> E[Create workout]
    E --> F[Persist workout history]
~~~

The workflow is:

1. The authenticated user creates a template.
2. Every referenced exercise must be accessible to that user.
3. The same exercise cannot appear twice in one template.
4. Exercise and set order is generated from request order.
5. The template is saved without creating a workout.
6. The user requests a workout draft.
7. The mapper converts template targets into a `WorkoutRequest`.
8. The draft uses the current date and can be edited by the client.
9. Only a later request to `POST /api/workouts` creates workout history.

Draft generation:

- Is read-only.
- Does not modify the template.
- Does not persist a workout.
- Does not create recorded exercise sets.

## Progress Analytics

### Workout Volume

Volume is calculated per set and then summed:

~~~text
volume = weight × repetitions
~~~

The API exposes volume for one workout, the last seven days, and the current month.

### Personal Records

The best set is selected using this priority:

1. Highest weight.
2. Highest repetitions when weight is equal.
3. Highest RIR when weight and repetitions are equal.
4. Most recent workout date.
5. Deterministic workout and set ordering for remaining ties.

The paginated all-records endpoint performs ranking directly in MySQL:

~~~sql
ROW_NUMBER() OVER (
    PARTITION BY exercise_definition_id
    ORDER BY weight DESC, reps DESC, rir DESC, date DESC
)
~~~

The query filters by authenticated username, returns one row per exercise, maps results through an interface projection, uses a dedicated count query, and paginates in the database.

### Estimated One-Repetition Maximum

Exercise history calculates estimated one-repetition maximum with the Epley formula:

~~~text
estimated 1RM = weight × (1 + repetitions / 30)
~~~

The value is intended for progress tracking and is not guaranteed to represent an exact maximal lift.

### Progress Summary

The activity summary contains:

- Total workout count.
- Distinct training days during the last 7 days.
- Distinct training days during the last 30 days.
- Total sets recorded during the last 7 days.
- Latest workout date.
- Most-trained exercise during the last 30 days, measured by set count.

## Persistence and Schema Management

The schema is managed by Flyway. Hibernate does not generate the production schema.

Migration files are stored in:

~~~text
src/main/resources/db/migration
~~~

They follow this naming convention:

~~~text
V<version>__<description>.sql
~~~

Main tables include:

- `users`
- `refresh_tokens`
- `reset_tokens`
- `exercise_definitions`
- `workouts`
- `workout_exercises`
- `exercise_sets`
- `training_goals`
- `workout_templates`
- `workout_template_exercises`
- `workout_template_sets`

Application startup follows this sequence:

1. The application connects to MySQL.
2. Flyway reads `flyway_schema_history`.
3. Flyway applies every migration that has not yet run.
4. Hibernate validates entity-to-schema compatibility through `ddl-auto=validate`.
5. Startup succeeds only when the mappings match the migrated schema.

Applied migrations are immutable. Every schema change must be introduced in a new versioned migration rather than by editing a migration that may already exist in another environment.

## JPA Loading and Query Strategy

The project uses several targeted approaches instead of applying one loading strategy globally.

### Detailed Single-Aggregate Reads

Detailed workout and template reads use `@EntityGraph` for the first nested level and referenced exercise definitions. Nested sets are fetched in batches.

This allows the mapper to traverse an initialized aggregate without issuing one additional query per exercise.

### Paginated Nested Aggregates

Workouts and workout templates use two-step pagination:

1. Query one page of aggregate IDs with the required filters and deterministic ordering.
2. Load the detailed aggregates for those IDs with an entity graph.
3. Load nested set collections through batch fetching.
4. Reconstruct the response page while preserving the ID-page order and metadata.

This avoids paginating directly over a collection fetch, which can duplicate root rows and produce incorrect page boundaries.

### Training Goals

Paginated training-goal reads use an entity graph for the referenced exercise definition. This keeps the query count constant instead of loading one exercise definition for each goal.

### Read-Heavy Analytics

The personal-record listing uses:

- Native SQL window functions.
- Interface projections.
- A dedicated count query.
- Database-level pagination.

This avoids loading complete workout aggregates when the endpoint only needs one ranked result per exercise.

### Query-Count Regression Tests

Hibernate statistics are used in dedicated integration tests to fix the expected query shape:

| Use case | Expected prepared statements |
| --- | ---: |
| Retrieve one detailed workout | 2 |
| Retrieve a filtered workout page | 4 |
| Retrieve one detailed workout template | 2 |
| Retrieve a workout-template page | 4 |
| Retrieve a training-goal page | 2 |

The tests also verify that adding more nested exercises and sets does not increase the number of statements for these use cases.

## Transactions and Scheduled Work

Services define the transaction boundaries for aggregate changes.

Important transactional behavior includes:

- Creating a complete workout with nested exercises and sets atomically.
- Replacing a workout as one operation.
- Duplicating a workout without modifying the source aggregate.
- Completing relevant training goals during workout creation.
- Creating and deleting a nested workout template atomically.
- Cascading template-child deletion without deleting referenced exercise definitions.
- Expiring all overdue active goals through one bulk update.

The template-to-workout-draft operation is intentionally read-only. A draft does not become workout history until the client explicitly submits it to the workout creation endpoint.

## Error Boundary

The global exception handler is the boundary between internal failures and the public error contract.

It converts:

- Bean Validation failures.
- Invalid path and query parameters.
- Missing resources.
- Ownership failures represented as inaccessible resources.
- Duplicate-resource conflicts.
- Invalid lifecycle transitions.
- Unexpected failures.

into Spring `ProblemDetail` responses with HTTP status, title, detail, type, request path, stable error code, and field-level errors when applicable.

Security-specific behavior and the public error format are documented in [security.md](security.md).

## Project Structure

~~~text
fitness-tracker-api/
├── .github/
│   └── workflows/
│       └── ci.yml
├── docs/
│   ├── architecture.md
│   ├── security.md
│   └── testing.md
├── src/
│   ├── main/
│   │   ├── java/com/cosmin/fitness_tracker_api/
│   │   │   ├── component/
│   │   │   ├── controller/
│   │   │   ├── DTO/
│   │   │   ├── Enum/
│   │   │   ├── exception/
│   │   │   ├── mapper/
│   │   │   ├── model/
│   │   │   ├── repository/
│   │   │   │   └── Projection/
│   │   │   ├── security/
│   │   │   └── service/
│   │   └── resources/
│   │       ├── db/
│   │       │   └── migration/
│   │       └── application.properties
│   └── test/
│       ├── java/com/cosmin/fitness_tracker_api/
│       │   ├── ControllerTest/
│       │   ├── IntegrationTest/
│       │   └── ServiceTest/
│       └── resources/
│           └── application-test.properties
├── .env.example
├── docker-compose.yml
├── Dockerfile
├── pom.xml
└── README.md
~~~

## Architectural Rules

When extending the project, preserve these rules:

1. Controllers validate and delegate; they do not contain business logic.
2. Services own use-case rules and transaction boundaries.
3. Services obtain the authenticated username only through `CurrentUserProvider`.
4. User-owned resources are loaded with ownership-aware queries.
5. JPA entities are not returned directly from controllers.
6. Mappers do not query repositories or persist data.
7. Aggregate updates maintain both sides of bidirectional relationships.
8. Nested exercises and sets preserve deterministic ordering.
9. Read-only endpoints do not change domain state.
10. Read-heavy endpoints prefer projections or aggregate queries when complete entities are unnecessary.
11. Paginated collection fetches use a strategy that preserves correct root pagination.
12. Applied Flyway migrations are never edited.
13. New failure modes are translated into the common `ProblemDetail` contract.
14. Important loading strategies are protected by integration or query-count regression tests.

For authentication, authorization, and production security requirements, see [security.md](security.md). For the complete testing approach, see [testing.md](testing.md).
