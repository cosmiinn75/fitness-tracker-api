# Fitness Tracker API

[![Fitness Tracker CI](https://github.com/cosmiinn75/fitness-tracker-api/actions/workflows/ci.yml/badge.svg)](https://github.com/cosmiinn75/fitness-tracker-api/actions/workflows/ci.yml)

A secure RESTful backend application built with **Java and Spring Boot** for managing workouts, exercises, sets, training volume, and personal records.

The project includes JWT authentication, refresh-token rotation, logout and token invalidation, user-specific resource access, nested workout management, pagination, validation, global exception handling, Swagger/OpenAPI documentation, Docker support, automated testing, and continuous integration with GitHub Actions.

---

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Domain Model](#domain-model)
- [API Endpoints](#api-endpoints)
- [Authentication Flow](#authentication-flow)
- [Request Examples](#request-examples)
- [Progress Tracking](#progress-tracking)
- [Validation and Error Handling](#validation-and-error-handling)
- [Environment Variables](#environment-variables)
- [Running with Docker](#running-with-docker)
- [Running Locally](#running-locally)
- [Running Tests](#running-tests)
- [Continuous Integration](#continuous-integration)
- [Swagger and OpenAPI](#swagger-and-openapi)
- [Project Structure](#project-structure)
- [Security Considerations](#security-considerations)
- [Future Improvements](#future-improvements)
- [What I Learned](#what-i-learned)
- [Status](#status)
- [Author](#author)

---

## Features

### Authentication and Security

- User registration
- User login
- JWT access-token authentication
- Refresh-token persistence
- Refresh-token rotation
- Logout and refresh-token invalidation
- BCrypt password hashing
- Stateless Spring Security configuration
- Protected API endpoints
- User-specific resource ownership checks
- Bearer-token authentication
- Secure configuration through environment variables

Protected requests use:

```http
Authorization: Bearer <access-token>
```

Refresh-token rotation ensures that, when a refresh token is used successfully, the old token is invalidated and replaced with a newly generated token.

---

### Exercise Definitions

- Create reusable exercise definitions
- Retrieve all exercise definitions
- Retrieve an exercise definition by ID
- Update an existing exercise definition
- Prevent duplicate exercise names
- Associate exercises with muscle groups

Example exercise definitions:

- Bench Press
- Squat
- Deadlift
- Pull Up
- Shoulder Press

---

### Workout Management

- Create workouts for the authenticated user
- Add multiple exercises to a workout
- Add multiple sets to each exercise
- Retrieve paginated workout history
- Retrieve a workout by ID
- Update workout metadata
- Replace an entire workout
- Delete workouts
- Restrict access to the authenticated owner

Each workout can contain multiple exercises, while each workout exercise can contain multiple sets.

---

### Exercise Management Inside a Workout

- Add an exercise to an existing workout
- Change the exercise definition while preserving existing sets
- Delete an exercise from a workout
- Automatically renumber remaining exercises after deletion

Exercises are ordered using an `exerciseNumber` field.

---

### Set Management

- Add a set to an existing workout exercise
- Partially update a set
- Delete a set
- Automatically renumber remaining sets after deletion

For partial updates, only the fields included in the request are modified.

Example:

```json
{
  "reps": 8
}
```

The existing weight and RIR values remain unchanged.

---

### Progress Tracking

- Calculate total workout volume
- Calculate training volume for the last seven days
- Calculate training volume for the last month
- Retrieve the personal record for a specific exercise
- Restrict progress data to the authenticated user

Workout volume is calculated using:

```text
volume = weight × repetitions
```

Personal records are selected using the following priority:

1. Highest weight
2. Highest number of repetitions when the weight is equal
3. Highest RIR when both weight and repetitions are equal

For example:

```text
100 kg × 8 repetitions @ RIR 3
```

is considered better than:

```text
100 kg × 8 repetitions @ RIR 1
```

because the same performance was achieved with more repetitions still available in reserve.

---

### Pagination

Workout history supports pagination using `page` and `size` query parameters.

Example:

```http
GET /api/workouts?page=0&size=10
```

The response includes:

- Workout content
- Current page
- Page size
- Total number of elements
- Total number of pages
- First-page indicator
- Last-page indicator

---

### API Documentation

The project includes:

- Swagger UI
- OpenAPI specification
- Documented controllers
- Documented request and response DTOs
- JWT authentication support inside Swagger
- Validation information
- Example request and response schemas

---

### Automated Testing

The project contains automated tests at multiple levels.

#### Service Unit Tests

Service tests use:

- JUnit
- Mockito
- Mocked repositories
- Mocked security and supporting dependencies

They verify business logic independently from the web and database layers.

#### Controller Tests

Controller tests use:

- MockMvc
- Spring MVC test support
- Mocked services
- JSON request and response assertions
- HTTP status assertions
- Validation and exception-response testing

They verify request mapping, serialization, validation, and HTTP responses.

#### Integration Tests

Integration tests use:

- `@SpringBootTest`
- `@AutoConfigureMockMvc`
- A dedicated MySQL test database
- Real controllers
- Real services
- Real repositories
- Real JPA entity relationships

Integration tests cover important end-to-end flows such as:

- User registration
- User login
- Token refresh
- Logout and token invalidation
- Exercise-definition retrieval
- Workout creation
- Workout persistence
- Workout retrieval
- Workout updates
- Workout deletion
- Unauthorized access
- Resource ownership protection

---

### Docker and Continuous Integration

- Dockerized Spring Boot application
- MySQL Docker service
- Docker Compose configuration
- Environment-based configuration
- GitHub Actions workflow
- Automated compilation and testing
- Dedicated MySQL service container during CI
- Maven `clean verify` execution
- Workflow execution on pushes and pull requests targeting `main`

---

## Tech Stack

### Backend

- Java 26
- Spring Boot
- Spring Web MVC
- Spring Security
- Spring Data JPA
- Hibernate
- Jakarta Bean Validation

### Authentication

- JSON Web Tokens
- JWT access tokens
- Refresh tokens
- Refresh-token rotation
- BCrypt password encoding

### Database

- MySQL 8
- Spring Data repositories
- JPA entity relationships

### Documentation

- Swagger UI
- OpenAPI

### Testing

- JUnit
- Mockito
- MockMvc
- Spring Boot Test
- Integration testing with MySQL
- Maven Surefire

### DevOps

- Maven Wrapper
- Docker
- Docker Compose
- GitHub Actions

---

## Architecture

The project follows a layered architecture:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
Database
```

### Controller Layer

Responsible for:

- Receiving HTTP requests
- Validating request data
- Calling the appropriate service
- Returning HTTP responses
- Mapping exceptions to consistent API responses

### Service Layer

Responsible for:

- Business logic
- Authentication logic
- Refresh-token management
- Resource ownership validation
- Entity creation and updates
- DTO mapping
- Workout calculations
- Personal-record selection

### Repository Layer

Responsible for:

- Database access
- Entity persistence
- User-specific queries
- Workout and exercise lookup
- Refresh-token persistence
- Progress-related queries

### DTO Layer

Request and response DTOs are used to avoid exposing persistence entities directly through the API.

---

## Domain Model

### User

Represents an application account.

A user can own multiple workouts and can have an associated refresh token.

### ExerciseDefinition

Represents a reusable exercise, such as Bench Press, Squat, or Deadlift.

### Workout

Represents a workout created by a specific user.

A workout contains:

- A name
- A date
- A list of workout exercises
- An owner

### WorkoutExercise

Represents an exercise performed inside a workout.

It contains:

- An exercise definition
- An exercise number
- A list of sets
- A reference to its workout

### ExerciseSet

Represents a performed set.

It contains:

- Set number
- Weight
- Repetitions
- RIR
- A reference to its workout exercise

### RefreshToken

Represents a refresh token used to obtain a new access token.

Refresh tokens are persisted and invalidated when they are rotated or used during logout.

---

## API Endpoints

All protected endpoints require:

```http
Authorization: Bearer <access-token>
```

### Authentication

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/register` | Register a new user |
| POST | `/api/auth/login` | Authenticate a user and return tokens |
| POST | `/api/auth/refresh` | Rotate the refresh token and return new tokens |
| POST | `/api/auth/logout` | Invalidate the supplied refresh token |

---

### Exercise Definitions

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/exercises` | Retrieve all exercise definitions |
| GET | `/api/exercises/{id}` | Retrieve an exercise definition by ID |
| POST | `/api/exercises` | Create a new exercise definition |
| PUT | `/api/exercises/{id}` | Update an exercise definition |

---

### Workouts

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/workouts?page=0&size=10` | Retrieve paginated workouts for the current user |
| GET | `/api/workouts/{id}` | Retrieve a workout by ID |
| POST | `/api/workouts` | Create a workout |
| PATCH | `/api/workouts/{id}` | Update workout metadata |
| PUT | `/api/workouts/{id}` | Replace an entire workout |
| DELETE | `/api/workouts/{id}` | Delete a workout |

---

### Workout Exercises

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/workouts/{workoutId}/exercises` | Add an exercise to a workout |
| PATCH | `/api/workouts/{workoutId}/exercises/{exerciseNumber}` | Change the exercise definition |
| DELETE | `/api/workouts/{workoutId}/exercises/{exerciseNumber}` | Delete and renumber an exercise |

---

### Exercise Sets

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/workouts/{workoutId}/exercises/{exerciseNumber}/sets` | Add a set |
| PATCH | `/api/workouts/{workoutId}/exercises/{exerciseNumber}/sets/{setNumber}` | Partially update a set |
| DELETE | `/api/workouts/{workoutId}/exercises/{exerciseNumber}/sets/{setNumber}` | Delete and renumber a set |

---

### Progress

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/progress/workouts/{workoutId}/volume` | Calculate the total volume of a workout |
| GET | `/api/progress/weekly-volume` | Calculate training volume for the last seven days |
| GET | `/api/progress/monthly-volume` | Calculate training volume for the last month |
| GET | `/api/progress/exercises/{exerciseDefinitionId}/personal-record` | Retrieve the personal record for an exercise |

---

## Authentication Flow

### Registration

1. The client sends a username, email, and password.
2. The password is encoded using BCrypt.
3. The user is saved in MySQL.
4. The API returns an access token and refresh token.

### Login

1. The client sends valid credentials.
2. Spring Security authenticates the user.
3. The API returns an access token and refresh token.
4. The refresh token is persisted in the database.

### Accessing Protected Endpoints

The access token is included in the request:

```http
Authorization: Bearer <access-token>
```

The authenticated username is extracted from the Spring Security context.

### Refresh-Token Rotation

1. The client sends the current refresh token.
2. The API verifies that the token exists and is valid.
3. The old refresh token is invalidated.
4. A new refresh token is generated and persisted.
5. The API returns a new access token and refresh token.

This prevents the same refresh token from being reused indefinitely.

### Logout

1. The client sends its refresh token.
2. The API invalidates the token.
3. The invalidated token can no longer be used to obtain new access tokens.

### Resource Ownership

Workout ownership is verified using:

```text
workout ID + authenticated username
```

This prevents users from reading, modifying, or deleting workouts belonging to another account.

---

## Request Examples

### Register

```http
POST /api/auth/register
Content-Type: application/json
```

```json
{
  "username": "cosmin",
  "email": "cosmin@example.com",
  "password": "password123"
}
```

Example response:

```json
{
  "accessToken": "<access-token>",
  "refreshToken": "<refresh-token>"
}
```

---

### Login

```http
POST /api/auth/login
Content-Type: application/json
```

```json
{
  "username": "cosmin",
  "password": "password123"
}
```

Example response:

```json
{
  "accessToken": "<access-token>",
  "refreshToken": "<refresh-token>"
}
```

---

### Refresh Tokens

```http
POST /api/auth/refresh
Content-Type: application/json
```

```json
{
  "refreshToken": "<current-refresh-token>"
}
```

Example response:

```json
{
  "accessToken": "<new-access-token>",
  "refreshToken": "<new-refresh-token>"
}
```

The previous refresh token is invalidated after a successful refresh.

---

### Logout

```http
POST /api/auth/logout
Content-Type: application/json
```

```json
{
  "refreshToken": "<refresh-token>"
}
```

---

### Create an Exercise Definition

```http
POST /api/exercises
Authorization: Bearer <access-token>
Content-Type: application/json
```

```json
{
  "name": "Bench Press",
  "muscleGroup": "CHEST"
}
```

---

### Create a Workout

```http
POST /api/workouts
Authorization: Bearer <access-token>
Content-Type: application/json
```

```json
{
  "workoutName": "Push Day",
  "date": "2026-07-15",
  "exerciseRequests": [
    {
      "exerciseDefinitionId": 1,
      "setRequests": [
        {
          "weight": 60.0,
          "reps": 10,
          "rir": 2
        },
        {
          "weight": 70.0,
          "reps": 8,
          "rir": 1
        }
      ]
    }
  ]
}
```

Example response:

```json
{
  "id": 1,
  "workoutName": "Push Day",
  "date": "2026-07-15",
  "exerciseResponses": [
    {
      "id": 1,
      "exerciseNumber": 1,
      "exerciseName": "Bench Press",
      "setResponses": [
        {
          "id": 1,
          "setNumber": 1,
          "weight": 60.0,
          "reps": 10,
          "rir": 2
        },
        {
          "id": 2,
          "setNumber": 2,
          "weight": 70.0,
          "reps": 8,
          "rir": 1
        }
      ]
    }
  ]
}
```

---

### Update Workout Metadata

```http
PATCH /api/workouts/1
Authorization: Bearer <access-token>
Content-Type: application/json
```

```json
{
  "workoutName": "Heavy Push Day",
  "date": "2026-07-16"
}
```

Only the supplied metadata fields are updated.

---

### Replace an Entire Workout

```http
PUT /api/workouts/1
Authorization: Bearer <access-token>
Content-Type: application/json
```

```json
{
  "workoutName": "Pull Day",
  "date": "2026-07-17",
  "exerciseRequests": [
    {
      "exerciseDefinitionId": 2,
      "setRequests": [
        {
          "weight": 80.0,
          "reps": 8,
          "rir": 2
        }
      ]
    }
  ]
}
```

---

### Add an Exercise

```http
POST /api/workouts/1/exercises
Authorization: Bearer <access-token>
Content-Type: application/json
```

```json
{
  "exerciseDefinitionId": 2,
  "setRequests": [
    {
      "weight": 50.0,
      "reps": 12,
      "rir": 2
    },
    {
      "weight": 55.0,
      "reps": 10,
      "rir": 1
    }
  ]
}
```

The new exercise receives the next available `exerciseNumber`.

---

### Add a Set

```http
POST /api/workouts/1/exercises/1/sets
Authorization: Bearer <access-token>
Content-Type: application/json
```

```json
{
  "weight": 75.0,
  "reps": 8,
  "rir": 1
}
```

The new set receives the next available `setNumber`.

---

### Partially Update a Set

```http
PATCH /api/workouts/1/exercises/1/sets/1
Authorization: Bearer <access-token>
Content-Type: application/json
```

Update only repetitions:

```json
{
  "reps": 12
}
```

Update multiple fields:

```json
{
  "weight": 75.0,
  "rir": 0
}
```

Fields that are not included remain unchanged.

---

### Retrieve Paginated Workouts

```http
GET /api/workouts?page=0&size=10
Authorization: Bearer <access-token>
```

Example response:

```json
{
  "content": [
    {
      "id": 1,
      "workoutName": "Push Day",
      "date": "2026-07-15",
      "exerciseResponses": []
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true
}
```

When the authenticated user has no workouts, the API returns `200 OK` with an empty content list.

---

## Progress Tracking

### Workout Volume

```http
GET /api/progress/workouts/1/volume
Authorization: Bearer <access-token>
```

Example response:

```json
{
  "totalVolume": 2270.0
}
```

---

### Weekly Volume

```http
GET /api/progress/weekly-volume
Authorization: Bearer <access-token>
```

Example response:

```json
{
  "startDate": "2026-07-10",
  "endDate": "2026-07-16",
  "totalVolume": 7450.0
}
```

---

### Monthly Volume

```http
GET /api/progress/monthly-volume
Authorization: Bearer <access-token>
```

Example response:

```json
{
  "startDate": "2026-06-16",
  "endDate": "2026-07-16",
  "totalVolume": 28450.0
}
```

---

### Personal Record

```http
GET /api/progress/exercises/1/personal-record
Authorization: Bearer <access-token>
```

Example response:

```json
{
  "exerciseDefinitionId": 1,
  "exerciseName": "Bench Press",
  "weight": 100.0,
  "reps": 6,
  "rir": 2,
  "workoutDate": "2026-07-15"
}
```

The personal record belongs only to the authenticated user.

---

## Validation and Error Handling

The API uses Jakarta Bean Validation on request DTOs and path parameters.

Examples of validation rules:

- Usernames must not be blank
- Emails must have a valid format
- Passwords must satisfy the configured constraints
- IDs must be positive
- Workout names must not be blank
- Workout dates must not be null
- Exercise-definition IDs must not be null
- Exercise and set collections must contain valid elements
- Weight must be positive
- Repetitions must be positive
- RIR must be within the accepted range

Invalid requests return an appropriate HTTP status code.

### Error Status Codes

| Error Case | Status Code |
|---|---|
| Invalid request body | `400 Bad Request` |
| Invalid path parameter | `400 Bad Request` |
| Invalid refresh token | `400 Bad Request` |
| Invalid logout token | `400 Bad Request` |
| Invalid login credentials | `401 Unauthorized` |
| Missing or invalid JWT | `401 Unauthorized` |
| User not found | `404 Not Found` |
| Workout not found | `404 Not Found` |
| Workout exercise not found | `404 Not Found` |
| Exercise set not found | `404 Not Found` |
| Exercise definition not found | `404 Not Found` |
| Personal record not found | `404 Not Found` |
| Duplicate username or email | `409 Conflict` |
| Duplicate exercise name | `409 Conflict` |

A global exception handler provides consistent API error responses.

---

## Environment Variables

Sensitive information is loaded from environment variables instead of being hardcoded.

Required variables:

```env
DB_URL=jdbc:mysql://mysql:3306/fitness_tracker_db
DB_USERNAME=root
DB_PASSWORD=your_database_password
JWT_SECRET=your_very_long_jwt_secret
```

The application imports an optional local `.env` file:

```properties
spring.config.import=optional:file:./.env[.properties]
```

Datasource and JWT configuration:

```properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
jwt.secret=${JWT_SECRET}
```

The `.env` file must not be committed.

Add it to `.gitignore`:

```gitignore
.env
```

An `.env.example` file can be committed safely:

```env
DB_URL=jdbc:mysql://mysql:3306/fitness_tracker_db
DB_USERNAME=root
DB_PASSWORD=replace_with_your_password
JWT_SECRET=replace_with_a_long_random_secret
```

---

## Running with Docker

### Requirements

- Docker
- Docker Compose

### 1. Clone the repository

```bash
git clone https://github.com/cosmiinn75/fitness-tracker-api.git
cd fitness-tracker-api
```

### 2. Create the `.env` file

Create a file named `.env` in the project root:

```env
DB_URL=jdbc:mysql://mysql:3306/fitness_tracker_db
DB_USERNAME=root
DB_PASSWORD=root
JWT_SECRET=replace_with_a_long_random_secret
```

Project structure:

```text
fitness-tracker-api/
├── .env
├── docker-compose.yml
├── Dockerfile
├── pom.xml
└── src/
```

### 3. Build and start the containers

```bash
docker compose up --build
```

Run the containers in the background:

```bash
docker compose up --build -d
```

### 4. Stop the containers

```bash
docker compose down
```

Remove containers and database volumes:

```bash
docker compose down -v
```

After startup, the API is available at:

```text
http://localhost:8080
```

Swagger UI is available at:

```text
http://localhost:8080/swagger-ui/index.html
```

---

## Running Locally

### Requirements

- Java 26
- MySQL 8
- Maven or Maven Wrapper

### 1. Clone the repository

```bash
git clone https://github.com/cosmiinn75/fitness-tracker-api.git
cd fitness-tracker-api
```

### 2. Create the database

```sql
CREATE DATABASE fitness_tracker_db;
```

### 3. Configure environment variables

Example `.env` file:

```env
DB_URL=jdbc:mysql://127.0.0.1:3306/fitness_tracker_db
DB_USERNAME=root
DB_PASSWORD=your_database_password
JWT_SECRET=your_very_long_jwt_secret
```

These values can be configured through:

- A local `.env` file
- IntelliJ run configuration
- Operating-system environment variables
- Terminal environment variables

### 4. Run the application

Linux or macOS:

```bash
./mvnw spring-boot:run
```

Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

The API starts at:

```text
http://localhost:8080
```

---

## Running Tests

### Test Database

Integration tests use a dedicated MySQL database.

Create it locally:

```sql
CREATE DATABASE fitness_tracker_test;
```

The test profile is configured in:

```text
src/test/resources/application-test.properties
```

Example configuration:

```properties
spring.datasource.url=jdbc:mysql://127.0.0.1:3306/fitness_tracker_test
spring.datasource.username=${SPRING_DATASOURCE_USERNAME:root}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD:root}

spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
```

The integration-test classes activate the test profile using:

```java
@ActiveProfiles("test")
```

### Run the Complete Test Suite

Linux or macOS:

```bash
./mvnw clean verify
```

Windows:

```powershell
.\mvnw.cmd clean verify
```

Alternatively:

```bash
mvn clean verify
```

### Run a Single Test Class

Windows:

```powershell
.\mvnw.cmd "-Dtest=AuthIntegrationTest" test
```

Linux or macOS:

```bash
./mvnw -Dtest=AuthIntegrationTest test
```

### Run a Single Test Method

Windows:

```powershell
.\mvnw.cmd "-Dtest=AuthIntegrationTest#register_ShouldReturnTokens" test
```

The project includes:

- Service-layer unit tests
- Controller-layer tests
- MockMvc tests
- Integration tests
- Validation tests
- Error-response tests
- Authentication tests
- Refresh-token tests
- Workout tests
- Exercise-definition tests
- Progress tests
- Security and ownership tests

---

## Continuous Integration

The project uses GitHub Actions for continuous integration.

The workflow runs automatically when:

- Code is pushed to `main`
- A pull request targets `main`
- The workflow is started manually

The CI pipeline:

1. Checks out the repository
2. Starts a MySQL 8 service container
3. Creates a dedicated test database
4. Configures Java 26
5. Restores the Maven dependency cache
6. Makes the Maven Wrapper executable
7. Runs the complete test suite

```bash
./mvnw --batch-mode clean verify
```

The CI environment provides database credentials and the JWT secret through environment variables.

The build fails when:

- The project does not compile
- A unit test fails
- A controller test fails
- An integration test fails
- The Spring application context cannot start

---

## Swagger and OpenAPI

Swagger UI is available at:

```text
http://localhost:8080/swagger-ui/index.html
```

The raw OpenAPI specification is available at:

```text
http://localhost:8080/v3/api-docs
```

### Using JWT in Swagger

1. Register or log in.
2. Copy the returned access token.
3. Open Swagger UI.
4. Click the **Authorize** button.
5. Enter the access token.
6. Execute protected requests.

Swagger sends:

```http
Authorization: Bearer <access-token>
```

---

## Project Structure

```text
src
├── main
│   ├── java
│   │   └── com.cosmin.fitness_tracker_api
│   │       ├── Controller
│   │       ├── DTO
│   │       ├── Exception
│   │       ├── Model
│   │       ├── Repository
│   │       ├── Security
│   │       └── Service
│   │
│   └── resources
│       └── application.properties
│
└── test
    ├── java
    │   └── com.cosmin.fitness_tracker_api
    │       ├── ControllerTest
    │       ├── IntegrationTest
    │       └── ServiceTest
    │
    └── resources
        └── application-test.properties
```

Additional project files:

```text
fitness-tracker-api/
├── .github/
│   └── workflows/
│       └── ci.yml
├── .env
├── .env.example
├── .gitignore
├── docker-compose.yml
├── Dockerfile
├── mvnw
├── mvnw.cmd
├── pom.xml
└── README.md
```

---

## Security Considerations

- Passwords are encoded before persistence
- JWT secrets are loaded from environment variables
- Database credentials are excluded from source control
- Protected endpoints require authentication
- Access tokens are short-lived
- Refresh tokens are persisted
- Refresh tokens are rotated after use
- Logout invalidates the supplied refresh token
- Resource ownership is verified using the authenticated username
- Users cannot access workouts belonging to other accounts
- Unauthorized resource access does not expose another user's data
- Authentication endpoints are publicly accessible
- Swagger and OpenAPI endpoints can be configured as public
- The application uses stateless authentication for protected requests
- Development, test, CI, and production secrets should be different

The JWT secret used outside development should be long, random, and securely stored.

---

## Future Improvements

Possible future improvements include:

- Deploy the API to a cloud platform
- Add Testcontainers for isolated integration-test databases
- Add workout filtering by date and name
- Add sorting options for workout history
- Add exercise progress history
- Add estimated one-repetition maximum calculations
- Add chart-ready progress endpoints
- Add user profile management
- Add password reset functionality
- Add email verification
- Add role-based authorization
- Add API rate limiting
- Add structured application logging
- Add health and metrics endpoints with Spring Boot Actuator
- Add database migrations using Flyway or Liquibase
- Add OAuth2 login support

---

## What I Learned

While building this project, I practiced:

- Designing REST APIs with Spring Boot
- Structuring a backend using controller, service, and repository layers
- Separating persistence entities from request and response DTOs
- Implementing JWT authentication
- Implementing access-token and refresh-token flows
- Implementing refresh-token rotation
- Invalidating refresh tokens during logout
- Protecting endpoints using Spring Security
- Extracting authenticated users from the security context
- Restricting resources to their owners
- Working with JPA and Hibernate relationships
- Managing nested resources
- Maintaining both sides of entity relationships
- Implementing partial updates with `PATCH`
- Replacing complete resources with `PUT`
- Renumbering ordered resources after deletion
- Implementing pagination
- Calculating workout volume
- Calculating weekly and monthly progress
- Selecting personal records using weight, repetitions, and RIR
- Validating request bodies and path parameters
- Handling custom exceptions globally
- Writing service unit tests with JUnit and Mockito
- Testing controllers with MockMvc
- Writing integration tests with Spring Boot and MySQL
- Testing complete controller-service-repository flows
- Testing authentication and resource ownership
- Running tests using Maven
- Containerizing an application with Docker
- Connecting Spring Boot to MySQL using Docker Compose
- Managing configuration through environment variables
- Creating a GitHub Actions continuous integration pipeline
- Running a MySQL service container inside CI
- Generating interactive API documentation with Swagger and OpenAPI
- Configuring JWT authentication inside Swagger UI
- Keeping credentials and secrets outside source control

---

## Status

The core API functionality is implemented and covered by automated tests.

The project currently includes:

- User registration
- User login
- JWT access tokens
- Refresh-token persistence
- Refresh-token rotation
- Logout and token invalidation
- Workout management
- Exercise-definition management
- Workout-exercise management
- Exercise-set management
- Progress calculations
- Personal records
- Pagination
- Validation
- Global exception handling
- Resource ownership protection
- Swagger/OpenAPI documentation
- Docker support
- Docker Compose support
- Service unit tests
- Controller tests
- Integration tests
- GitHub Actions CI
- MySQL integration inside CI

The project is maintained as a Java and Spring Boot backend portfolio project.

---

## Author

**Anghel Cosmin**

GitHub: [cosmiinn75](https://github.com/cosmiinn75)
