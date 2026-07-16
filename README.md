# Fitness Tracker API

A Spring Boot REST API for tracking workouts, exercises, sets, training volume, and personal records.

The application includes JWT authentication, user-specific workout access, nested workout management, granular exercise and set editing, progress statistics, pagination, validation, global exception handling, Swagger/OpenAPI documentation, and unit tests with JUnit and Mockito.

---

## Tech Stack

- Java
- Spring Boot
- Spring Web MVC
- Spring Security
- JWT Authentication
- Spring Data JPA
- Hibernate
- MySQL
- Maven
- Jakarta Bean Validation
- Swagger / OpenAPI
- JUnit 5
- Mockito

---

## Features

### Authentication

- Register a new user
- Login with username and password
- JWT-based authentication
- Password encoding before persistence
- Protected endpoints using:

```http
Authorization: Bearer <token>
```

### Exercise Definitions

- Create exercise definitions
- Retrieve all available exercise definitions
- Retrieve an exercise definition by ID
- Update an existing exercise definition
- Prevent duplicate exercise names

Example exercise definitions:

- Bench Press
- Squat
- Deadlift
- Pull Up
- Shoulder Press

### Workout Management

- Create workouts for the authenticated user
- Add multiple exercises to a workout
- Add multiple sets to each exercise
- Retrieve a paginated workout history
- Retrieve a specific workout by ID
- Update workout name or date
- Replace an entire workout
- Delete workouts
- Ensure users can access only their own workouts

### Granular Exercise and Set Management

- Update only selected fields of a set
- Change the exercise definition while keeping its sets
- Add a new set to an existing exercise
- Delete a set and renumber the remaining sets
- Add a new exercise with multiple sets
- Delete an exercise and renumber the remaining exercises

For partial set updates, only the fields included in the request are modified.

Example:

```json
{
  "reps": 8
}
```

The existing weight and RIR remain unchanged.

### Progress Tracking

- Calculate the total volume of a workout
- Calculate training volume for the last seven days
- Calculate training volume for the last month
- Retrieve the personal record for a specific exercise

Workout volume is calculated using:

```text
volume = weight × repetitions
```

Personal records are selected using the following rule:

1. Highest weight
2. Highest repetitions when multiple sets use the same maximum weight

### Pagination

Workout history supports pagination using page and size parameters.

Example:

```http
GET /api/workouts?page=0&size=10
```

### API Documentation

- Interactive Swagger UI
- OpenAPI specification
- JWT authentication support through the Swagger **Authorize** button
- Documented endpoints, responses, DTOs, and validation rules

### Security

- Users can access only their own workouts
- Workout ownership is checked using both workout ID and authenticated username
- Protected endpoints require a valid JWT
- Public access is limited to authentication and API documentation endpoints
- Accessing another user's workout returns `404 Not Found`

### Testing

The project includes service-layer unit tests for:

- Successful registration
- Duplicate username or email registration
- Successful login
- Login with invalid credentials
- Successful workout creation
- Invalid exercise definition during workout creation
- Retrieving workouts for the authenticated user
- Workout pagination
- Workout deletion
- Workout volume calculation
- Weekly and monthly volume calculation
- Personal record selection
- Personal record tie-breaking by repetitions
- Partial set updates
- Changing an exercise definition

---

## Project Structure

```text
src/main/java/com/cosmin/fitness_tracker_api
│
├── Controller
│   ├── AuthController
│   ├── ExerciseDefinitionController
│   ├── ProgressController
│   └── WorkoutController
│
├── DTO
│   ├── AuthRequest
│   ├── LoginRequest
│   ├── AuthResponse
│   ├── WorkoutRequest
│   ├── WorkoutMetaDataRequest
│   ├── WorkoutResponse
│   ├── ExerciseRequest
│   ├── ExerciseResponse
│   ├── SetRequest
│   ├── SetResponse
│   ├── UpdateExerciseSetRequest
│   ├── ChangeWorkoutExerciseRequest
│   ├── ExerciseDefinitionRequest
│   ├── ExerciseDefinitionResponse
│   ├── WorkoutVolumeResponse
│   ├── VolumeProgressResponse
│   ├── PersonalRecordResponse
│   └── PagedResponse
│
├── Exception
│   ├── GlobalExceptionHandler
│   ├── AccountAlreadyExistsException
│   ├── InvalidCredentialsException
│   ├── UserNotFoundException
│   ├── WorkoutNotFoundException
│   ├── WorkoutExerciseNotFoundException
│   ├── ExerciseSetNotFoundException
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
│   ├── JWTFilter
│   ├── JWTUtil
│   └── OpenAPIConfig
│
└── Service
    ├── AuthService
    ├── ExerciseDefinitionService
    ├── ProgressService
    └── WorkoutService
```

---

## API Endpoints

### Authentication

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/register` | Register a new user |
| POST | `/api/auth/login` | Login and receive a JWT |

### Exercise Definitions

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/exercises` | Get all exercise definitions |
| GET | `/api/exercises/{id}` | Get an exercise definition by ID |
| POST | `/api/exercises` | Create an exercise definition |
| PUT | `/api/exercises/{id}` | Update an exercise definition |

### Workouts

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/workouts?page=0&size=10` | Get paginated workouts for the current user |
| GET | `/api/workouts/{id}` | Get a workout by ID |
| POST | `/api/workouts` | Create a workout |
| PATCH | `/api/workouts/{id}` | Update workout name or date |
| PUT | `/api/workouts/{id}` | Replace an entire workout |
| DELETE | `/api/workouts/{id}` | Delete a workout |

### Exercise and Set Management

| Method | Endpoint | Description |
|---|---|---|
| PATCH | `/api/workouts/{workoutId}/exercises/{exerciseNumber}` | Change an exercise definition |
| POST | `/api/workouts/{workoutId}/exercises` | Add an exercise to a workout |
| DELETE | `/api/workouts/{workoutId}/exercises/{exerciseNumber}` | Delete and renumber an exercise |
| PATCH | `/api/workouts/{workoutId}/exercises/{exerciseNumber}/sets/{setNumber}` | Partially update a set |
| POST | `/api/workouts/{workoutId}/exercises/{exerciseNumber}/sets` | Add a set |
| DELETE | `/api/workouts/{workoutId}/exercises/{exerciseNumber}/sets/{setNumber}` | Delete and renumber a set |

### Progress

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/progress/workouts/{workoutId}/volume` | Calculate the volume of a workout |
| GET | `/api/progress/weekly-volume` | Calculate volume for the last seven days |
| GET | `/api/progress/monthly-volume` | Calculate volume for the last month |
| GET | `/api/progress/exercises/{exerciseDefinitionId}/personal-record` | Get the personal record for an exercise |

---

## Authentication Flow

1. The user registers or logs in.
2. The API returns a JWT.
3. The client sends the token with protected requests:

```http
Authorization: Bearer <token>
```

The API extracts the authenticated username from the Spring Security context.

Workout queries use both the workout ID and the username:

```text
workout ID + authenticated username
```

This prevents users from reading or modifying workouts belonging to other accounts.

---

## Swagger / OpenAPI

Interactive API documentation is available through Swagger UI.

After starting the application, open:

```text
http://localhost:8080/swagger-ui/index.html
```

The raw OpenAPI specification is available at:

```text
http://localhost:8080/v3/api-docs
```

### Using JWT in Swagger

1. Register or log in through the authentication endpoint.
2. Copy the returned JWT.
3. Click the **Authorize** button in Swagger UI.
4. Enter only the token, without manually adding the `Bearer` prefix.
5. Execute protected requests directly from Swagger.

Swagger automatically sends:

```http
Authorization: Bearer <token>
```

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

## Example Exercise Definition Request

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

### Example Response

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

## Example Partial Set Update

The following request updates only the repetitions:

```http
PATCH /api/workouts/1/exercises/1/sets/1
Authorization: Bearer <token>
Content-Type: application/json
```

```json
{
  "reps": 12
}
```

The weight and RIR remain unchanged.

Multiple fields can also be updated:

```json
{
  "weight": 75.0,
  "rir": 0
}
```

---

## Example Add Set Request

```http
POST /api/workouts/1/exercises/1/sets
Authorization: Bearer <token>
Content-Type: application/json
```

```json
{
  "weight": 75.0,
  "reps": 8,
  "rir": 1
}
```

The new set is added after the existing sets and receives the next available `setNumber`.

---

## Example Add Exercise Request

```http
POST /api/workouts/1/exercises
Authorization: Bearer <token>
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

The exercise is added after the existing exercises and receives the next available `exerciseNumber`.

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

When the authenticated user has no workouts, the API returns `200 OK` with an empty content list.

---

## Progress Examples

### Workout Volume

```http
GET /api/progress/workouts/1/volume
Authorization: Bearer <token>
```

Example response:

```json
{
  "totalVolume": 2270.0
}
```

### Weekly Volume

```http
GET /api/progress/weekly-volume
Authorization: Bearer <token>
```

Example response:

```json
{
  "startDate": "2026-07-10",
  "endDate": "2026-07-16",
  "totalVolume": 7450.0
}
```

### Monthly Volume

```http
GET /api/progress/monthly-volume
Authorization: Bearer <token>
```

Example response:

```json
{
  "startDate": "2026-06-16",
  "endDate": "2026-07-16",
  "totalVolume": 28450.0
}
```

### Personal Record

```http
GET /api/progress/exercises/1/personal-record
Authorization: Bearer <token>
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

## Validation

The API uses request DTOs and Jakarta Bean Validation.

Examples of validation rules:

- IDs and path numbers must be positive
- Workout names must not be blank
- Workout dates must not be null
- Exercise definition IDs must not be null
- Exercise and set lists must contain valid elements
- Weight must be positive
- Repetitions must be positive
- RIR must have a valid value

Invalid request bodies or path parameters return:

```text
400 Bad Request
```

---

## Error Handling

The project uses a global exception handler to return consistent error responses.

| Error Case | Status Code |
|---|---|
| Invalid request body | `400 Bad Request` |
| Invalid path parameter | `400 Bad Request` |
| Invalid credentials | `401 Unauthorized` |
| Missing or invalid JWT | `401 Unauthorized` |
| User not found | `404 Not Found` |
| Workout not found | `404 Not Found` |
| Workout exercise not found | `404 Not Found` |
| Exercise set not found | `404 Not Found` |
| Exercise definition not found | `404 Not Found` |
| Personal record not found | `404 Not Found` |
| Duplicate username or email | `409 Conflict` |
| Duplicate exercise name | `409 Conflict` |

---

## Environment Variables

Sensitive data is not hardcoded in `application.properties`.

```properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
jwt.secret=${JWT_SECRET}
```

Required environment variables:

```env
DB_URL=jdbc:mysql://localhost:3306/fitness_tracker_db
DB_USERNAME=root
DB_PASSWORD=your_password
JWT_SECRET=your_very_long_secret_key
```

Do not commit real credentials or JWT secrets to GitHub.

---

## How to Run Locally

### 1. Clone the repository

```bash
git clone https://github.com/cosmiinn75/fitness-tracker-api.git
cd fitness-tracker-api
```

### 2. Create the MySQL database

```sql
CREATE DATABASE fitness_tracker_db;
```

### 3. Configure environment variables

Configure the following variables in IntelliJ, your terminal, or your operating system:

```env
DB_URL=jdbc:mysql://localhost:3306/fitness_tracker_db
DB_USERNAME=root
DB_PASSWORD=your_password
JWT_SECRET=your_very_long_secret_key
```

In IntelliJ:

```text
Run
→ Edit Configurations
→ FitnessTrackerApiApplication
→ Environment variables
```

### 4. Run the application

```bash
mvn spring-boot:run
```

The API starts at:

```text
http://localhost:8080
```

Swagger UI is available at:

```text
http://localhost:8080/swagger-ui/index.html
```

---

## How to Run Tests

```bash
mvn test
```

The project includes service-layer unit tests using JUnit 5 and Mockito.

---

## What I Learned

While building this project, I practiced:

- Designing REST APIs with Spring Boot
- Structuring a backend using controller, service, repository, DTO, and entity layers
- Implementing JWT authentication
- Protecting endpoints with Spring Security
- Restricting resources to their authenticated owners
- Working with JPA and Hibernate relationships
- Managing nested resources such as workouts, exercises, and sets
- Implementing partial updates with PATCH
- Maintaining both sides of entity relationships
- Renumbering ordered nested resources after deletion
- Implementing pagination
- Calculating workout progress statistics
- Selecting personal records using comparators
- Handling custom exceptions globally
- Validating request bodies and path parameters
- Writing unit tests with JUnit and Mockito
- Generating interactive API documentation with Swagger/OpenAPI
- Configuring JWT authentication inside Swagger UI
- Keeping sensitive configuration outside source control

---

## Future Improvements

Planned improvements:

- Add Docker and Docker Compose for the API and MySQL
- Add GitHub Actions for continuous integration
- Add integration tests using a dedicated test database
- Deploy the API online
- Add refresh tokens
- Add advanced workout filtering and sorting
- Add exercise progress history
- Add estimated one-repetition maximum calculations
- Add user profile management
- Add password reset functionality

---

## Status

This project is under active development and was created as a backend portfolio project for practicing Java, Spring Boot, Spring Security, JWT, JPA, Hibernate, REST API design, testing, and API documentation.
