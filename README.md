# Property Billing API

Property Billing API is a backend REST API for tracking monthly housing and apartment fees.

The system helps property owners or administrators manage properties, units, tenants, monthly invoices, payments, expenses, and cash-flow reports.

## Project Goal

This project is built as a backend engineering portfolio project.

It demonstrates:

- REST API design
- Relational database design
- PostgreSQL usage
- Flyway database migration
- Spring Boot layered architecture
- OpenAPI documentation
- Business logic implementation
- Unit and integration testing
- Docker-based local development
- GitHub Issues workflow
- Pull request workflow
- CI using GitHub Actions

## Tech Stack

- Java 21
- Spring Boot
- Gradle Groovy DSL
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

## Main Features

- Admin and staff authentication
- Property management
- Unit management
- Tenant management
- Tenant assignment to unit
- Monthly invoice generation
- Payment recording
- Invoice status tracking
- Property expense tracking
- Monthly cash-flow report
- Monthly cash balance support

## Main Business Flow

1. Admin creates a property.
2. Admin creates units inside the property.
3. Admin creates tenant data.
4. Admin assigns tenant to a unit.
5. Admin generates monthly invoices.
6. Admin records tenant payments.
7. System updates invoice status.
8. Admin records property expenses.
9. System calculates monthly cash flow.

## Design Decision

Unit and Tenant are separated.

```text
Property = building or housing area
Unit = room, house, apartment number, or rented space
Tenant = person who lives in or rents the unit
UnitTenant = assignment history between unit and tenant
```

Tenant login is not part of MVP.

For MVP, tenants are managed as data records by admin or staff.

## Project Structure

```text
property-billing-api/
├── src/
├── docs/
│   ├── API_SPEC.md
│   ├── DATABASE_DESIGN.md
│   ├── DEVELOPMENT_PLAN.md
│   ├── OPENAPI_GUIDE.md
│   ├── TESTING_STRATEGY.md
│   └── DAILY_SCHEDULE.md
├── AGENTS.md
├── CODEX.java.md
├── PROJECT_GUIDE.md
├── README.md
├── build.gradle
├── settings.gradle
├── docker-compose.yml
└── .github/
    └── workflows/
        └── ci.yml
```

## Local Development

### Requirements

- Java 21
- Docker Desktop
- WSL Ubuntu recommended for Windows users
- Git
- IntelliJ IDEA
- PostgreSQL client such as DBeaver
- Postman or Insomnia

### Run PostgreSQL

```bash
docker compose up -d
```

### Run Tests

```bash
./gradlew clean test
```

### Build Application

```bash
./gradlew clean build
```

### Run Application

```bash
./gradlew bootRun
```

## API Documentation

Swagger UI will be available after the application is running.

Expected local URL:

```text
http://localhost:8080/swagger-ui/index.html
```

OpenAPI JSON:

```text
http://localhost:8080/v3/api-docs
```

## Development Workflow

This project follows module-by-module development.

Each module must be completed with tests before moving to the next module.

Development flow:

```text
GitHub Issue
→ Branch
→ Database design
→ Migration
→ OpenAPI contract
→ Implementation
→ Tests
→ Pull Request
→ CI pass
→ Merge
→ Next module
```

## Branch Convention

Every branch must be based on a GitHub Issue.

Branch format:

```text
rasyid-{issueNumber}-{slugFromIssue}
```

Examples:

```text
rasyid-1-add-project-documentation
rasyid-2-create-spring-boot-base-project
rasyid-3-add-initial-database-migration
```

## Pull Request Rule

Each pull request must link its issue.

Example:

```text
Closes #1
```

## Documentation

Important documents:

- `AGENTS.md`
- `CODEX.java.md`
- `PROJECT_GUIDE.md`
- `docs/DEVELOPMENT_PLAN.md`
- `docs/DATABASE_DESIGN.md`
- `docs/API_SPEC.md`
- `docs/OPENAPI_GUIDE.md`
- `docs/TESTING_STRATEGY.md`
- `docs/DAILY_SCHEDULE.md`

## Status

Current status:

```text
Planning and setup phase
```

## License

This project is for portfolio and learning purposes.
