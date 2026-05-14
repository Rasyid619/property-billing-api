# AGENTS.md

## Read First

Before making any code changes, read these files:

1. `PROJECT_GUIDE.md` - product requirements and business rules
2. `CODEX.java.md` - Java code style and engineering conventions
3. `docs/DEVELOPMENT_PLAN.md` - module-by-module development workflow
4. `docs/DATABASE_DESIGN.md` - database schema and relationships
5. `docs/API_SPEC.md` - API contract and response format
6. `docs/OPENAPI_GUIDE.md` - OpenAPI contract-first rules
7. `docs/TESTING_STRATEGY.md` - testing requirements
8. `docs/DAILY_SCHEDULE.md` - daily working routine

## Project Summary

This repository is a Spring Boot backend API for apartment and housing monthly fee tracking.

The system helps property owners or administrators manage:

- Properties
- Units
- Tenants
- Tenant assignments
- Monthly invoice generation
- Payment recording
- Property expenses
- Cash-flow reporting
- Monthly cash balance tracking

## Required Stack

- Java 21
- Spring Boot
- Gradle Groovy DSL
- Gradle Wrapper
- PostgreSQL
- Flyway Migration
- Spring Data JPA
- Spring Security
- JWT Authentication
- Bean Validation
- Docker Compose
- Swagger / OpenAPI
- JUnit
- Mockito
- Testcontainers optional
- GitHub Actions

## Development Rules

- Follow `CODEX.java.md`.
- Follow `PROJECT_GUIDE.md`.
- Follow `docs/DEVELOPMENT_PLAN.md`.
- Use Gradle Wrapper.
- Do not use Maven.
- Do not rewrite unrelated files.
- Keep changes small and focused.
- Explain what files were changed.
- Run tests when possible.
- Do not implement future modules early.
- Do not expose JPA entities directly from controllers.
- Use DTOs for request and response.
- Use `BigDecimal` for money.
- Use `UUID` for IDs.
- Keep controllers thin.
- Put business logic in service layer.

## Issue and Branch Workflow

Every code or documentation task must start from a GitHub Issue.

Do not start implementation without an issue.

Each branch must be created from an issue using this naming convention:

```text
rasyid-{issueNumber}-{slugFromIssue}
```

Examples:

```text
rasyid-1-add-project-documentation
rasyid-2-create-spring-boot-base-project
rasyid-3-add-initial-database-migration
rasyid-4-add-openapi-foundation
rasyid-5-implement-property-module
```

Branch naming rules:

- Use lowercase letters.
- Use hyphen-separated words.
- Start with `rasyid`.
- Include the GitHub issue number.
- Use a short slug from the issue title.
- Do not use spaces.
- Do not use underscores.
- Do not use special characters.
- Keep the slug clear and short.

Each pull request must link the issue.

Use one of these in the pull request description:

```text
Closes #1
Fixes #1
Resolves #1
```

Do not merge a pull request unless:

- The branch name follows the convention.
- The pull request links to an issue.
- The module checklist is complete.
- Tests pass locally.
- GitHub Actions CI passes.

## Module Completion Rule

Every module must be completed with tests before moving to the next module.

Codex must not start a new module until:

- The current module implementation is finished
- Relevant database migration is added if needed
- OpenAPI contract is defined or updated
- Relevant unit tests are added
- Relevant integration tests are added when needed
- `./gradlew clean test` passes, or the reason it cannot be run is clearly explained
- GitHub Actions CI passes
- Changed files are summarized

When implementing a task, only work on the current module.

## API Contract Rule

After database migration and before implementing each module, define or update the OpenAPI contract.

For every new endpoint, document:

- Path
- HTTP method
- Request body
- Response body
- Query parameters
- Path parameters
- Validation rules
- Success status code
- Error status codes
- Example request
- Example response

Do not implement a controller endpoint before its API contract is defined.

When implementation changes the public API, update the OpenAPI contract and `docs/API_SPEC.md`.

## Tenant Login Rule

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

Unit and Tenant must remain separate because:

- A unit can exist without a tenant.
- A tenant can move out.
- Another tenant can move into the same unit.
- Assignment history must be preserved.

## Development Flow

Use this flow for every module:

1. Create GitHub Issue
2. Create branch using issue number
3. Review requirement
4. Review database design
5. Add or update migration if needed
6. Define or update OpenAPI contract
7. Implement entity/model
8. Implement repository
9. Implement DTOs
10. Implement service
11. Implement controller
12. Add validation
13. Add tests
14. Run tests
15. Update documentation if needed
16. Push branch
17. Open pull request
18. Link pull request to issue
19. Wait for CI to pass
20. Merge pull request
21. Move to next module

## Required Test Command

Before moving to the next module, run:

```bash
./gradlew clean test
```

For larger changes, also run:

```bash
./gradlew clean build
```

## Task Order

Implement features in this order:

1. Project documentation and Codex instructions
2. Base Spring Boot Gradle setup
3. Docker Compose PostgreSQL
4. Flyway migrations
5. OpenAPI contract foundation
6. GitHub Actions CI
7. Auth module
8. Property module
9. Unit module
10. Tenant module
11. Tenant assignment module
12. Invoice module
13. Payment module
14. Property expense module
15. Cash-flow report module
16. Cash balance closing module
17. API documentation improvement
18. README improvement
19. Deployment guide

## Important Restrictions

Do not:

- Use Maven
- Skip tests
- Implement multiple modules at once
- Add unrelated refactors
- Work directly on `main`
- Hardcode secrets
- Commit `.env` files
- Commit database passwords
- Commit JWT secrets
- Expose internal stack traces in API responses
