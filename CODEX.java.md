# Java Project Conventions

## Code Style

### Flat control flow

If logic can stay flat, do not make it nested.

- Prefer guard clauses and early returns over `else` blocks
- Keep the happy path visually straight
- If possible, every method should end with the happy path
- Extract a small helper before adding another indentation level
- Avoid unnecessary nesting in controllers, services, repositories, and tests

```java
// correct
public void updateUser(UpdateUserRequest payload) {
    if (payload.id() == null) {
        throw new ValidationException();
    }

    if (payload.name() == null || payload.name().isBlank()) {
        throw new ValidationException();
    }

    userRepository.save(payload);
}

// wrong
public void updateUser(UpdateUserRequest payload) {
    if (payload.id() != null) {
        if (payload.name() != null && !payload.name().isBlank()) {
            userRepository.save(payload);
        } else {
            throw new ValidationException();
        }
    } else {
        throw new ValidationException();
    }
}
```

### Formatting and imports

- Use the formatter configured by the project
- Do not manually align declarations or parameters
- Keep imports organized by the project's formatter or IDE rules
- Avoid wildcard imports unless the project already allows them
- Remove unused imports before committing

### Naming

- Methods should start with a verb when they perform work, for example `fetchRewards` or `computeTotal`
- Boolean names must start with `is`, `has`, `should`, `can`, or another clear predicate
- Classes, records, and enums should be nouns
- Plural names are reserved for collections and arrays
- Constants must use `UPPER_SNAKE_CASE`

### Method shape

- One method should do one job
- Prefer small methods with explicit inputs and outputs
- Keep controllers thin and orchestration-focused
- Keep repositories limited to data access
- Extract helpers only when they reduce duplication or improve clarity
- Prefer constructor injection over field injection

### Layer responsibilities

- Controllers should parse requests, call services, map status codes, and shape responses
- Complex business logic should live in services
- Repositories should contain no business logic
- Repositories should only read, write, lock, and map database data
- Do not move logic into repositories just to make services look shorter

### Data shaping

- Prefer explicit object construction over passing broad objects through layers
- Keep request DTOs, response DTOs, query projections, and domain models clearly separated
- Do not hide important shape changes inside generic helpers
- Prefer records for immutable DTOs when the project supports them

### Diffs

- Prefer the smallest correct diff
- Do not refactor unrelated code in the same change unless required
- Preserve existing local patterns unless the task requires changing them

### Multi-line declarations

For multi-line declarations, place each element on its own line. Opening and closing brackets should be easy to scan.

```java
List<String> requiredEnvVarNames = List.of(
    "APP_ENV",
    "DATABASE_URL",
    "JWT_SECRET"
);

CreateUserRequest request = new CreateUserRequest(
    payload.id(),
    payload.name(),
    payload.email()
);
```

## Documentation

- Public classes, records, interfaces, enums, and public methods should have Javadoc when they are part of the module API
- Javadoc should explain behavior and intent, not repeat the method name
- Include `@param`, `@return`, and `@throws` when they add useful information
- Do not add file-level Javadocs unless the project already uses them

```java
/**
 * Reward data with current stock information.
 *
 * @param id unique identifier for the reward
 * @param name display name of the reward
 * @param stockCount number of items remaining in stock
 */
public record RewardWithStock(
    UUID id,
    String name,
    int stockCount
) {}
```

## Types

- Use records for immutable data carriers when appropriate
- Use classes for behavior-rich domain objects and services
- Use interfaces for boundaries, abstractions, and multiple implementations
- Avoid broad casts; keep conversions narrow and local
- Prefer explicit return types over inference where Java requires clarity

### Type categories

Use packages that reflect ownership:

```text
com.example.project
  controller     API boundary
  service        business workflows
  repository     data access
  domain         domain models
  dto            API request and response payloads
  projection     database query result shapes
```

## Testing

Each test should cover one behavior or code path. If assertions cover the same code path, keep them in the same test.

- Test behavior, not implementation noise
- Use Mockito for mocking in Java tests when the project does not already standardize on another mocking library
- Mock only boundaries outside the unit under test
- Use integration tests for repository and API happy paths
- Do not write tests for rethrowing unexpected errors outside our control
- If a regression-prone branch changes, add a targeted test for that branch

```java
@Nested
class GetUser {
    @Test
    void returnsUserWhenUserExists() {
        // assertions for the happy path
    }

    @Test
    void throwsNotFoundWhenUserDoesNotExist() {
        // assertions for the missing user path
    }
}
```

## Commits

Use Conventional Commits for all commit messages. Prefix with one of:

```text
feat:
fix:
build:
refactor:
test:
docs:
chore:
ci:
style:
perf:
```

- Commit messages should describe the actual change
- Keep unrelated changes out of the same commit when possible

## Workflow

- Read the surrounding implementation before editing
- Reuse existing repo patterns before introducing new abstractions
- When changing API behavior, check routes, validation, DTOs, OpenAPI, and tests
- Run the narrowest useful verification first, usually a targeted test class or package test
- State clearly when verification could not be run
- Never include temporary local files such as `.codex` in commits

## SQL And Repositories

- Format multi-clause SQL vertically
- Prefer explicit column lists over `SELECT *`
- Alias selected columns when it makes result shapes clearer
- Keep ownership and existence checks explicit
- Use row locks only when the workflow requires them

## API And Validation

- Reuse existing request and response schemas where appropriate
- Keep validation close to the request boundary
- Return the narrowest response shape needed by the endpoint
- Use `400`, `403`, `404`, and `409` intentionally based on behavior

## Environment Variables

Required environment variable lists must be sorted alphabetically.

## OpenAPI

In `openapi.yml`, paths, schemas, and tags must all be sorted alphabetically.

All `summary` and `description` fields must be proper sentences.

## Property Billing API Project Rules

This project is a Spring Boot backend API for property billing, apartment monthly fee tracking, invoice generation, payment recording, property expense tracking, and cash-flow reporting.

### Stack Rules

- Use Java 21.
- Use Spring Boot.
- Use Gradle Groovy DSL.
- Use Gradle Wrapper.
- Do not use Maven.
- Use PostgreSQL.
- Use Flyway for database migrations.
- Use Docker Compose for local dependencies.
- Use Springdoc OpenAPI for API documentation.
- Use JUnit and Mockito for tests.
- Use Testcontainers for integration tests when database behavior needs to be verified.
- Use GitHub Actions for CI.

### Architecture Rules

Use this package structure unless the project already has a better established structure:

```text
com.propertybilling
  config
  controller
  domain
  dto
  exception
  projection
  repository
  security
  service
```

Controllers must stay thin.

Services contain business logic.

Repositories contain data access only.

DTOs must be separated from entities.

Do not expose JPA entities directly from controllers.

### Data Type Rules

- Use `UUID` for entity IDs.
- Use `BigDecimal` for money.
- Use `LocalDate` for business dates such as billing month, due date, payment date, and expense date.
- Use `Instant` or `OffsetDateTime` for created and updated timestamps.
- Use enums for fixed statuses such as invoice status.
- Store billing month as the first day of the month.

Example:

```text
2026-05-01
```

### Money Rules

Money must never use:

```text
double
float
```

Use:

```java
BigDecimal
```

### Unit and Tenant Separation Rules

Keep Unit and Tenant separated.

Use this model:

```text
Property = building or housing area
Unit = room, house, apartment number, or rented space
Tenant = person who lives in or rents the unit
UnitTenant = assignment history between unit and tenant
```

Do not merge Unit and Tenant into one table.

Reasons:

- A unit can exist without a tenant.
- A tenant can move out.
- Another tenant can move into the same unit.
- Assignment history must be preserved.
- Invoices should be linked to both the unit and the tenant at the time of billing.

### Tenant Login Rules

Tenant login is not part of MVP.

For MVP:

- Only admin and staff users can login.
- Tenants are managed as data records.
- Tenants do not have application accounts yet.

Future tenant login options:

1. Email or phone login linked to tenant profile.
2. Invitation-based tenant account creation.
3. Property code, unit number, and secure access code.

Do not use property name and unit name alone as login credentials because they are easy to guess.

### Invoice Rules

Invoice status values:

```text
unpaid
partial
paid
overdue
cancelled
```

Invoice status must be calculated from:

- Invoice amount
- Total paid amount
- Due date
- Current date

Rules:

```text
total_paid = 0 -> unpaid
total_paid > 0 and total_paid < amount -> partial
total_paid >= amount -> paid
due_date < today and total_paid < amount -> overdue
```

A paid invoice must not become overdue.

### API Contract Rules

Before implementing a controller endpoint:

- Update `docs/API_SPEC.md`
- Update OpenAPI annotations or `openapi.yml` when used
- Define request body
- Define response body
- Define validation rules
- Define error responses
- Add or update tests

### GitHub Issue and Branch Rules

Every task must be based on a GitHub Issue.

Branch naming convention:

```text
rasyid-{issueNumber}-{slugFromIssue}
```

Examples:

```text
rasyid-1-add-project-documentation
rasyid-2-create-spring-boot-base-project
rasyid-3-add-initial-database-migration
```

Rules:

- One issue per focused task.
- One branch per issue.
- One pull request per branch.
- Pull request must link the issue using `Closes #issueNumber`.
- Do not mix unrelated issues in one branch.
- Do not work directly on `main`.

### Module Completion Rules

Do not move to the next module until the current module has:

- GitHub Issue
- Branch from issue
- Migration if needed
- API contract
- Implementation
- Unit tests
- Integration tests when needed
- Documentation update if public behavior changes
- Passing local tests
- Passing GitHub Actions CI

Required command:

```bash
./gradlew clean test
```

For larger changes:

```bash
./gradlew clean build
```

### GitHub Actions Rule

CI must run tests for:

- Push to main
- Pull request to main

Do not treat a module as complete when CI is failing.

### Documentation Rules

Update documentation when public behavior changes.

Relevant docs:

- `PROJECT_GUIDE.md`
- `docs/API_SPEC.md`
- `docs/DATABASE_DESIGN.md`
- `docs/DEVELOPMENT_PLAN.md`
- `docs/OPENAPI_GUIDE.md`
- `docs/TESTING_STRATEGY.md`
