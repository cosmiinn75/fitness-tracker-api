# Fitness Tracker API

A Spring Boot REST API for tracking workouts, exercises, sets, and fitness progress.

The project includes JWT authentication, user-specific workout access, nested workout creation, validation, exception handling, pagination, and unit tests with JUnit and Mockito.

---

## Tech Stack

- Java
- Spring Boot
- Spring Security
- JWT Authentication
- Spring Data JPA
- Hibernate
- MySQL
- Maven
- JUnit 5
- Mockito
- Jakarta Bean Validation

---

## Features

### Authentication

- Register a new user
- Login with username and password
- JWT-based authentication
- Protected endpoints using `Authorization: Bearer <token>`
- Passwords are encoded before being saved

### Exercise Definitions

- Create exercise definitions
- Retrieve available exercises
- Prevent duplicate exercise names

Example exercise definitions:

- Bench Press
- Squat
- Deadlift
- Pull Up
- Shoulder Press

### Workouts

- Create workouts for the authenticated user
- Add multiple exercises to a workout
- Add multiple sets to each exercise
- Retrieve workouts for the current user
- Retrieve a specific workout by id
- Update workout metadata
- Replace a full workout
- Delete workouts
- Pagination support for workout history

### Security

- Users can only access their own workouts
- Workout data is filtered by the currently authenticated user
- JWT is validated on protected endpoints
- Public endpoints are limited to authentication routes

### Testing

The project includes service-layer unit tests for:

- Register success
- Register duplicate username/email
- Login success
- Login with invalid credentials
- Create workout successfully
- Create workout with invalid exercise definition
- Retrieve workouts for current user
- Delete workout

---

## Project Structure

```text
src/main/java/com/cosmin/fitness_tracker_api
│
├── Controller
│   ├── AuthController
│   ├── ExerciseDefinitionController
│   └── WorkoutController
│
├── DTO
│   ├── AuthRequest
│   ├── LoginRequest
│   ├── AuthResponse
│   ├── WorkoutRequest
│   ├── WorkoutResponse
│   ├── ExerciseRequest
│   ├── ExerciseResponse
│   ├── SetRequest
│   ├── SetResponse
│   └── PagedResponse
│
├── Exception
│   ├── GlobalExceptionHandler
│   ├── AccountAlreadyExistsException
│   ├── InvalidCredentialsException
│   ├── UserNotFoundException
│   ├── WorkoutNotFoundException
│   ├── ExerciseDefinitionNotFoundException
│   └── NameAlreadyExistsException
│
├── Model
│   ├── User
│   ├── ExerciseDefinition
│   ├── Workout
│   ├── WorkoutExercise
│   └── ExerciseSet
│
├── Repository
│   ├── UserRepository
│   ├── ExerciseDefinitionRepository
│   ├── WorkoutRepository
│   ├── WorkoutExerciseRepository
│   └── ExerciseSetRepository
│
├── Security
│   ├── SecurityConfig
│   ├── JwtFilter
│   └── JwtUtil
│
└── Service
    ├── AuthService
    ├── ExerciseDefinitionService
    └── WorkoutService
```

---

## Main API Endpoints

### Authentication

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/register` | Register a new user |
| POST | `/api/auth/login` | Login and receive JWT token |

### Exercise Definitions

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/exercises` | Get all exercise definitions |
| POST | `/api/exercises` | Create a new exercise definition |

### Workouts

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/workouts?page=0&size=10` | Get paginated workouts for current user |
| POST | `/api/workouts` | Create a workout |
| GET | `/api/workouts/{id}` | Get workout by id |
| PATCH | `/api/workouts/{id}` | Update workout metadata |
| PUT | `/api/workouts/{id}` | Replace full workout |
| DELETE | `/api/workouts/{id}` | Delete workout |

---

## Authentication Flow

1. User registers or logs in.
2. The API returns a JWT token.
3. The client sends the token on protected requests:

```http
Authorization: Bearer <token>
```

Protected endpoints use the current authenticated username to make sure users can only access their own workout data.

---

## Example Register Request

```http
POST /api/auth/register
Content-Type: application/json
```

```json
{
  "username": "cosmin",
  "email": "cosmin@gmail.com",
  "password": "password123"
}
```

### Example Response

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

---

## Example Login Request

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

### Example Response

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

---

## Example Create Exercise Definition Request

```http
POST /api/exercises
Authorization: Bearer <token>
Content-Type: application/json
```

```json
{
  "name": "Bench Press",
  "muscleGroup": "CHEST"
}
```

---

## Example Create Workout Request

```http
POST /api/workouts
Authorization: Bearer <token>
Content-Type: application/json
```

```json
{
  "workoutName": "Push Day",
  "date": "2025-02-10",
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

### Example Response

```json
{
  "id": 1,
  "workoutName": "Push Day",
  "date": "2025-02-10",
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

## Pagination

Workout history supports pagination.

Example:

```http
GET /api/workouts?page=0&size=10
Authorization: Bearer <token>
```

Example response:

```json
{
  "content": [
    {
      "id": 1,
      "workoutName": "Push Day",
      "date": "2025-02-10",
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

Pagination is used to avoid returning a large workout history in a single request.

---

## Validation

The API uses request DTOs and validation annotations to prevent invalid data.

Examples of validation rules:

- Workout name must not be blank
- Date must not be null
- Exercise definition id must not be null
- Weight must be positive
- Reps must be positive
- RIR must be valid

Invalid request bodies return `400 Bad Request`.

---

## Error Handling

The project uses a global exception handler to return consistent error responses.

Examples:

| Error Case | Status Code |
|---|---|
| Invalid credentials | `401 Unauthorized` |
| User not found | `404 Not Found` |
| Workout not found | `404 Not Found` |
| Exercise definition not found | `404 Not Found` |
| Duplicate username/email | `409 Conflict` |
| Duplicate exercise name | `409 Conflict` |
| Validation error | `400 Bad Request` |

---

## Environment Variables

Sensitive data should not be hardcoded in `application.properties`.

Use environment variables instead:

```properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
jwt.secret=${JWT_SECRET}
```

Example local values:

```env
DB_URL=jdbc:mysql://localhost:3306/fitness_tracker_db
DB_USERNAME=root
DB_PASSWORD=your_password
JWT_SECRET=your_very_long_secret_key
```

Do not commit real secrets to GitHub.

---

## How to Run Locally

### 1. Clone the repository

```bash
git clone https://github.com/cosmiinn75/fitness-tracker-api.git
cd fitness-tracker-api
```

### 2. Create a MySQL database

```sql
CREATE DATABASE fitness_tracker_db;
```

### 3. Configure environment variables

Set the following variables in your IDE or terminal:

```env
DB_URL=jdbc:mysql://localhost:3306/fitness_tracker_db
DB_USERNAME=root
DB_PASSWORD=your_password
JWT_SECRET=your_very_long_secret_key
```

### 4. Run the application

```bash
mvn spring-boot:run
```

The API will start on:

```text
http://localhost:8080
```

---

## How to Run Tests

```bash
mvn test
```

The project includes unit tests using JUnit and Mockito.

---

## What I Learned

While building this project, I practiced:

- Building REST APIs with Spring Boot
- Structuring a backend project using controller, service, repository, entity and DTO layers
- Implementing JWT authentication
- Protecting endpoints with Spring Security
- Using JPA/Hibernate relationships
- Creating nested resources such as workouts, exercises and sets
- Returning paginated API responses
- Handling custom exceptions globally
- Writing unit tests with JUnit and Mockito
- Keeping sensitive configuration out of source control

---

## Future Improvements

Planned improvements:

- Add progress statistics
  - Weekly volume
  - Monthly volume
  - Personal records per exercise
- Add endpoints for editing individual sets
- Add Docker Compose for MySQL
- Add GitHub Actions for CI
- Add Swagger/OpenAPI documentation
- Add integration tests
- Add refresh tokens
- Add more advanced filtering and sorting

---

## Status

This project is still in development and is mainly built as a backend portfolio project for practicing Java, Spring Boot, Spring Security, JPA, REST API design and testing.
