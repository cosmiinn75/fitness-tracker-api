# Testing and Continuous Integration

[← Back to README](../README.md)

This document describes the automated testing strategy, test organization, MySQL and Redis Testcontainers setup, mapper-testing approach, Redis cache integration tests, query-count regression tests, and GitHub Actions pipeline of the Fitness Tracker API.

At the documented repository state, the project contains 126 test methods across 21 executable test classes, plus shared test base and Testcontainers configuration classes.

## Table of Contents

- [Testing Goals](#testing-goals)
- [Running the Suite](#running-the-suite)
- [Test Structure](#test-structure)
- [Test Levels](#test-levels)
- [Service Testing Strategy](#service-testing-strategy)
- [Controller Testing](#controller-testing)
- [Integration Testing with MySQL](#integration-testing-with-mysql)
- [Redis Cache Integration Tests](#redis-cache-integration-tests)
- [Authentication Tests](#authentication-tests)
- [Exercise-Definition Tests](#exercise-definition-tests)
- [Workout Tests](#workout-tests)
- [Progress and Personal-Record Tests](#progress-and-personal-record-tests)
- [Training-Goal Tests](#training-goal-tests)
- [Workout-Template Tests](#workout-template-tests)
- [ProblemDetail and Validation Tests](#problemdetail-and-validation-tests)
- [JPA Query-Count Regression Tests](#jpa-query-count-regression-tests)
- [Persistence-Context Discipline](#persistence-context-discipline)
- [Continuous Integration](#continuous-integration)
- [Adding a New Feature](#adding-a-new-feature)
- [Remaining Testing Priorities](#remaining-testing-priorities)

## Testing Goals

The suite is designed to verify more than successful CRUD responses.

The main testing goals are:

- Validate business rules in isolation.
- Verify controller routing, request validation, serialization, and status codes.
- Exercise real JPA mappings against MySQL 8.
- Protect user ownership and cross-user isolation.
- Verify refresh-token rotation and revocation.
- Confirm deterministic pagination and ordering.
- Test nested workout and template persistence.
- Validate native SQL ranking logic.
- Detect JPA N+1 regressions through query counts.
- Verify Redis-backed exercise-definition caching and cache invalidation against a real Redis instance.
- Verify mapper output without duplicating mapping stubs in every service test.
- Keep local and CI verification reproducible.

The complete Maven verification lifecycle is the source of truth before a release.

## Running the Suite

Run all tests and Maven verification steps:

~~~bash
./mvnw clean verify
~~~

On Windows PowerShell:

~~~powershell
.\mvnw.cmd clean verify
~~~

Run one test class:

~~~powershell
.\mvnw.cmd "-Dtest=WorkoutServiceTest" test
~~~

Run one test method:

~~~powershell
.\mvnw.cmd "-Dtest=WorkoutServiceTest#duplicateWorkout_ShouldCopyWorkout" test
~~~

Integration tests use Docker through Testcontainers. Docker must be running before those tests start.

## Test Structure

~~~text
src/test/
├── java/com/cosmin/fitness_tracker_api/
│   ├── ControllerTest/
│   │   ├── AuthControllerTest.java
│   │   ├── ExerciseDefinitionControllerTest.java
│   │   ├── ProgressControllerTest.java
│   │   ├── TrainingGoalControllerTest.java
│   │   └── WorkoutControllerTest.java
│   ├── IntegrationTest/
│   │   ├── AbstractIntegrationTest.java
│   │   ├── AuthIntegrationTest.java
│   │   ├── EmailEventListenerIntegrationTest.java
│   │   ├── ExerciseDefinitionCacheIntegrationTest.java
│   │   ├── ExerciseDefinitionIntegrationTest.java
│   │   ├── PersonalRecordRepositoryIntegrationTest.java
│   │   ├── TestcontainersConfiguration.java
│   │   ├── TrainingGoalQueryCountIntegrationTest.java
│   │   ├── WorkoutIntegrationTest.java
│   │   ├── WorkoutQueryCountIntegrationTest.java
│   │   └── WorkoutTemplateQueryCountIntegrationTest.java
│   ├── ServiceTest/
│   │   ├── AuthServiceTest.java
│   │   ├── ExerciseDefinitionServiceTest.java
│   │   ├── ProgressServiceTest.java
│   │   ├── TrainingGoalServiceTest.java
│   │   ├── WorkoutServiceTest.java
│   │   └── WorkoutTemplateServiceTest.java
│   └── FitnessTrackerApiApplicationTests.java
└── resources/
    └── application-test.properties
~~~

## Test Levels

| Level | Main tools | Purpose |
| --- | --- | --- |
| Service unit tests | JUnit 5, Mockito | Business rules, ownership decisions, repository coordination, mapper output |
| Controller tests | MockMvc, mocked services | Routing, JSON contract, validation, status codes |
| Repository integration tests | Spring Boot Test, JPA, MySQL Testcontainer | Native SQL, projections, pagination, persistence behavior |
| Redis cache integration tests | Spring Boot Test, Spring Cache, Redis Testcontainer, Awaitility | Real cache population, cache hits, and invalidation behavior |
| End-to-end backend integration tests | Spring Boot Test, MockMvc, MySQL Testcontainer | HTTP-to-database behavior, authentication, ownership, serialization |
| Query-count regression tests | Hibernate Statistics, MySQL Testcontainer | N+1 prevention and constant query behavior |
| Context smoke test | Spring Boot Test | Application-context startup |

These levels are complementary. A service test can explain a failed business rule quickly, while an integration test verifies that configuration, mappings, SQL, transactions, and serialization work together.

## Service Testing Strategy

Service tests isolate repositories, authentication access, and external services with Mockito.

Simple mapper implementations are used as spies:

~~~java
@Mock
private WorkoutRepository workoutRepository;

@Mock
private CurrentUserProvider currentUserProvider;

@Spy
private WorkoutMapper workoutMapper = new WorkoutMapper();

@InjectMocks
private WorkoutService workoutService;
~~~

This arrangement keeps:

- Database access mocked.
- The authenticated username explicit.
- Business services isolated.
- Mapper transformations real.
- Constructor injection intact.
- Spring context startup unnecessary.

`ExerciseDefinitionServiceTest` follows the same service-boundary philosophy while mocking `ExerciseDefinitionCacheService`. It verifies that exercise-definition reads delegate through the cache layer and that create/archive operations trigger the expected cache invalidation without requiring Redis in the unit-test layer.

Current exercise-definition service scenarios include:

- Listing exercise definitions through the cache service.
- Retrieving one exercise definition through the cache service.
- Archiving a custom exercise and evicting both list and item cache entries.
- Creating a custom exercise and evicting the cached list.
- Rejecting duplicate custom exercise names without persistence or cache eviction.
- Rejecting names that conflict with system exercises without persistence or cache eviction.

Redis behavior itself is verified separately by the cache integration tests.

Using a real mapper through `@Spy` avoids two common testing problems:

1. Repeating mapper stubs in every service test.
2. Returning an incomplete DTO that does not match production mapping behavior.

Mapper spies should not be used to hide repository calls. If mapping triggers lazy loading, the integration and query-count tests must expose that behavior.

Service tests should generally follow this pattern:

1. Arrange the authenticated user and repository data.
2. Invoke one public service method.
3. Assert the returned DTO or thrown domain exception.
4. Verify the important repository interactions.
5. Verify that forbidden persistence calls did not occur.

## Controller Testing

Controller tests use MockMvc with service dependencies mocked.

They verify:

- HTTP method and route.
- Path variables and query parameters.
- JSON request deserialization.
- Jakarta Bean Validation.
- Expected service call.
- HTTP status.
- Response JSON shape.

Current controller coverage includes:

- Registration, login, refresh, and logout.
- Accessible exercise listing, retrieval, creation, and replacement.
- Workout listing, retrieval, creation, update, deletion, duplication, and nested set/exercise operations.
- Weekly/monthly/workout volume.
- Personal records, exercise history, and progress summary.
- Training-goal creation, listing, and cancellation.

Controller tests do not replace service tests. A controller test proves that the HTTP layer delegates correctly; it should not reimplement every service business scenario with mocked behavior.

## Integration Testing with MySQL

Integration tests extend a common base:

~~~java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import(TestcontainersConfiguration.class)
public abstract class AbstractIntegrationTest {

    @MockitoBean
    protected JavaMailSender javaMailSender;
}
~~~

The shared Testcontainers configuration creates a real MySQL 8 container:

~~~java
@Bean
@ServiceConnection
MySQLContainer mySQLContainer() {
    return new MySQLContainer("mysql:8.0")
            .withDatabaseName("fitness_tracker_test")
            .withUsername("test")
            .withPassword("test");
}
~~~

Using MySQL rather than an H2 compatibility database is important because the application relies on:

- MySQL 8 behavior.
- Native SQL.
- Window functions.
- Flyway migrations.
- Real foreign keys and indexes.
- Hibernate mappings against the production database engine.

Integration tests verify:

- Entity persistence.
- Nested JPA relationships.
- Repository queries.
- Controller-to-database behavior.
- Serialized nested responses.
- Authentication and ownership boundaries.
- Standardized error responses.

The mail sender is mocked so integration tests do not contact an SMTP service.

## Redis Cache Integration Tests

`ExerciseDefinitionCacheIntegrationTest` verifies Spring Cache behavior against a real Redis 8 Testcontainer instead of mocking the cache infrastructure.

The test context uses:

- A real `RedisCacheManager`.
- A Redis 8 Alpine Testcontainer connected through Spring Boot service connections.
- A MySQL 8 Testcontainer required by the full Spring Boot application context.
- A mocked `ExerciseDefinitionRepository` so repository invocation counts can clearly distinguish cache hits from misses.
- Awaitility to wait for cache state to become observable without relying on immediate timing assumptions.

The cache integration suite currently contains two tests.

### Cache Hit Behavior

`findAll_ShouldUseRedisCache` verifies that:

1. The configured cache manager is a `RedisCacheManager`.
2. `ExerciseDefinitionCacheService` is running through a Spring AOP proxy.
3. The first `findAll(username)` call loads data through the repository.
4. The result becomes available in the `exerciseDefinitions` Redis cache.
5. A second call for the same username returns the cached value.
6. The repository is invoked only once across both reads.

The test waits for the cache entry before issuing the second read:

~~~java
await()
        .atMost(Duration.ofSeconds(2))
        .until(() -> cache.get(username) != null);
~~~

This protects the observable cache contract without assuming that the Redis entry must be visible at the exact instant the intercepted service method returns.

### Cache Eviction Behavior

`evictList_ShouldRemoveCachedValue` verifies that:

1. The first read populates the Redis cache.
2. A second read uses the cached value.
3. `evictList(username)` removes the user's cached exercise-definition list.
4. The next read reaches the repository again.
5. The reloaded value is cached again.

Cache cleanup between tests uses `Cache.invalidate()` for the exercise-definition cache regions before Mockito interactions are reset.

The tested behavior is therefore:

~~~text
repository read
→ Redis cache population
→ cache hit
→ eviction
→ repository read again
→ cache repopulation
~~~

This test complements `ExerciseDefinitionServiceTest`: the service unit test verifies when the application chooses to read from or invalidate the cache layer, while the Redis integration test verifies that Spring Cache, Redis connectivity, serialization, lookup, and eviction work together.

## Authentication Tests

Authentication tests cover both service/controller behavior and database-backed flows.

Current scenarios include:

- Successful registration.
- Duplicate username rejection.
- Duplicate email rejection.
- Successful login.
- Invalid credential rejection.
- Successful refresh.
- Invalid refresh-token rejection.
- Successful logout.
- Refresh-token revocation after logout.
- New token pair returned after refresh.

Important assertions include:

- The user is persisted during registration.
- Password authentication uses the configured password encoder.
- A refresh request replaces the usable token state.
- Logout prevents the supplied refresh token from being used again.
- Controller responses contain the expected token fields and status.

Concurrency and token-reuse-detection scenarios are separate remaining priorities.

## Exercise-Definition Tests

Exercise-definition service, controller, database integration, and Redis cache integration tests provide broad coverage across business rules, ownership, HTTP behavior, and caching.

They verify:

- System exercises and the current user's custom exercises are returned together.
- Another user's custom exercises are not returned.
- Archived exercises are excluded.
- A system exercise can be retrieved.
- Another user's custom exercise returns not found.
- An archived custom exercise returns not found.
- A created custom exercise belongs to the authenticated user.
- Names are cleaned and normalized.
- Duplicate owned custom names are rejected.
- Names that conflict with system exercises are rejected.
- The owner can update a custom exercise.
- System exercises cannot be updated.
- Another user's custom exercise cannot be updated.
- A normalized duplicate produced during update is rejected.
- The owner can archive a custom exercise.
- System exercises cannot be archived.
- Another user's custom exercise cannot be archived.

The dedicated `ExerciseDefinitionServiceTest` additionally verifies:

- Read methods delegate to `ExerciseDefinitionCacheService`.
- Archiving marks the owned custom exercise as archived and evicts the list and single-item cache entries.
- Creating a custom exercise persists the normalized `CUSTOM` definition with the authenticated owner and evicts the cached list.
- Duplicate custom or system exercise names are rejected before save and do not trigger cache eviction.

These tests are important because the exercise catalog combines global data and private data in the same endpoint.

## Workout Tests

Workout service, controller, and integration tests cover:

- Creating a workout with exercises and sets.
- Rejecting an inaccessible or missing exercise definition.
- Retrieving the current user's workouts.
- Retrieving a persisted nested workout.
- Updating workout metadata.
- Replacing a workout.
- Deleting a workout.
- Adding and changing workout exercises.
- Adding, updating, and deleting sets.
- Renumbering ordered children.
- Duplicating a complete workout.
- Preserving the source workout during duplication.
- Returning not found for another user's workout.
- Returning unauthorized when authentication is missing.

Integration assertions should verify both:

- The HTTP response.
- The resulting database state.

Nested responses should be loaded from the persisted state rather than relying only on the in-memory object graph created during test setup.

## Progress and Personal-Record Tests

Progress service and repository tests cover volume, history, summary, and ranking behavior.

### Volume

Tests verify:

- Volume for one owned workout.
- Weekly volume.
- Monthly volume.
- Missing-workout behavior.

The expected formula is:

~~~text
volume = sum(weight × repetitions)
~~~

### Personal Records

Personal-record tests verify:

1. Highest weight wins.
2. Repetitions break a weight tie.
3. RIR participates in further tie-breaking.
4. More recent dates break later ties.
5. Other users' sets are excluded.
6. Page metadata is correct.
7. Multiple exercises are split correctly across pages.
8. A user without recorded sets receives an empty page.

The repository integration test is particularly important because selection uses native MySQL SQL with `ROW_NUMBER()` and `PARTITION BY`.

### Exercise History

Tests verify:

- Paginated history mapping.
- Optional date filtering.
- Invalid date-range rejection.
- Missing or inaccessible exercise behavior.
- Estimated one-repetition maximum values.

### Summary

Summary tests verify mapping of:

- Total workout count.
- Recent distinct training days.
- Recent set count.
- Latest workout date.
- Most-trained exercise.

## Training-Goal Tests

Training-goal service, controller, and query-count tests cover:

- Successful creation.
- Initial `ACTIVE` status.
- Missing exercise rejection.
- One active goal per user and exercise.
- Paginated goal retrieval.
- Manual cancellation.
- Missing-goal behavior.
- Completion when one set reaches both targets.
- No completion when target values are split across different sets.
- No completion when the workout predates the goal.
- User isolation.
- Constant query behavior while listing goals with exercise definitions.

Goal completion tests must construct the workout sets carefully. The rule is satisfied by one qualifying set, not by combining the weight from one set with the repetitions from another.

The current suite should still be extended with a vertical database-backed lifecycle test that covers creation, completion through workout creation, cancellation, expiration, and concurrent active-goal creation.

## Workout-Template Tests

Workout-template service tests currently verify:

- Nested template creation.
- Ordered exercises and sets.
- Normalized duplicate-name rejection.
- Duplicate-exercise rejection.
- Missing or inaccessible exercise rejection.
- Ownership protection for retrieval.
- Ownership protection for deletion.
- Template-to-workout-draft conversion.
- No workout persistence during draft generation.

Query-count integration tests verify detailed template retrieval and paginated template listing against MySQL.

The template suite should still be completed with:

- `WorkoutTemplateControllerTest`.
- A full HTTP-to-database integration test for create, get, list, draft, and delete.
- Cascade-deletion assertions for template exercises and sets.
- An assertion that deleting a template does not delete referenced exercise definitions.

## ProblemDetail and Validation Tests

The API uses Spring `ProblemDetail` for validation and domain errors.

Tests should assert the contract, not only the numeric status:

~~~json
{
  "type": "urn:problem:workout-not-found",
  "title": "Workout not found",
  "status": 404,
  "detail": "Workout not found",
  "instance": "/api/workouts/10",
  "code": "WORKOUT_NOT_FOUND"
}
~~~

For Bean Validation failures, tests should also assert `fieldErrors`.

Important categories are:

- Invalid request body.
- Invalid path variable.
- Invalid pagination.
- Invalid date range.
- Missing authentication.
- Missing or inaccessible resource.
- Duplicate normalized name.
- Invalid lifecycle transition.
- Unexpected server failure.

A dedicated contract test should keep `type`, `title`, `status`, `code`, `instance`, and `fieldErrors` consistent across handlers.

## JPA Query-Count Regression Tests

Dedicated integration tests use Hibernate `Statistics`:

~~~java
Statistics statistics = sessionFactory.getStatistics();
statistics.setStatisticsEnabled(true);
statistics.clear();

// execute the read use case

long executedStatements = statistics.getPrepareStatementCount();
~~~

The current expected counts are:

| Test | Expected prepared statements |
| --- | ---: |
| Get one detailed workout | 2 |
| Get one filtered workout page | 4 |
| Get one detailed workout template | 2 |
| Get one workout-template page | 4 |
| Get one training-goal page | 2 |

The paginated workout and template tests protect a two-step loading strategy:

1. Select a page of root IDs.
2. Execute the page count query.
3. Load detailed roots with exercises and exercise definitions.
4. Batch-load nested sets.

The detailed single-resource tests load the root/exercise graph and then batch-load sets.

These tests must:

- Prepare all fixture data before clearing statistics.
- Clear the persistence context so cached entities do not hide queries.
- Clear Hibernate statistics immediately before the measured operation.
- Execute the same public service path used by the application.
- Assert both the response contents and the statement count.
- Add more nested rows than one trivial example.

The purpose is not to chase the smallest possible number. The purpose is to prevent the number of statements from growing with the number of exercises, sets, templates, or goals.

## Persistence-Context Discipline

JPA's first-level cache can make an integration test pass even when production loading is incorrect.

Where a test needs to simulate a real later request:

1. Persist the fixture.
2. Flush changes to MySQL.
3. Clear the persistence context.
4. Execute the repository, service, or MockMvc request.
5. Assert data loaded from the database.

This is especially important for:

- Bidirectional relationships.
- Nested response mapping.
- Cascade deletion.
- Lazy loading.
- Entity-graph verification.
- Query-count measurement.

Transactional test rollback keeps the container database isolated between test methods, while flush/clear ensures the operation under test does not rely on stale managed entities.

## Continuous Integration

The GitHub Actions workflow runs for pushes and pull requests targeting `main`.

The pipeline:

1. Checks out the repository.
2. Configures Java 25.
3. Restores the Maven dependency cache.
4. Makes Docker available to Testcontainers.
5. Starts the Testcontainers required by the executed integration tests, including MySQL 8 and Redis 8 where applicable.
6. Applies Flyway migrations to the MySQL test database.
7. Compiles the project.
8. Runs `./mvnw clean verify`.
9. Fails the workflow when any verification step fails.

The README badge links to the current workflow status.

CI protects:

- Compilation.
- Migration compatibility.
- Unit and controller behavior.
- MySQL-backed integration behavior.
- Redis-backed cache integration behavior.
- Native SQL.
- Ownership boundaries.
- Query-count expectations.

Before tagging a release, the same `clean verify` command should pass locally and on the final `main` commit.

## Adding a New Feature

A new use case should usually add tests in this order:

1. Service tests for happy path and business-rule failures.
2. Controller tests for route, validation, status, and JSON contract.
3. Integration tests for persistence, ownership, serialization, and infrastructure behavior such as caching when applicable.
4. Repository tests for custom JPQL/native SQL.
5. Query-count tests when mapping traverses nested relationships or the endpoint is paginated.
6. Concurrency tests when correctness depends on uniqueness, rotation, or state transitions.

For every user-owned feature, include:

- Owner success.
- Another user denied as not found.
- Missing authentication.
- Missing resource.
- Invalid input.
- Duplicate/conflict behavior where applicable.

For every nested aggregate, include:

- Correct child ordering.
- Add/update/delete persistence.
- Cascade behavior.
- Protection of referenced shared entities.

## Remaining Testing Priorities

The highest-value remaining additions are:

1. **Workout-template controller tests** for create, validation, get, list, draft, delete, authentication, ownership, and the full `ProblemDetail` shape.
2. **Workout-template vertical integration test** for nested persistence, draft read-only behavior, cascade deletion, and preservation of exercise definitions.
3. **Training-goal vertical integration test** for create, complete through workout creation, cancel, expire, and ownership.
4. **Password and account-security tests** for reset-token expiration, one-time use, password change, old-password rejection, and refresh-token revocation.
5. **Concurrency tests** for duplicate active goals and simultaneous refresh-token rotation.
6. **ProblemDetail contract tests** for all major handler categories.
7. **JaCoCo reporting** with realistic thresholds for critical packages rather than a project-wide 100% target.
8. **Architecture tests** to enforce layer boundaries, such as preventing controllers from using repositories and mappers from performing persistence.
9. **Additional query-count tests** for progress summary and weekly/monthly analytics after those endpoints are moved toward database-side aggregation.
10. **Rate-limit integration tests** for public-IP and authenticated-user buckets, `429 Too Many Requests`, Redis-backed bucket state, and security-filter ordering.

The project already uses Testcontainers and query-count regression tests. Those items should be marked as completed in the README roadmap rather than listed as future work.
