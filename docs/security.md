# Authentication and Security

[← Back to README](../README.md)

This document describes the authentication model, token lifecycle, authorization rules, ownership boundaries, Redis-backed rate limiting, validation behavior, secret management, and production-security expectations of the Fitness Tracker API.

## Table of Contents

- [Security Model](#security-model)
- [Endpoint Protection](#endpoint-protection)
- [Authentication Flow](#authentication-flow)
- [Access Tokens](#access-tokens)
- [Refresh Tokens](#refresh-tokens)
- [Logout](#logout)
- [Password Storage](#password-storage)
- [Password Reset](#password-reset)
- [Authenticated Password Change](#authenticated-password-change)
- [Stateless Spring Security](#stateless-spring-security)
- [Rate Limiting](#rate-limiting)
- [Current-User Abstraction](#current-user-abstraction)
- [Authorization and Resource Ownership](#authorization-and-resource-ownership)
- [Exercise Access Rules](#exercise-access-rules)
- [Validation and Error Responses](#validation-and-error-responses)
- [Configuration and Secrets](#configuration-and-secrets)
- [Security Testing](#security-testing)
- [Production Checklist](#production-checklist)
- [Planned Hardening](#planned-hardening)

## Security Model

The API uses stateless bearer-token authentication.

The main security controls are:

- BCrypt password hashing.
- Short-lived JWT access tokens.
- Persistent refresh tokens stored in MySQL.
- Refresh-token rotation and revocation.
- A custom JWT filter for access-token validation.
- Redis-backed Token Bucket rate limiting for selected public authentication routes and authenticated API requests.
- Stateless Spring Security configuration.
- Ownership-aware repository queries.
- A centralized `CurrentUserProvider`.
- Consistent errors through Spring `ProblemDetail`.
- Environment-based database and JWT secrets.

The application does not use an HTTP session to remember authenticated users. Every protected request must carry a valid access token.

## Endpoint Protection

Protected requests use:

~~~http
Authorization: Bearer <access-token>
~~~

The current public/protected boundary is:

| Endpoint group | Authentication | Purpose |
| --- | --- | --- |
| `/api/auth/**` | Public | Registration, login, refresh, logout, and password reset |
| `/swagger-ui/**` | Public | Interactive API documentation |
| `/v3/api-docs/**` | Public | OpenAPI document |
| `/actuator/health` | Public | Application health |
| Exercise endpoints | Required | Accessible system and owned custom exercises |
| Workout endpoints | Required | Owned workout history and nested data |
| Progress endpoints | Required | Analytics derived from the authenticated user's data |
| Training-goal endpoints | Required | Owned training goals |
| Workout-template endpoints | Required | Owned reusable templates |

Although the authentication endpoints are publicly reachable, their operations still validate credentials or tokens before returning protected data. Selected high-risk public authentication operations are additionally rate-limited by client IP before JWT processing.

## Authentication Flow

~~~mermaid
sequenceDiagram
    participant C as Client
    participant A as Auth API
    participant D as MySQL

    C->>A: Register or login
    A-->>C: Access token + refresh token
    C->>A: Protected request + access token
    A-->>C: Protected response
    C->>A: Refresh token
    A->>D: Validate and revoke current token
    A->>D: Store replacement token
    A-->>C: New access token + refresh token
~~~

The complete flow is:

1. A user registers or logs in.
2. The public authentication rate-limit filter evaluates configured authentication routes before credential processing.
3. The API validates the supplied data.
4. The password is verified against its BCrypt hash.
5. The API returns an access token and a refresh token.
6. The client sends the access token in the `Authorization` header.
7. The JWT filter validates the token and populates the Spring Security context.
8. The authenticated-user rate-limit filter applies the general API policy using the authenticated username.
9. When the access token expires, the client submits the refresh token to `POST /api/auth/refresh`.
10. The public authentication rate-limit filter applies the refresh policy before token rotation is attempted.
11. The API validates the refresh token.
12. The current refresh token is revoked.
13. A replacement access-token/refresh-token pair is generated.
14. The replacement refresh token is persisted.
15. The client discards the old token pair and uses the new one.

## Access Tokens

Access tokens are:

- JWTs signed with `HS256`.
- Short-lived.
- Valid for 15 minutes.
- Sent as bearer tokens.
- Validated by a custom JWT filter.
- Used to establish the authenticated principal for the current request.

Access tokens are not used as database records and do not create an HTTP session.

The JWT signing secret is provided through the `JWT_SECRET` environment variable. It must be long, random, different between environments, and never committed to source control.

## Refresh Tokens

Refresh tokens are:

- Persisted in MySQL.
- Valid for 7 days.
- Revocable.
- Rotated after successful use.
- Revoked during logout.

Rotation means that a successful refresh request consumes the current token and creates a replacement. A successfully rotated token cannot be used again.

The intended security property is:

~~~text
one successful refresh
→ old refresh token revoked
→ new token pair issued
→ old refresh token rejected on reuse
~~~

Refresh-token persistence allows the API to invalidate a token before its normal expiration, which is not possible with a completely self-contained token that is never checked against server-side state.

The current hardening roadmap includes storing only token hashes and adding stronger replay/reuse handling. Until that work is completed, database access must be restricted because stored refresh-token values are sensitive credentials.

## Logout

The logout endpoint receives a refresh token and revokes it.

After logout:

- The supplied refresh token cannot be used to obtain a new token pair.
- Existing access tokens remain valid until their short expiration unless an additional access-token denylist strategy is introduced.
- The client should delete both the access token and refresh token from its local state.

Logout is therefore based on revoking the long-lived credential and relying on the limited lifetime of the access token.

## Password Storage

Passwords are never stored in plaintext.

Spring Security's BCrypt support is used to:

- Hash passwords before persistence.
- Generate a unique salted hash.
- Verify login attempts without decrypting a stored password.

Password values must not be:

- Written to application logs.
- Included in exception messages.
- Returned in API responses.
- Stored in test fixtures used outside the test environment.

The database contains password hashes, not recoverable passwords.

## Password Reset

Forgotten-password recovery is exposed through:

| Method | Endpoint | Authentication | Result |
| --- | --- | --- | --- |
| `POST` | `/api/auth/forgot-password` | Public | Returns `202 Accepted` |
| `POST` | `/api/auth/reset-password` | Public | Returns `204 No Content` after a valid reset |

The reset flow is:

1. The client submits an email address.
2. The API accepts the request without revealing whether the account exists.
3. If a matching user exists, previous reset tokens for that user are deleted.
4. The API generates 32 random bytes with `SecureRandom`.
5. The raw URL-safe token is sent to the user by email.
6. Only the SHA-256 hash of the token is stored in MySQL.
7. The reset token expires after 15 minutes.
8. The client submits the raw token with the new password and confirmation.
9. The API hashes the submitted token and looks up the stored hash.
10. The new password must match its confirmation and differ from the old password.
11. The new password is stored as a BCrypt hash.
12. All refresh tokens belonging to the user are revoked.
13. All reset tokens for that user are deleted.
14. A confirmation email is sent.

This flow avoids storing usable password-reset credentials in the database and prevents the forgot-password endpoint from becoming an account-enumeration oracle.

## Authenticated Password Change

An authenticated user can change the current password through:

~~~http
PATCH /api/users/me/password
~~~

The operation requires:

- A valid access token.
- The correct current password.
- Matching new-password and confirmation values.
- A new password different from the current password.

The new password is stored as a BCrypt hash.

Two consistency improvements remain:

- Resolve the current username through `CurrentUserProvider` instead of direct `SecurityContextHolder` access in the account service.
- Revoke all active refresh tokens after a successful authenticated password change, matching the behavior already implemented for password reset.

## Stateless Spring Security

The application is configured without server-side HTTP sessions.

For each protected request:

1. The client presents the access token.
2. The public authentication rate-limit filter passes non-authentication API routes through without consuming a public-auth bucket.
3. The JWT filter validates the access token.
4. The filter creates the authenticated principal.
5. Spring Security stores that authentication only for the current request context.
6. The authenticated-user rate-limit filter applies the general API token bucket using the authenticated username.
7. Business services resolve the username through `CurrentUserProvider`.

CSRF protection is disabled because authentication uses stateless bearer tokens rather than browser-managed session cookies.

This decision assumes clients send the bearer token explicitly. If authentication is later moved into cookies, the CSRF strategy must be reviewed.

## Rate Limiting

The API uses a Redis-backed Token Bucket algorithm to control abusive request rates without introducing server-side sessions.

Rate limiting is split into two security filters because public authentication requests and authenticated API requests have different identities available at the time they are processed.

### Public Authentication Rate Limiting

`RateLimitFilter` runs before JWT authentication and applies endpoint-specific policies to selected public authentication operations.

The current rules are:

| Method | Endpoint | Bucket identity | Capacity | Refill |
| --- | --- | --- | ---: | --- |
| `POST` | `/api/auth/login` | Client IP | 5 | 1 token every 2 seconds |
| `POST` | `/api/auth/register` | Client IP | 3 | 1 token every 30 seconds |
| `POST` | `/api/auth/forgot-password` | Client IP | 3 | 1 token every 60 seconds |
| `POST` | `/api/auth/refresh` | Client IP | 10 | 1 token every second |

The public limiter uses the remote client address as part of the Redis key. When the application is deployed behind a reverse proxy, forwarded-client-address handling must be configured only for trusted proxies before those headers are used for security decisions.

### Authenticated API Rate Limiting

`AuthenticatedRateLimitFilter` runs after the JWT filter.

Once the JWT filter has established the principal, the authenticated limiter creates a bucket key from the authenticated username and applies the general API policy:

~~~text
capacity: 100 tokens
refill: 10 tokens per second
~~~

This creates one shared general-purpose request budget per authenticated user instead of maintaining one rule for every protected endpoint.

The authenticated limiter excludes:

- `/api/auth/**`
- `/swagger-ui/**`
- `/v3/api-docs/**`
- `/actuator/health`

These routes either have separate public-authentication handling or are intentionally excluded from the authenticated-user bucket.

### Filter Order

The security-filter order is:

~~~text
Public authentication rate-limit filter
→ JWT filter
→ Authenticated-user rate-limit filter
→ Controller
~~~

The order is significant:

- Public authentication limits must work before an authenticated principal exists.
- The JWT filter must run before user-based rate limiting so the username is available.
- Domain controllers and services are reached only after the applicable security filters allow the request.

### Redis Token-Bucket State

Both filters delegate to the same `RateLimitService` and use the same Redis Lua script.

The bucket stores:

- Current token balance.
- Last-refill timestamp.

Refill is calculated lazily from elapsed time whenever a request arrives. No scheduled refill task is required.

The Lua script atomically performs:

~~~text
read state
→ calculate proportional refill
→ cap tokens at capacity
→ consume one token when available
→ persist updated state
→ refresh bucket TTL
~~~

Atomic execution prevents concurrent requests from independently reading the same balance and overspending the bucket.

When a bucket has no available token, the request is rejected with:

~~~http
HTTP/1.1 429 Too Many Requests
~~~

The rate-limit state is operational Redis data rather than durable domain data. MySQL remains the source of truth for users, tokens, workouts, exercises, goals, and templates.

## Current-User Abstraction

Core domain services depend on:

~~~java
public interface CurrentUserProvider {
    String getCurrentUsername();
}
~~~

The production implementation reads the principal from Spring Security. Workout, exercise, progress, training-goal, and workout-template services do not call `SecurityContextHolder` directly.

The authenticated password-change service is a known remaining exception and is listed in the hardening roadmap.

This design:

- Centralizes principal resolution.
- Keeps security-context code out of the domain layer.
- Makes authentication dependencies visible in constructors.
- Allows unit tests to supply a username without starting Spring Security.
- Prevents different services from implementing slightly different current-user logic.

The provider resolves identity. It does not grant ownership by itself. Services and repositories still validate that the requested resource belongs to that identity.

## Authorization and Resource Ownership

Authentication answers:

~~~text
Who is making the request?
~~~

Ownership checks answer:

~~~text
May this authenticated user access this specific resource?
~~~

The API applies ownership at the query and service level.

| Resource | Ownership rule |
| --- | --- |
| Workouts | Loaded by workout ID and authenticated username |
| Workout exercises and sets | Accessed only through an owned workout |
| Progress analytics | Every query filters by authenticated username |
| Training goals | Loaded by goal ID and authenticated username |
| Workout templates | Loaded by template ID and authenticated username |
| Custom exercises | Visible and mutable only for their owner |
| System exercises | Readable by every authenticated user and immutable to users |

An ID belonging to another user is treated as not found rather than forbidden.

Example:

~~~text
User A requests User B's workout ID
→ ownership-aware query returns no result
→ API returns 404
~~~

This avoids confirming the existence of another user's private resource.

Ownership is also applied when one resource references another. For example:

- A workout can reference a system exercise or the current user's custom exercise.
- A training goal can target only an accessible exercise.
- A workout template can contain only accessible exercises.
- A custom exercise owned by a different user is invalid even if its numeric ID is known.

## Exercise Access Rules

The exercise catalog uses explicit type and ownership rules.

| Operation | System exercise | Own custom exercise | Another user's custom exercise |
| --- | --- | --- | --- |
| List | Allowed | Allowed when not archived | Not visible |
| Get by ID | Allowed | Allowed | Returned as not found |
| Use in workout | Allowed | Allowed when accessible | Rejected |
| Use in training goal | Allowed | Allowed when accessible | Rejected |
| Use in template | Allowed | Allowed when accessible | Rejected |
| Update | Rejected | Allowed | Returned as not found |
| Archive | Rejected | Allowed | Returned as not found |

Custom names are normalized before uniqueness checks:

- Surrounding whitespace is removed.
- Repeated spaces are collapsed.
- Comparison is case-insensitive.
- A duplicate custom name for the same user is rejected.
- A custom name that conflicts with a system exercise is rejected.

Archiving preserves historical references. It avoids deleting an exercise definition that may already be used by old workouts.

## Validation and Error Responses

Jakarta Bean Validation protects the API boundary before business logic executes.

Validated input includes:

- Usernames, email addresses, and passwords.
- Exercise names and muscle groups.
- Workout names and dates.
- Positive exercise-definition IDs.
- Weight, repetitions, and RIR.
- Positive path variables.
- Page indexes and page sizes.
- Start/end date ordering.
- Non-empty nested collections.
- Partial updates containing at least one field.
- Training-goal target values and dates.
- Exercise accessibility and ownership.
- Unique normalized template names.
- Duplicate exercises inside one template.
- Template target values.

The global exception handler converts known failures into Spring `ProblemDetail`.

The standard shape includes:

- `type`: stable problem category.
- `title`: short human-readable summary.
- `status`: HTTP status.
- `detail`: explanation of this failure.
- `instance`: request path.
- `code`: stable machine-readable application code.
- `fieldErrors`: validation details when applicable.

Example:

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

Typical status codes are:

| Status | Meaning |
| --- | --- |
| `400 Bad Request` | Invalid input, invalid pagination, or invalid date range |
| `401 Unauthorized` | Missing, expired, or invalid authentication |
| `404 Not Found` | Missing or inaccessible user-owned resource |
| `409 Conflict` | Duplicate resource or invalid lifecycle transition |
| `429 Too Many Requests` | An applicable public-IP or authenticated-user token bucket has no token available |
| `500 Internal Server Error` | Unexpected server failure |

Stable application codes allow clients to handle failures without parsing human-readable text.

Sensitive values must never appear in `detail`, field errors, stack traces returned to clients, or logs.

## Configuration and Secrets

The main security-relevant environment variables are:

| Variable | Purpose |
| --- | --- |
| `DB_URL` | JDBC connection target |
| `DB_USERNAME` | Database account used by the application |
| `DB_PASSWORD` | Database credential |
| `JWT_SECRET` | Secret used to sign and validate access tokens |
| `REDIS_HOST` | Redis host used for caching and rate-limit state |
| `REDIS_PORT` | Redis port used for caching and rate-limit state |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | Optional Hibernate schema-strategy override |

Do not commit:

- `.env`
- Database passwords.
- JWT secrets.
- Production credentials.
- Real user tokens.

The repository may contain `.env.example`, but it must contain placeholders rather than valid credentials.

Inside Docker Compose, the API connects to MySQL through the internal service name. In production, MySQL should not be exposed to the public internet.

Redis is used for exercise-definition caching and rate-limit bucket state. It should also remain on a private network and must not be exposed directly to untrusted clients.

## Security Testing

The automated suite covers security-relevant behavior such as:

- Authentication.
- Refresh-token rotation.
- Refresh-token revocation.
- Ownership checks.
- User isolation.
- Exercise accessibility.
- Validation failures.
- Standardized `ProblemDetail` responses.
- Inaccessible resource IDs returning not found.
- Training-goal ownership and lifecycle transitions.
- Workout-template ownership.
- Native progress queries filtering by authenticated username.

Password-reset and authenticated password-change contract/integration coverage remains incomplete and is a priority for the next test phase.

Service tests mock `CurrentUserProvider`, which makes the authorization context explicit without coupling unit tests to Spring Security internals.

Integration tests use a real MySQL 8 Testcontainer and exercise repository, persistence, and HTTP boundaries with the test profile.

See [testing.md](testing.md) for the complete testing strategy.

## Production Checklist

Before a public deployment:

- [ ] Serve the API only through HTTPS.
- [ ] Use a long random JWT secret generated for production.
- [ ] Keep secrets outside the repository.
- [ ] Use a dedicated database user instead of a root account.
- [ ] Grant the database user only required privileges.
- [ ] Keep MySQL on a private network.
- [ ] Use separate local, test, and production configuration.
- [ ] Restrict CORS to known frontend origins.
- [ ] Decide whether Swagger/OpenAPI should remain public.
- [ ] Expose only required Actuator endpoints.
- [ ] Avoid logging passwords, access tokens, refresh tokens, or reset tokens.
- [ ] Configure centralized secret management.
- [x] Apply Redis-backed rate limiting to login, registration, forgot-password, refresh, and authenticated API requests.
- [ ] Monitor failed authentication and token-refresh activity.
- [ ] Back up MySQL and test the restore process.
- [ ] Review error responses so internal exception details are not exposed.
- [ ] Run the full automated suite before deployment.

## Planned Hardening

The current security foundation is suitable for a portfolio backend, but the following items remain explicit hardening work:

### Refresh-Token Storage

Store only a cryptographic hash of each refresh token in MySQL. The raw token should be returned once to the client and should not be recoverable from a database read.

### Refresh-Token Reuse Detection

Track token replacement relationships or token families. Reusing an already-rotated token should be treated as a possible credential theft event and may revoke the entire active family.

### Password-Change Revocation

Revoke active refresh tokens after a password change or password reset so old long-lived credentials cannot continue creating new access tokens.

### External Token Configuration

Move access-token and refresh-token lifetimes, issuer, audience, and related values into typed external configuration.

### Rate-Limit Hardening

The current implementation applies Redis-backed Token Bucket limits to login, registration, forgot-password, refresh, and authenticated API requests.

Further hardening should:

- Extend the dedicated public-authentication limiter to other sensitive public operations such as password reset.
- Consider combining network-origin limits with a privacy-preserving account identifier where appropriate.
- Configure trusted forwarded-header handling before relying on client IP addresses behind a reverse proxy.
- Add metrics and alerts for repeated `429 Too Many Requests` responses without logging credentials or tokens.

### JWT Error Differentiation

Return the same `ProblemDetail` contract for missing, malformed, expired, and otherwise invalid access tokens while retaining stable machine-readable error codes.

### Concurrency Protection

Test concurrent refresh requests that use the same refresh token. Exactly one request should be allowed to rotate it successfully.

### Audit and Monitoring

Add security-safe audit events and metrics for:

- Failed logins.
- Refresh failures.
- Token reuse.
- Logout and account-level revocation.
- Password changes and resets.

Audit output must identify the event without recording secret token values or passwords.

### Production CORS and Secret Management

Replace permissive development settings with explicit production origins and managed secrets. Production configuration should be reviewed independently from local defaults.
