# Development Plan

## Development Rule

Each module must be completed and tested before moving to the next module.

A module is considered complete only when:

- GitHub Issue exists
- Branch is created from the issue
- Database migration is added if needed
- OpenAPI contract is defined or updated
- Request and response format are documented
- Error response format is documented
- Entity/model is implemented
- Repository is implemented if needed
- DTO/request/response classes are implemented
- Service/business logic is implemented
- Controller/API endpoint is implemented
- Validation is added
- Unit tests are added for business logic
- Integration/API tests are added when needed
- Existing tests pass
- Pull request links the issue
- GitHub Actions CI passes
- README or API documentation is updated if public behavior changes

Do not start the next module until the current module passes:

```bash
./gradlew clean test
```

For larger changes, also run:

```bash
./gradlew clean build
```

## Issue-Based Development Rule

Every module and task must be tracked using GitHub Issues.

Before starting work:

1. Create a GitHub Issue.
2. Write the task scope.
3. Add acceptance criteria.
4. Create a branch from the issue.
5. Implement only the issue scope.
6. Open a pull request.
7. Link the pull request to the issue.
8. Merge only after tests and CI pass.

Branch format:

```text
rasyid-{issueNumber}-{slugFromIssue}
```

Example:

```text
rasyid-5-implement-property-module
```

Pull request description must include:

```text
Closes #{issueNumber}
```

Example:

```text
Closes #5
```

## Development Flow Per Module

Use this workflow for every module:

```text
GitHub Issue
→ Branch
→ Requirement
→ Database design
→ Migration
→ OpenAPI contract
→ Implementation
→ Unit tests
→ Integration tests if needed
→ Documentation update
→ Pull Request
→ CI pass
→ Merge
→ Next module
```

## Phase 0 - Planning and Repository Setup

### Module 1: Project Documentation and Codex Instructions

Tasks:

- Create `AGENTS.md`
- Add or update `CODEX.java.md`
- Create `PROJECT_GUIDE.md`
- Create `README.md`
- Create `docs/DEVELOPMENT_PLAN.md`
- Create `docs/DATABASE_DESIGN.md`
- Create `docs/API_SPEC.md`
- Create `docs/OPENAPI_GUIDE.md`
- Create `docs/TESTING_STRATEGY.md`
- Create `docs/DAILY_SCHEDULE.md`

Completion checklist:

- Documentation files exist
- GitHub Issue exists
- Branch follows naming convention
- Pull request links the issue
- Documentation explains project goal
- Documentation explains workflow
- Documentation explains testing rule
- Documentation explains OpenAPI rule
- Documentation explains issue and branch rule
- Documentation explains Unit and Tenant separation
- Documentation explains Tenant login is not part of MVP

## Phase 1 - Project Setup

### Module 2: Base Spring Boot Setup

Tasks:

- Create Spring Boot project
- Use Java 21
- Use Gradle Groovy DSL
- Add Gradle Wrapper
- Add Spring Web
- Add Spring Data JPA
- Add PostgreSQL Driver
- Add Flyway
- Add Spring Security
- Add Validation
- Add Swagger/OpenAPI
- Add basic health check endpoint if needed

Completion checklist:

- Application can start successfully
- Basic test exists
- `./gradlew clean test` passes
- GitHub Actions CI passes

### Module 3: Docker Compose PostgreSQL Setup

Tasks:

- Add `docker-compose.yml`
- Add PostgreSQL service
- Add database name
- Add local username and password for development only
- Add environment variable documentation
- Ensure `.env` is ignored if used

Completion checklist:

- PostgreSQL can run using Docker Compose
- Application can connect to PostgreSQL
- Documentation explains how to start database
- `./gradlew clean test` passes
- GitHub Actions CI passes

## Phase 2 - Database Foundation

### Module 4: Initial Database Migration

Create Flyway migration for:

- users
- properties
- units
- tenants
- unit_tenants
- invoices
- payments
- property_expenses
- cash_balances

Rules:

- Use UUID primary keys
- Use `TEXT` for money columns
- Store money as decimal strings, for example `750000.00`
- Parse money strings to `BigDecimal` in Java before validation or calculation
- Use `DATE` for billing month and due date
- Store billing month as the first day of the month
- Add unique constraint for `units(property_id, unit_number)`
- Add unique constraint for `invoices(unit_id, billing_month)`
- Add unique constraint for `cash_balances(property_id, month)`

Completion checklist:

- Migration runs successfully on clean database
- Database constraints are correct
- Migration test or integration startup test passes
- `docs/DATABASE_DESIGN.md` is updated
- `./gradlew clean test` passes
- GitHub Actions CI passes

## Phase 3 - OpenAPI Foundation

### Module 5: OpenAPI Contract Foundation

Tasks:

- Add Springdoc OpenAPI dependency if not already added
- Configure Swagger/OpenAPI
- Define global API response format
- Define global error response format
- Define JWT authentication scheme
- Create initial OpenAPI tags for:
  - Auth
  - Properties
  - Units
  - Tenants
  - Tenant Assignments
  - Invoices
  - Payments
  - Property Expenses
  - Reports
  - Cash Balances
- Create or update `docs/API_SPEC.md`
- Add example request and response format

Completion checklist:

- Swagger UI is accessible
- OpenAPI JSON is generated
- `docs/API_SPEC.md` explains API design rules
- API response format is consistent
- Error response format is consistent
- `./gradlew clean test` passes
- GitHub Actions CI passes

## Phase 4 - CI

### Module 6: GitHub Actions CI

Tasks:

- Create `.github/workflows/ci.yml`
- Run CI on push to main
- Run CI on pull request to main
- Set up Java 21
- Enable Gradle cache
- Run `./gradlew clean test`

Completion checklist:

- CI runs on pull request
- CI runs on push to main
- CI fails when tests fail
- CI passes when tests pass
- README documents CI workflow

## Phase 5 - Core Modules

### Module 7: Auth Module

Tasks:

- Implement admin/staff registration if needed
- Implement admin/staff login
- Hash password
- Generate JWT token
- Add authentication filter
- Protect secured endpoints
- Add current user handling

Rules:

- Tenant login is not part of MVP.
- Do not implement tenant account login in this module.

Tests:

- Register user successfully
- Login with valid credentials
- Reject login with invalid credentials
- Protected endpoint rejects missing token
- Protected endpoint rejects invalid token
- Protected endpoint accepts valid token

Completion checklist:

- Auth API contract is defined
- Auth API works
- Security behavior is tested
- `./gradlew clean test` passes
- GitHub Actions CI passes

### Module 8: Property Module

Tasks:

- Create property entity/model
- Create request/response DTOs
- Create repository
- Create service
- Create controller
- Add create property endpoint
- Add list properties endpoint
- Add get property detail endpoint
- Add update property endpoint
- Add delete/deactivate property endpoint if needed

Tests:

- Create property successfully
- Validate required fields
- List properties
- Get property by ID
- Update property
- Handle property not found

Completion checklist:

- Property API contract is defined
- Property CRUD works
- Validation works
- Unit tests pass
- API/integration tests pass if added
- `./gradlew clean test` passes
- GitHub Actions CI passes

### Module 9: Unit Module

Tasks:

- Create unit entity/model
- Create request/response DTOs
- Create repository
- Create service
- Create controller
- Add create unit endpoint
- Add list units by property endpoint
- Add get unit detail endpoint
- Add update unit endpoint
- Add deactivate unit endpoint if needed

Business rules:

- Unit number must be unique inside one property
- Monthly fee is stored as a decimal string in the database
- Monthly fee must be parsed to `BigDecimal` in Java before validation or calculation
- Due day must be valid
- Unit must belong to an existing property
- Unit must be separate from Tenant

Tests:

- Create unit successfully
- Reject duplicate unit number in same property
- Allow same unit number in different property
- Reject invalid monthly fee
- Reject invalid due day
- Handle property not found
- List units by property

Completion checklist:

- Unit API contract is defined
- Unit module works
- Unique constraint behavior is tested
- Validation works
- `./gradlew clean test` passes
- GitHub Actions CI passes

### Module 10: Tenant Module

Tasks:

- Create tenant entity/model
- Create request/response DTOs
- Create repository
- Create service
- Create controller
- Add create tenant endpoint
- Add list tenants endpoint
- Add get tenant detail endpoint
- Add update tenant endpoint

Business rules:

- Tenant is separate from Unit
- Tenant does not login in MVP
- Tenant represents a person, not a room or unit

Tests:

- Create tenant successfully
- Validate required fields
- List tenants
- Get tenant by ID
- Update tenant
- Handle tenant not found

Completion checklist:

- Tenant API contract is defined
- Tenant module works
- Validation works
- `./gradlew clean test` passes
- GitHub Actions CI passes

### Module 11: Tenant Assignment Module

Tasks:

- Assign tenant to unit
- End current active tenant assignment when tenant moves out
- List tenant assignment history by unit
- Get active tenant for unit

Business rules:

- One unit can only have one active tenant assignment at a time
- A tenant assignment needs `start_date`
- `end_date` must be null for active assignment
- Previous assignment should be ended before new active assignment is created
- Assignment history must be preserved

Tests:

- Assign tenant to empty unit
- Reject assignment if unit already has active tenant, or automatically end previous assignment depending on chosen rule
- Move tenant out
- Get active tenant by unit
- List assignment history
- Handle unit not found
- Handle tenant not found

Completion checklist:

- Tenant assignment API contract is defined
- Tenant assignment flow works
- Active tenant rule is tested
- `./gradlew clean test` passes
- GitHub Actions CI passes

### Module 12: Invoice Module

Tasks:

- Generate monthly invoice
- List invoices with filters
- Get invoice detail
- Recalculate invoice status if needed

Business rules:

- One invoice per unit per billing month
- Billing month must be first day of month
- Invoice amount uses unit monthly fee
- Due date is based on unit due day
- Generate invoice only for active unit with active tenant
- Prevent duplicate invoice generation
- Return generated invoices and skipped units
- Invoice must store both unit and tenant references at billing time

Tests:

- Generate invoice successfully
- Skip unit without active tenant
- Prevent duplicate invoice for same unit and month
- Generate due date correctly
- Use unit monthly fee correctly
- List invoices by month
- List invoices by status
- Get invoice detail
- Validate billing month format/rule

Completion checklist:

- Invoice API contract is defined
- Invoice generation works
- Duplicate prevention is tested
- Filtering works
- `./gradlew clean test` passes
- GitHub Actions CI passes

### Module 13: Payment Module

Tasks:

- Record payment for invoice
- List payments by invoice
- Recalculate invoice status after payment
- Support partial payments

Business rules:

- Payment amount is stored as a decimal string in the database
- Payment amount must be parsed to `BigDecimal` in Java and greater than zero
- Payment date is required
- Payment method is required
- One invoice can have multiple payments
- Total paid determines invoice status

Status logic:

- `unpaid`: total paid = 0
- `partial`: total paid > 0 and total paid < invoice amount
- `paid`: total paid >= invoice amount
- `overdue`: due date is past and total paid < invoice amount
- Parse payment amount and invoice amount from stored decimal strings before comparison

Tests:

- Record full payment and mark invoice as paid
- Record partial payment and mark invoice as partial
- Record multiple payments and update status correctly
- Reject negative or zero payment
- Handle invoice not found
- Mark unpaid invoice as overdue when due date is past
- Do not mark paid invoice as overdue

Completion checklist:

- Payment API contract is defined
- Payment recording works
- Invoice status recalculation is tested
- Partial payment behavior is tested
- `./gradlew clean test` passes
- GitHub Actions CI passes

### Module 14: Property Expense Module

Tasks:

- Create property expense
- List expenses by property and month
- Update expense
- Delete expense if needed

Business rules:

- Expense must belong to a property
- Amount is stored as a decimal string in the database
- Amount must be parsed to `BigDecimal` in Java and greater than zero
- Expense date is required
- Category is required

Tests:

- Create expense successfully
- Reject invalid amount
- Reject missing category
- List expenses by month
- Handle property not found
- Update expense
- Delete expense if implemented

Completion checklist:

- Property expense API contract is defined
- Expense tracking works
- Validation works
- `./gradlew clean test` passes
- GitHub Actions CI passes

### Module 15: Cash Flow Report Module

Tasks:

- Create monthly cash-flow report endpoint
- Calculate total income from payments
- Calculate total expense from property expenses
- Calculate net saving

Endpoint:

```http
GET /reports/cash-flow?propertyId=uuid&month=2026-05
```

Rules:

- Total income comes from payments joined through invoice and unit
- Total expense comes from `property_expenses`
- Do not directly join payments and expenses in one raw join because it can duplicate sums
- Use separate aggregates or CTEs
- Parse stored money strings to decimal values before calculating totals
- Net saving = total income - total expense

Tests:

- Calculate total income correctly
- Calculate total expense correctly
- Calculate net saving correctly
- Return zero when no income exists
- Return zero when no expenses exist
- Avoid duplicated sums when multiple payments and multiple expenses exist
- Handle property not found

Completion checklist:

- Cash-flow report API contract is defined
- Cash-flow report is accurate
- Duplicate sum risk is tested
- `./gradlew clean test` passes
- GitHub Actions CI passes

### Module 16: Cash Balance Closing Module

This can be after MVP.

Tasks:

- Close monthly book
- Store opening balance as a decimal string
- Store total income as a decimal string
- Store total expense as a decimal string
- Store closing balance as a decimal string
- Prevent duplicate monthly closing

Formula:

```text
closing_balance = opening_balance + total_income - total_expense
```

Parse stored balance, income, and expense strings to `BigDecimal` before applying
the formula.

Tests:

- Close month successfully
- Prevent duplicate closing for same property and month
- Carry previous month closing balance as next month opening balance
- Calculate closing balance correctly
- Handle missing previous month balance

Completion checklist:

- Cash balance API contract is defined
- Monthly closing works
- Balance formula is tested
- `./gradlew clean test` passes
- GitHub Actions CI passes

## Phase 6 - Documentation and Deployment

### Module 17: API Documentation

Tasks:

- Add Swagger/OpenAPI annotations where useful
- Update `docs/API_SPEC.md`
- Add example requests and responses
- Add error response format

Completion checklist:

- API documentation is understandable
- Swagger is accessible
- `./gradlew clean test` passes
- GitHub Actions CI passes

### Module 18: README Improvement

Tasks:

- Add project overview
- Add tech stack
- Add local setup
- Add Docker Compose instruction
- Add how to run tests
- Add API documentation link
- Add portfolio explanation
- Add issue and branch workflow

Completion checklist:

- New developer can run project from README
- `./gradlew clean test` passes
- GitHub Actions CI passes

### Module 19: Deployment

Tasks:

- Add production profile
- Add environment variable documentation
- Add deployment guide
- Deploy backend if possible
- Deploy PostgreSQL using free-tier option if possible

Completion checklist:

- Deployment guide is documented
- App can run with environment variables
- `./gradlew clean build` passes
- GitHub Actions CI passes
