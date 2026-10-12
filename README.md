# Fitness Tracker API

[![Fitness Tracker CI](https://github.com/cosmiinn75/fitness-tracker-api/actions/workflows/ci.yml/badge.svg?branch=main&event=push)](https://github.com/cosmiinn75/fitness-tracker-api/actions/workflows/ci.yml)

A REST API for tracking strength workouts, exercises, training goals, and progress. Built with **Java 25, Spring Boot 4.1, MySQL 8, and Redis 8**.

This is my main public Java backend project. I've used it to work through authentication, JPA relationships, SQL queries, caching, transactions, and testing in a single application.

## Demo

- **[Watch the Flutter app demo on YouTube](https://youtube.com/shorts/U6vBpthKjUQ?feature=share)**
- [Flutter mobile client — source code](https://github.com/cosmiinn75/fitness-tracker-app)

The video shows the mobile client using this API. The client is still a work in progress, so not every backend feature is shown in the demo.

## Features

- **Workouts:** Create, edit, duplicate, and delete workouts with ordered exercises and sets. Browse history with pagination and filters.
- **Exercises:** Shared system exercises and user-owned custom exercises, with archive support to keep workout history intact.
- **Templates:** Save a routine and generate an editable workout draft without persisting a new workout.
- **Progress:** Workout volume, exercise history, estimated 1RM, and paginated personal records. The personal-record query uses MySQL's `ROW_NUMBER()` window function.
- **Training goals:** Track target weight and repetitions, complete goals when a workout meets them, and expire overdue goals with a scheduled job.
- **Accounts:** JWT authentication, refresh-token rotation and revocation, password reset, and ownership checks for user data.

## Implementation notes

| Area | How it works |
| --- | --- |
| API structure | Layered Spring Boot application with controllers, services, repositories, DTOs, and dedicated mappers |
| Security | Spring Security, short-lived access tokens, persisted refresh tokens, and resource-level ownership checks |
| Rate limiting | Redis Token Bucket with an atomic Lua script; separate policies for public auth endpoints and authenticated requests |
| Caching | Redis-backed exercise reads with user-scoped keys, a 30-minute TTL, and targeted cache eviction on writes |
| Persistence | Spring Data JPA/Hibernate, Flyway migrations, and native SQL/projections for personal records |
| Email | Password emails are handled asynchronously through Spring events after the database transaction commits |
| Errors | Consistent error responses using Spring `ProblemDetail` and application-specific codes |
| Testing | JUnit, Mockito, MockMvc, Testcontainers for MySQL and Redis, and GitHub Actions CI |

A couple of design decisions I wanted to get right:

- **Keep business logic out of controllers.** Services handle validation, ownership, and transaction boundaries; mappers build response DTOs.
- **Keep reads isolated by user.** Queries and Redis cache keys take the authenticated user into account.
- **Avoid sending emails for rolled-back changes.** Password services publish events handled with `@TransactionalEventListener(AFTER_COMMIT)` and `@Async`. This isn't a durable message queue; an outbox/retry mechanism would be needed for stronger delivery guarantees.

More detail is available in the [architecture](docs/architecture.md), [security](docs/security.md), and [testing](docs/testing.md) docs.

## Tech stack

`Java 25` · `Spring Boot 4.1` · `Spring Security` · `Spring Data JPA / Hibernate` · `MySQL 8` · `Redis 8` · `Flyway` · `JUnit 5` · `Mockito` · `Testcontainers` · `Docker Compose` · `GitHub Actions`

Also used: Spring Cache, Spring Mail, Spring Boot Actuator, Swagger/OpenAPI, and Mailpit for local email testing.

## Run locally

**Requirements:** Docker and Docker Compose.

```bash
git clone https://github.com/cosmiinn75/fitness-tracker-api.git
cd fitness-tracker-api
cp .env.example .env
```

Set the database password and a strong JWT secret in `.env`, then run:

```bash
docker compose up --build
```

The stack starts the API, MySQL, Redis, and Mailpit. Flyway applies the database migrations on startup.

| Service | Local URL |
| --- | --- |
| Swagger UI | http://localhost:8080/swagger-ui/index.html |
| Health check | http://localhost:8080/actuator/health |
| Mailpit | http://localhost:8025 |

Register or log in through Swagger, then use the returned access token to authorize protected requests.

Stop the containers without deleting the database volume:

```bash
docker compose down
```

## Tests and CI

With Docker running (for Testcontainers), execute:

```bash
./mvnw clean verify
```

The tests cover service rules, controller responses, authentication and ownership, database queries, Redis caching and rate limiting, and email events across transaction commit/rollback.

GitHub Actions runs the build and test suite for pushes and pull requests to `main`. The badge at the top links to the workflow.

## Next steps

I'm focusing on measuring and improving JPA queries (including N+1 issues), hashing stored refresh tokens, and adding more concurrency tests. I also want to make asynchronous email delivery retry-safe across application restarts.

---

Built by [Cosmin Anghel](https://github.com/cosmiinn75).
