# Property Billing API

Property Billing API is a backend REST API for tracking monthly housing and apartment fees.

The system is designed for property owners or administrators who need to manage properties, units, tenants, monthly invoices, payments, expenses, and cash-flow reports from one backend service.

Current implementation status: the platform foundation is in place and the authentication module is complete for MVP admin/staff access.

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

Implemented:

- Admin and staff authentication
- JWT access-token login
- Refresh-token renewal
- Authenticated current-user lookup

Planned MVP modules:

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

## Implemented API

The public API contract is defined in `openapi.yml` and documented in `docs/API_SPEC.md`.

Implemented auth endpoints:

| Method | Path | Purpose |
|---|---|---|
| `POST` | `/api/v1/auth/login` | Login an admin or staff user and return access/refresh tokens. |
| `POST` | `/api/v1/auth/refresh` | Exchange a refresh token for a new access token. |
| `GET` | `/api/v1/auth/me` | Return the currently authenticated admin or staff user. |

Tenant login is intentionally not part of MVP.

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

Docker Compose starts a local PostgreSQL database using development-only defaults:

```text
Database: property_billing
Username: property_billing
Password: property_billing_password
Port: 5432
```

The PostgreSQL port is bound to `127.0.0.1` for local development only.

Docker Compose reads a local `.env` file automatically. To override the Docker
database settings, create a local `.env` file from `.env.example`. Do not commit
`.env`. Note that this project does not automatically load `.env` when starting
Spring Boot with Gradle, so if you change values such as the database port,
name, username, or password in `.env`, you must also export the same values in
your shell or pass them explicitly to the Gradle/Spring Boot run command.

Useful commands:

```bash
docker compose ps
docker compose logs -f postgres
docker compose down
```

To remove the local database volume and start with an empty database:

```bash
docker compose down -v
```

### Configure Environment

Create a local `.env` file from `.env.example` for Docker Compose overrides:

```bash
cp .env.example .env
```

The application expects these runtime variables outside the `local` profile:

| Variable | Purpose |
|---|---|
| `SPRING_DATASOURCE_URL` | JDBC connection string |
| `SPRING_DATASOURCE_USERNAME` | Database username |
| `SPRING_DATASOURCE_PASSWORD` | Database password |
| `JWT_SECRET` | HMAC secret used to sign JWTs |
| `INITIAL_ADMIN_ID` | Seeded admin UUID |
| `INITIAL_ADMIN_NAME` | Seeded admin name |
| `INITIAL_ADMIN_EMAIL` | Seeded admin login email |
| `INITIAL_ADMIN_PASSWORD_HASH` | Seeded admin BCrypt hash |
| `INITIAL_ADMIN_ROLE` | Seeded admin role |

For local development, `application-local.properties` supplies the PostgreSQL defaults used by Docker Compose. You still need to provide the initial admin values and `JWT_SECRET` when starting the application.

### Run Tests

```bash
./gradlew clean test
```

### Build Application

```bash
./gradlew clean build
```

CI runs both checks independently:

```text
test  -> ./gradlew clean test
build -> ./gradlew clean build
```

### Run Application

```bash
SPRING_PROFILES_ACTIVE=local \
JWT_SECRET=change_me \
INITIAL_ADMIN_ID=00000000-0000-0000-0000-000000000001 \
INITIAL_ADMIN_NAME="Admin User" \
INITIAL_ADMIN_EMAIL=admin@example.com \
INITIAL_ADMIN_PASSWORD_HASH='$2a$10$replace_with_real_bcrypt_hash' \
INITIAL_ADMIN_ROLE=admin \
./gradlew bootRun
```

The `local` Spring profile uses the same local PostgreSQL defaults as Docker
Compose. For non-local environments, provide `SPRING_DATASOURCE_URL`,
`SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD` explicitly.

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
{name}-{issueNumber}-{slugFromIssue}
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

The public repository protects `main` with required CI checks and code-owner
review. Pull requests must pass both the `test` and `build` jobs, resolve review
conversations, and receive approval from the repository owner before merge.

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
Foundation complete
Auth module complete
Next planned module: Property management
```

## License

This project is for portfolio and learning purposes.
