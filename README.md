# Fitness Tracker API

[![Fitness Tracker CI](https://github.com/cosmiinn75/fitness-tracker-api/actions/workflows/ci.yml/badge.svg)](https://github.com/cosmiinn75/fitness-tracker-api/actions/workflows/ci.yml)

A RESTful backend application built with **Java and Spring Boot** for managing workouts, exercises, sets, training volume and personal records.

The API provides JWT-based authentication, refresh token support, user-specific resource access, nested workout management, pagination, validation, global exception handling, Swagger/OpenAPI documentation, automated tests, Docker support and continuous integration with GitHub Actions.

---

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Domain Model](#domain-model)
- [API Endpoints](#api-endpoints)
- [Authentication](#authentication)
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

---

## Features

### Authentication and Security

- User registration
- User login
- JWT access token authentication
- Refresh token support
- Logout functionality
- Password hashing before persistence
- Stateless Spring Security configuration
- Protected API endpoints
- Authentication through the standard Bearer token header
- User-specific resource ownership checks

Protected requests use:

```http
Authorization: Bearer <access-token>
```

---

### Exercise Definitions

- Create exercise definitions
- Retrieve all available exercise definitions
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
- Add multiple sets to every exercise
- Retrieve a paginated workout history
- Retrieve a workout by ID
- Update workout metadata
- Replace an entire workout
- Delete workouts
- Restrict workout access to the authenticated owner

Each workout can contain multiple exercises, and each exercise can contain multiple sets.

---

### Exercise Management Inside a Workout

- Add an exercise to an existing workout
- Change the exercise definition while preserving its sets
- Delete an exercise from a workout
- Automatically renumber the remaining exercises after deletion

Exercises are ordered using an `exerciseNumber` value.

---

### Set Management

- Add a set to an existing exercise
- Partially update a set
- Delete a set
- Automatically renumber the remaining sets after deletion

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

- Calculate the total volume of a workout
- Calculate training volume for the last seven days
- Calculate training volume for the last month
- Retrieve the personal record for an exercise

Workout volume is calculated using:

```text
volume = weight × repetitions
```

Personal records are selected using the following priority:

1. Highest weight
2. Highest number of repetitions when multiple sets use the same maximum weight

---

### Pagination

Workout history supports pagination using `page` and `size` query parameters.

Example:

```http
GET /api/workouts?page=0&size=10
```

The response includes:

- Current page
- Page size
- Total number of elements
- Total number of pages
- Whether the current page is the last page
- Workout content

---

### API Documentation

The project includes:

- Swagger UI
- OpenAPI specification
- Documented controllers and DTOs
- JWT authentication support in Swagger
- Request and response documentation
- Validation information

---

### Testing

The project contains automated tests for both the service and controller layers.

Service tests use:

- JUnit 5
- Mockito
- Mocked repositories and dependencies

Controller tests use:

- Spring MVC test support
- MockMvc
- Mocked services
- JSON request and response assertions
- HTTP status assertions

The test suite covers successful operations, invalid input, missing resources and authentication-related scenarios.

---

### Docker and Continuous Integration

- Dockerized Spring Boot application
- MySQL Docker service
- Docker Compose configuration
- Environment-based configuration
- GitHub Actions workflow
- Automatic build and test execution
- Dedicated MySQL database during CI
- Maven `clean verify` execution on every push and pull request to `main`

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
- Access tokens
- Refresh tokens
- BCrypt password encoding

### Database

- MySQL 8
- Spring Data repositories
- JPA entity relationships

### Documentation

- Swagger UI
- OpenAPI

### Testing

- JUnit 5
- Mockito
- MockMvc
- Spring Boot Test
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

### Service Layer

Responsible for:

- Business logic
- Authentication logic
- Ownership validation
- Entity mapping
- Workout calculations
- Resource creation and updates

### Repository Layer

Responsible for:

- Database access
- Entity persistence
- User-specific queries
- Workout and exercise lookup

### DTO Layer

Request and response DTOs are used to prevent exposing persistence entities directly through the API.

---

## Domain Model

The main entities are:

### User

Represents an application account.

A user can own multiple workouts.

### ExerciseDefinition

Represents a reusable exercise, such as Bench Press or Squat.

### Workout

Represents a workout created by a specific user.

A workout contains:

- A name
- A date
- A list of workout exercises

### WorkoutExercise

Represents an exercise performed inside a workout.

It contains:

- An exercise definition
- An exercise number
- A list of sets

### ExerciseSet

Represents a performed set.

It contains:

- Set number
- Weight
- Repetitions
- RIR

### Refresh Token

Represents a refresh token associated with the authentication flow.

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
| POST | `/api/auth/login` | Authenticate a user |
| POST | `/api/auth/refresh` | Refresh authentication tokens |
| POST | `/api/auth/logout` | Logout and invalidate the current session or token |

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

## Authentication

### Authentication Flow

1. The user registers or logs in.
2. The API generates authentication tokens.
3. The client stores the returned tokens.
4. The access token is included in protected requests.
5. The refresh token can be used to obtain new authentication tokens.
6. Logout invalidates the current authentication session or token.

Protected requests use:

```http
Authorization: Bearer <access-token>
```

The authenticated username is extracted from the Spring Security context.

Workout ownership is verified using both:

```text
workout ID + authenticated username
```

This prevents users from reading, updating or deleting workouts belonging to other accounts.

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

Only the provided metadata fields are updated.

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
  "last": true
}
```

When the user has no workouts, the API returns `200 OK` with an empty content list.

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
  "workoutDate": "2026-07-15"
}
```

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
- Exercise definition IDs must not be null
- Exercise and set lists must contain valid elements
- Weight must be positive
- Repetitions must be positive
- RIR must be within the accepted range

Invalid requests return an appropriate HTTP status code.

### Error Status Codes

| Error Case | Status Code |
|---|---|
| Invalid request body | `400 Bad Request` |
| Invalid path parameter | `400 Bad Request` |
| Invalid refresh or logout token | `400 Bad Request` |
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

A global exception handler is used to provide consistent API error responses.

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

Datasource configuration:

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

To run the containers in the background:

```bash
docker compose up --build -d
```

### 4. Stop the containers

```bash
docker compose down
```

To also remove Docker volumes:

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

### 2. Configure the database

Create a MySQL database:

```sql
CREATE DATABASE fitness_tracker_db;
```

### 3. Configure environment variables

Example:

```env
DB_URL=jdbc:mysql://127.0.0.1:3306/fitness_tracker_db
DB_USERNAME=root
DB_PASSWORD=your_database_password
JWT_SECRET=your_very_long_jwt_secret
```

These values can be configured through:

- A local `.env` file
- IntelliJ run configuration
- Operating system environment variables
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

The complete test suite can be executed using Maven.

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

The project includes:

- Service-layer unit tests
- Controller-layer tests
- MockMvc request tests
- Validation tests
- Error response tests
- Authentication controller tests
- Workout controller tests
- Exercise definition controller tests
- Progress controller tests

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
3. Configures Java 26
4. Restores the Maven dependency cache
5. Makes the Maven Wrapper executable
6. Runs:

```bash
./mvnw --batch-mode clean verify
```

The CI environment uses a dedicated database:

```text
fitness_tracker_test_db
```

The build fails when compilation or any automated test fails.

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
5. Enter the token.
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
    └── java
        └── com.cosmin.fitness_tracker_api
            ├── ControllerTest
            └── ServiceTest
```

Additional project files:

```text
fitness-tracker-api/
├── .github/
│   └── workflows/
│       └── ci.yml
├── .env
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

- Passwords are encoded before being stored
- JWT secrets are loaded through environment variables
- Real credentials are excluded from source control
- Protected endpoints require authentication
- Resource ownership is verified using the authenticated username
- Users cannot access workouts belonging to other accounts
- Unauthorized resource access does not expose another user's data
- Authentication endpoints are public
- Swagger and OpenAPI endpoints can be configured as public
- The application uses stateless authentication for protected requests

The JWT secret used in production should be long, random and different from development or CI secrets.

---

## Future Improvements

Possible future improvements include:

- Deploy the API to a cloud platform
- Add integration tests with Testcontainers
- Add workout filtering by date and name
- Add sorting options for workout history
- Add exercise progress history
- Add estimated one-repetition maximum calculations
- Add charts-ready progress endpoints
- Add user profile management
- Add password reset functionality
- Add email verification
- Add role-based authorization
- Add API rate limiting
- Add structured application logging
- Add health and metrics endpoints with Spring Boot Actuator
- Add database migrations using Flyway or Liquibase

---

## What I Learned

While building this project, I practiced:

- Designing REST APIs with Spring Boot
- Structuring a backend using controller, service and repository layers
- Separating persistence entities from request and response DTOs
- Implementing JWT authentication
- Implementing access and refresh token flows
- Protecting endpoints with Spring Security
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
- Selecting personal records with tie-breaking rules
- Validating request bodies and path parameters
- Handling custom exceptions globally
- Writing unit tests with JUnit and Mockito
- Testing controllers with MockMvc
- Mocking Spring dependencies in web-layer tests
- Running tests using Maven
- Containerizing an application with Docker
- Connecting Spring Boot to MySQL through Docker Compose
- Managing configuration with environment variables
- Creating a GitHub Actions continuous integration pipeline
- Running a MySQL service inside CI
- Generating interactive API documentation with Swagger and OpenAPI
- Configuring JWT authentication inside Swagger UI
- Keeping credentials and secrets outside source control

---

## Status

The main API functionality is implemented and covered by automated service and controller tests.

The project currently includes:

- Authentication
- JWT access tokens
- Refresh token support
- Logout
- Workout management
- Exercise and set management
- Progress calculations
- Personal records
- Pagination
- Validation
- Global exception handling
- Swagger documentation
- Docker support
- GitHub Actions CI
- Service tests
- Controller tests

The project is actively maintained as a Java and Spring Boot backend portfolio project.

---

## Author

**Cosmin**

GitHub: [cosmiinn75](https://github.com/cosmiinn75)
