# Daily Schedule

## 2026-05-31 — Issue #94: Automate monthly invoice generation

## What I Did

- Pulled latest `main`, reviewed Issue #94, and created branch `rasyid-94-automate-monthly-invoice-generation`.
- Re-read the required project docs and confirmed the current scope is internal monthly invoice automation, not a new public endpoint.
- Reviewed invoice generation requirements, database design, existing manual invoice generation workflow, OpenAPI contract, and scheduler authentication rule before changing code.
- Updated `openapi.yml` and `docs/API_SPEC.md` to clarify that `POST /invoices/generate-monthly` remains the authenticated manual trigger and automation is internal.
- Added configurable invoice automation settings with the scheduler disabled by default.
- Added an internal Spring scheduled job that generates next-month invoices for active properties only and skips duplicate/conflicting property-month generation.
- Reused existing invoice generation service rules so inactive properties, ineligible units, due date calculation, initial unpaid status, duplicate protection, and tenant/unit credit application remain consistent.
- Added unit, configuration, and PostgreSQL integration coverage for scheduled generation, duplicate skipping, inactive property handling, and scheduler enablement.
- Stayed within the Issue #94 invoice automation scope and did not add property expense, cash-flow, cash-balance, or unauthenticated invoice endpoints.

## Test Results

```text
./gradlew test --tests com.propertybilling.service.InvoiceAutomationSchedulerTest --tests com.propertybilling.service.InvoiceAutomationSchedulerContextTest --tests com.propertybilling.integration.invoice.InvoiceAutomationSchedulerIntegrationTest
Passed

./gradlew test --tests com.propertybilling.service.InvoiceServiceTest --tests com.propertybilling.controller.InvoiceControllerTest --tests com.propertybilling.integration.invoice.InvoiceGenerateMonthlyIntegrationTest --tests com.propertybilling.integration.invoice.InvoiceAutomationSchedulerIntegrationTest --tests com.propertybilling.service.InvoiceAutomationSchedulerTest --tests com.propertybilling.service.InvoiceAutomationSchedulerContextTest --tests com.propertybilling.openapi.OpenApiEndpointTest --tests com.propertybilling.PropertyBillingApiApplicationTests
Passed

./gradlew clean test
Passed
```

## Pull Request

- Issue: https://github.com/Rasyid619/property-billing-api/issues/94

## Blockers

- None.

---

## 2026-05-31 — Issue #91: Add tenant credit rollover for invoice overpayments

## What I Did

- Pulled latest `main`, reviewed Issue #91, and created branch `rasyid-91-add-tenant-credit-rollover`.
- Re-read the required project docs and confirmed the current scope is tenant/unit credit rollover inside the invoice/payment settlement module.
- Reviewed payment overpayment requirements, invoice status rules, existing invoice/payment schema, OpenAPI contract, and current payment recording workflow before changing code.
- Updated `PROJECT_GUIDE.md`, `docs/DATABASE_DESIGN.md`, `openapi.yml`, and `docs/API_SPEC.md` before controller-facing behavior changes.
- Added credit rollover migration with `tenant_unit_credits` and `credit_applications` tables plus timestamp triggers.
- Added credit and credit-application entities and repositories.
- Updated invoice responses to expose `paid_amount`, `credit_applied_amount`, and `amount_due`.
- Updated payment recording so actual cash is recorded once, overpayment creates tenant/unit credit, and credit applies automatically to same tenant/unit open invoices.
- Updated monthly invoice generation so available tenant/unit credit is automatically applied to newly generated invoices.
- Added service, migration, controller, and PostgreSQL integration coverage for overpayment credit creation, automatic credit application, partial settlement, invoice response settlement totals, and migration cleanup.
- Stayed within the Issue #91 invoice/payment settlement scope and did not implement future expense, cash balance closing, or cash-flow report controllers that are not present yet.

## Test Results

```text
./gradlew compileTestJava
Passed

./gradlew test --tests com.propertybilling.service.InvoiceServiceTest --tests com.propertybilling.controller.InvoiceControllerTest --tests com.propertybilling.integration.payment.PaymentCreateIntegrationTest --tests com.propertybilling.integration.invoice.InvoiceGenerateMonthlyIntegrationTest --tests com.propertybilling.integration.invoice.InvoiceIndexIntegrationTest --tests com.propertybilling.integration.invoice.InvoiceShowIntegrationTest --tests com.propertybilling.migration.FlywayMigrationTest
Passed

./gradlew clean test
Passed
```

## Pull Request

- Issue: https://github.com/Rasyid619/property-billing-api/issues/91

## Blockers

- None.

## Tomorrow

- Wait for Issue #91 CI before moving to the next module.

---

## 2026-05-31 — Issue #25: GET /invoices/{invoice_id}/payments

## What I Did

- Pulled latest `main`, reviewed Issue #25, and created branch `rasyid-25-get-invoices-invoice-id-payments`.
- Re-read the required project docs and confirmed the current issue scope is only `GET /invoices/{invoice_id}/payments`.
- Reviewed payment listing requirements, invoice/payment database design, existing OpenAPI contract, and current invoice/payment endpoint patterns before changing code.
- Updated `openapi.yml` and `docs/API_SPEC.md` before controller work to document the invoice payment index endpoint behavior.
- Added payment index DTOs, repository query projection, service mapping with invoice existence check, and authenticated controller endpoint.
- Added service, controller, and PostgreSQL integration tests for successful payment listing, empty payment lists for existing invoices, invoice not-found behavior, response shape, and deterministic ordering by payment date, creation timestamp, and ID.
- Stayed within the Issue #25 payment index endpoint scope and did not implement expense, report, cash balance, credit rollover, or future tenant login behavior.

## Test Results

```text
./gradlew compileTestJava
Passed

./gradlew test --tests com.propertybilling.service.InvoiceServiceTest --tests com.propertybilling.controller.InvoiceControllerTest --tests com.propertybilling.integration.payment.PaymentIndexIntegrationTest
Passed

./gradlew clean test
Passed
```

## Pull Request

- Issue: https://github.com/Rasyid619/property-billing-api/issues/25

## Blockers

- None.

## Tomorrow

- Wait for Issue #25 CI before moving to the next payment endpoint.

---

## 2026-05-31 — Issue #26: POST /invoices/{invoice_id}/payments

## What I Did

- Pulled latest `main`, reviewed Issue #26, and created branch `rasyid-26-post-invoice-payment`.
- Re-read the required project docs and confirmed the current issue scope is only `POST /invoices/{invoice_id}/payments`.
- Reviewed payment requirements, invoice status rules, database design, existing OpenAPI contract, and current invoice endpoint patterns before changing code.
- Updated `openapi.yml` and `docs/API_SPEC.md` before controller work to document positive payment validation, accepted overpayments, oldest-open-invoice surplus allocation, and surplus preservation for future credit rollover.
- Added `invoice_status` database domain migration with default `unpaid`, `payment_method` database domain migration, and shared Java enums for invoice status and payment method values.
- Added payment request DTO, payment entity, payment repository, invoice row-lock queries, payment recording workflow, invoice status recalculation, and authenticated payment controller endpoint.
- Updated payment invoice locking to use one ordered query that locks the selected invoice and same-tenant open invoices together, avoiding mixed selected-invoice/open-invoice lock ordering during surplus allocation.
- Addressed PR review comments by removing redundant explicit getters covered by Lombok and injecting a `Clock` into invoice payment status calculation so overdue tests do not depend on the wall clock.
- Added service, controller, PostgreSQL integration, and migration tests for full payment, partial payment, multiple payments, overdue recalculation, invoice not found, zero/negative amount rejection, unsupported payment method rejection, overpayment acceptance, multi-month surplus allocation, invoice status default/type behavior, and payment method type behavior.
- Stayed within the Issue #26 payment recording endpoint scope and did not implement payment index, expense, report, cash balance, or full tenant credit rollover policy from Issue #91.

## Test Results

```text
./gradlew test --tests com.propertybilling.service.InvoiceServiceTest --tests com.propertybilling.controller.InvoiceControllerTest --tests com.propertybilling.integration.payment.PaymentCreateIntegrationTest --tests com.propertybilling.migration.FlywayMigrationTest
Passed

./gradlew clean test
Passed
```

## Pull Request

- Issue: https://github.com/Rasyid619/property-billing-api/issues/26

## Blockers

- None.

## Tomorrow

- Wait for Issue #26 CI before moving to the next payment endpoint.

---

## 2026-05-31 — Issue #24: GET /invoices/{invoice_id}

## What I Did

- Pulled latest `main`, reviewed Issue #24, and used branch `rasyid-24-get-invoices-invoice-id`.
- Re-read the required project docs and confirmed the current issue scope is only `GET /invoices/{invoice_id}`.
- Reviewed invoice requirements, database design, existing OpenAPI contract, and current invoice index/generation patterns before changing code.
- Updated `openapi.yml` and `docs/API_SPEC.md` before controller work to describe invoice detail behavior.
- Added invoice detail response and query projection, joined invoice lookup by ID, service mapping, invoice not-found exception handling, and authenticated controller endpoint.
- Added service, controller, and PostgreSQL integration tests for successful invoice detail response shape and missing invoice `404`.
- Stayed within the Issue #24 invoice detail endpoint scope and did not implement payments, expenses, reports, cash balances, or future tenant login behavior.

## Test Results

```text
./gradlew test --tests com.propertybilling.service.InvoiceServiceTest --tests com.propertybilling.controller.InvoiceControllerTest --tests com.propertybilling.integration.invoice.InvoiceShowIntegrationTest
Passed

./gradlew clean test
Passed
```

## Pull Request

- Issue: https://github.com/Rasyid619/property-billing-api/issues/24

## Blockers

- None.

## Tomorrow

- Wait for Issue #24 CI before moving to the next invoice endpoint.

---

## 2026-05-31 — Issue #23: POST /invoices/generate-monthly

## What I Did

- Pulled latest `main`, reviewed Issue #23, and created branch `rasyid-23-implement-post-invoices-generate-monthly`.
- Re-read the required project docs and confirmed the current scope is only `POST /invoices/generate-monthly`.
- Reviewed invoice generation requirements, database design, existing OpenAPI contract, and current invoice index/tenant assignment patterns before changing code.
- Updated `openapi.yml` and `docs/API_SPEC.md` before controller work to clarify first-day billing month validation, active-property generation, skipped units without active tenants, duplicate invoice conflict behavior, and bodyless `201`.
- Added `InvoiceGenerateMonthlyRequest`, generation target query projection, invoice generation service workflow, duplicate invoice conflict handling, authenticated controller endpoint, and conflict exception mapping.
- Added service, controller, and PostgreSQL integration tests for successful generation, skipped ineligible units, first-day billing month validation, missing property, inactive property, duplicate invoice conflicts, due date calculation, monthly fee copying, and stored unit/tenant references.
- Stayed within the Issue #23 invoice generation endpoint scope and did not implement invoice detail, payments, expenses, reports, cash balances, or credit rollover behavior.

## Test Results

```text
./gradlew test --tests com.propertybilling.service.InvoiceServiceTest --tests com.propertybilling.controller.InvoiceControllerTest
Passed

./gradlew test --tests com.propertybilling.integration.invoice.InvoiceGenerateMonthlyIntegrationTest
Passed

./gradlew test --tests com.propertybilling.service.InvoiceServiceTest --tests com.propertybilling.controller.InvoiceControllerTest --tests com.propertybilling.integration.invoice.InvoiceIndexIntegrationTest --tests com.propertybilling.integration.invoice.InvoiceGenerateMonthlyIntegrationTest
Passed

./gradlew clean test
Passed
```

## Pull Request

- Issue: https://github.com/Rasyid619/property-billing-api/issues/23

## Blockers

- None.

## Tomorrow

- Wait for Issue #23 CI before moving to the next invoice endpoint.

---

## 2026-05-31 — Issue #22: GET /invoices

## What I Did

- Pulled latest `main`, reviewed Issue #22, and created branch `rasyid-22-implement-get-invoices`.
- Re-read the required project docs and confirmed the current scope is only `GET /invoices`.
- Reviewed invoice requirements, database design, existing OpenAPI contract, and current index endpoint patterns before changing code.
- Updated `openapi.yml` and `docs/API_SPEC.md` before controller work to clarify invoice index ordering and month/status query validation.
- Added invoice entity mapping, repository projection query, DTOs, service workflow, and authenticated controller endpoint for listing invoices.
- Added service, controller, and PostgreSQL integration tests for invoice index response shape, filters, validation, and newest-month ordering.
- Fixed CI integration-test data cleanup by clearing shared PostgreSQL test tables in foreign-key-safe order before each integration test.
- Replaced the invoice index static nullable-parameter JPQL with a Criteria API query so PostgreSQL only receives bound filters that are actually present.
- Stayed within the Issue #22 invoice index endpoint scope and did not implement invoice generation, invoice detail, payments, expenses, or credit rollover behavior.

## Test Results

```text
./gradlew test --tests com.propertybilling.service.InvoiceServiceTest --tests com.propertybilling.controller.InvoiceControllerTest
Passed

./gradlew compileTestJava
Passed

./gradlew test --tests com.propertybilling.service.InvoiceServiceTest --tests com.propertybilling.controller.InvoiceControllerTest --tests com.propertybilling.integration.invoice.InvoiceIndexIntegrationTest
Failed: Testcontainers could not find a valid Docker environment.

./gradlew clean test
Failed: Testcontainers could not find a valid Docker environment; Docker is not installed or integrated in this WSL distro.
```

## Pull Request

- Issue: https://github.com/Rasyid619/property-billing-api/issues/22

## Blockers

- Local Docker/Testcontainers verification is blocked because the `docker` command is unavailable in this WSL distro.

## Tomorrow

- Wait for Issue #22 CI before moving to the next invoice endpoint.

---

## 2026-05-28 — Issue #39: PATCH /unit-tenant-assignments/{assignmentId}/move-out

## What I Did

- Pulled latest `main`, reviewed Issue #39, and created branch `rasyid-39-patch-unit-tenant-assignments-assignmentid-move-out`.
- Re-read the required project docs and confirmed the current scope is only `PATCH /unit-tenant-assignments/{assignmentId}/move-out`.
- Reviewed tenant assignment requirements, database design, existing OpenAPI contract, and current tenant assignment create/history/active lookup patterns before changing code.
- Updated `openapi.yml` and `docs/API_SPEC.md` before controller work to make move-out bodyless.
- Added row-locked assignment lookup, entity move-out behavior, service validation, authenticated controller endpoint, and generated move-out date handling.
- Added service, controller, and PostgreSQL integration tests for successful move-out, invalid generated end date, missing assignment, and already-closed assignment behavior.
- Stayed within the Issue #39 tenant assignment move-out endpoint scope and did not implement future tenant login or invoice behavior.

## Test Results

```text
./gradlew test --tests com.propertybilling.service.TenantAssignmentServiceTest --tests com.propertybilling.controller.TenantAssignmentControllerTest --tests com.propertybilling.integration.tenantassignment.TenantAssignmentMoveOutIntegrationTest
./gradlew clean test
Passed
```

## Pull Request

- Issue: https://github.com/Rasyid619/property-billing-api/issues/39

## Blockers

- None.

## Tomorrow

- Wait for Issue #39 CI before moving to the next endpoint.

---

## 2026-05-28 — Issue #44: GET /units/{unit_id}/tenant-assignments

## What I Did

- Pulled latest `main`, reviewed Issue #44, and created branch `rasyid-44-get-units-unit-id-tenant-assignments`.
- Re-read the required project docs and confirmed the current scope is only `GET /units/{unit_id}/tenant-assignments`.
- Reviewed tenant assignment requirements, database design, existing OpenAPI contract, and current tenant assignment create/active lookup patterns before changing code.
- Updated `openapi.yml` and `docs/API_SPEC.md` before controller work to clarify tenant assignment history list behavior.
- Added tenant assignment index DTOs, repository history lookup, service workflow, and authenticated controller endpoint for listing assignment history by unit.
- Added service, controller, and PostgreSQL integration tests for assignment history response shape, newest-first ordering, empty history, and missing-unit behavior.
- Stayed within the Issue #44 tenant assignment history endpoint scope and did not implement move-out or other future endpoints.

## Test Results

```text
./gradlew test --tests com.propertybilling.service.TenantAssignmentServiceTest --tests com.propertybilling.controller.TenantAssignmentControllerTest --tests com.propertybilling.integration.tenantassignment.TenantAssignmentIndexIntegrationTest
./gradlew clean test
Passed
```

## Pull Request

- Issue: https://github.com/Rasyid619/property-billing-api/issues/44

## Blockers

- None.

## Tomorrow

- Wait for Issue #44 CI before moving to the next tenant assignment endpoint.

---

## 2026-05-28 — Issue #45: POST /units/{unit_id}/tenant-assignments

## What I Did

- Pulled latest `main`, reviewed Issue #45, and created branch `rasyid-45-post-units-unit-id-tenant-assignments`.
- Re-read the required project docs and confirmed the current scope is only `POST /units/{unit_id}/tenant-assignments`.
- Reviewed the tenant assignment requirements, database design, existing OpenAPI contract, and current unit/tenant assignment patterns before changing code.
- Updated `openapi.yml` and `docs/API_SPEC.md` before controller work to clarify tenant assignment create request fields.
- Added `TenantAssignmentCreateRequest`, active assignment conflict handling, authenticated controller support, and service logic that locks the unit row before assignment creation.
- Added a PostgreSQL partial unique index to enforce one active tenant assignment per unit at the database layer.
- Added service, controller, integration, and migration tests for successful assignment, request validation, missing unit/tenant behavior, duplicate active assignment conflict, and database constraint coverage.
- Stayed within the Issue #45 tenant assignment create endpoint scope and did not implement move-out or history endpoints.

## Test Results

```text
./gradlew test --tests com.propertybilling.service.TenantAssignmentServiceTest --tests com.propertybilling.controller.TenantAssignmentControllerTest --tests com.propertybilling.integration.tenantassignment.TenantAssignmentCreateIntegrationTest --tests com.propertybilling.migration.FlywayMigrationTest
./gradlew clean test
Passed
```

## Pull Request

- Issue: https://github.com/Rasyid619/property-billing-api/issues/45

## Blockers

- None.

## Tomorrow

- Wait for Issue #45 CI before moving to the next tenant assignment endpoint.

---

## 2026-05-28 — Issue #43: GET /units/{unit_id}/active-tenant

## What I Did

- Pulled latest `main`, reviewed Issue #43, and created branch `rasyid-43-get-units-unit-id-active-tenant`.
- Re-read the required project docs and confirmed the current scope is only `GET /units/{unit_id}/active-tenant`.
- Reviewed the unit, tenant, and tenant assignment requirements, database design, existing OpenAPI contract, and current unit/tenant implementation patterns before changing code.
- Updated `openapi.yml` and `docs/API_SPEC.md` before controller work to add the active tenant assignment behavior description.
- Added the tenant assignment entity, repository lookup, response DTO, service workflow, authenticated controller endpoint, and not-found exception mapping for active tenant lookup.
- Added service, controller, and PostgreSQL integration tests for successful active tenant retrieval, missing unit behavior, missing active assignment behavior, authentication, and inactive assignment filtering.
- Stayed within the Issue #43 tenant assignment endpoint scope and did not implement other tenant assignment endpoints or future tenant login behavior.

## Test Results

```text
./gradlew test --tests com.propertybilling.service.TenantAssignmentServiceTest --tests com.propertybilling.controller.TenantAssignmentControllerTest --tests com.propertybilling.integration.tenantassignment.TenantAssignmentActiveTenantIntegrationTest
./gradlew clean test
Passed
```

## Pull Request

- Issue: https://github.com/Rasyid619/property-billing-api/issues/43

## Blockers

- None.

## Tomorrow

- Push the Issue #43 branch, open the pull request, and wait for CI before moving to the next endpoint.

---

## 2026-05-25 (continued) — Issue #38: PATCH /tenants/{tenant_id}

## What I Did

- Pulled latest `main`, reviewed Issue #38, and created branch `rasyid-38-patch-tenants-tenant-id`.
- Re-read the required project docs and confirmed the current scope is the Tenant module update endpoint only.
- Reviewed the tenant requirements, database design, uniqueness rules, and existing create/detail patterns before changing code.
- Updated `openapi.yml` and `docs/API_SPEC.md` before controller work to clarify tenant update replaces name, phone, and email, and rejects duplicate contact values with `409`.
- Added `TenantUpdateRequest`, tenant entity update behavior, row-locked repository lookup, duplicate contact checks, and authenticated controller support for `PATCH /api/v1/tenants/{tenant_id}`.
- Added service, controller, and PostgreSQL integration tests for successful tenant update, validation failure, tenant-not-found behavior, and duplicate phone/email conflicts.
- Stayed within the Tenant module scope and did not implement tenant assignment or login behavior.

## Test Results

```text
./gradlew test --tests com.propertybilling.service.TenantServiceTest --tests com.propertybilling.controller.TenantControllerTest --tests com.propertybilling.integration.tenant.TenantUpdateIntegrationTest
./gradlew clean test
Passed
```

## Pull Request

- Issue: https://github.com/Rasyid619/property-billing-api/issues/38

## Blockers

- None.

## Tomorrow

- Open the Issue #38 pull request and wait for CI before moving to the next tenant endpoint.

---

## 2026-05-25 (continued) — Issue #37: GET /tenants/{tenant_id}

## What I Did

- Pulled latest `main`, reviewed Issue #37, and created branch `rasyid-37-get-tenants-tenant-id`.
- Re-read the required project docs and confirmed the current scope is the Tenant module detail endpoint only.
- Reviewed the tenant requirements, database design, and existing tenant/property detail patterns before changing code.
- Updated `openapi.yml` and `docs/API_SPEC.md` before controller work to clarify tenant detail returns data records only and does not involve tenant login accounts.
- Added `TenantShowResponse`, a tenant not-found exception mapped to `404`, the tenant detail service workflow, and authenticated controller support for `GET /api/v1/tenants/{tenant_id}`.
- Added service, controller, and PostgreSQL integration tests for successful tenant detail retrieval and tenant-not-found behavior.
- Stayed within the Tenant module scope and did not implement tenant update, assignment, or login behavior.

## Test Results

```text
./gradlew test --tests com.propertybilling.service.TenantServiceTest --tests com.propertybilling.controller.TenantControllerTest --tests com.propertybilling.integration.tenant.TenantShowIntegrationTest
./gradlew clean test
Passed
```

## Pull Request

- Issue: https://github.com/Rasyid619/property-billing-api/issues/37

## Blockers

- None.

## Tomorrow

- Open the Issue #37 pull request and wait for CI before moving to the next tenant endpoint.

---

## 2026-05-25 (continued) — Issue #36: POST /tenants

## What I Did

- Pulled latest `main`, reviewed Issue #36, and created branch `rasyid-36-post-tenants`.
- Re-read the required project docs and confirmed the current scope is the Tenant module create endpoint only.
- Reviewed the tenant requirements, database design, and existing property/unit create patterns before changing code.
- Updated `openapi.yml` and `docs/API_SPEC.md` before controller work to clarify tenant creation stores data records only, does not create login accounts, and rejects duplicate phone or email values.
- Added `TenantCreateRequest`, the tenant creation service workflow, unique tenant phone/email persistence rules, and authenticated controller support for `POST /api/v1/tenants`.
- Added service, controller, migration, and PostgreSQL integration tests for successful tenant creation, validation failure, duplicate contact conflicts, and persistence.
- Stayed within the Tenant module scope and did not implement tenant detail, update, assignment, or login behavior.

## Test Results

```text
./gradlew test --tests com.propertybilling.service.TenantServiceTest --tests com.propertybilling.controller.TenantControllerTest --tests com.propertybilling.integration.tenant.TenantCreateIntegrationTest --tests com.propertybilling.migration.FlywayMigrationTest
./gradlew clean test
Passed
```

## Pull Request

- Issue: https://github.com/Rasyid619/property-billing-api/issues/36

## Blockers

- None.

## Tomorrow

- Open the Issue #36 pull request and wait for CI before moving to the next tenant endpoint.

---

## 2026-05-25 — Issue #35: GET /tenants

## What I Did

- Pulled latest `main`, reviewed Issue #35, and created branch `rasyid-35-get-tenants`.
- Re-read the required project docs and confirmed the current scope is the Tenant module list endpoint only.
- Reviewed the tenant requirements, database design, and existing property/unit index patterns before changing code.
- Updated `openapi.yml` and `docs/API_SPEC.md` before controller work to clarify optional tenant search behavior.
- Added tenant entity, repository projection query, DTOs, service workflow, and authenticated controller endpoint for `GET /api/v1/tenants`.
- Added service, controller, and PostgreSQL integration tests for listing tenants, search filtering, validation, and authentication behavior.
- Stayed within the Tenant module scope and did not implement tenant create, detail, update, assignment, or login behavior.

## Test Results

```text
./gradlew test --tests com.propertybilling.service.TenantServiceTest --tests com.propertybilling.controller.TenantControllerTest --tests com.propertybilling.integration.tenant.TenantIndexIntegrationTest
./gradlew clean test
Passed
```

## Pull Request

- Issue: https://github.com/Rasyid619/property-billing-api/issues/35

## Blockers

- None.

## Tomorrow

- Push the Issue #35 branch, open the pull request, and wait for CI before moving to the next tenant endpoint.

---

## Purpose

This schedule is used to treat the project like a full-time job while looking for a new backend engineering opportunity.

The daily goals are:

- Build the portfolio project
- Improve backend engineering skill
- Apply to jobs consistently
- Keep progress visible
- Maintain discipline

## Working Days

Monday to Friday.

## Daily Routine

| Time | Activity |
|---|---|
| 09:00 - 09:30 | Planning |
| 09:30 - 11:30 | Deep work: project development |
| 11:30 - 12:00 | Run tests, commit, write notes |
| 12:00 - 13:00 | Break |
| 13:00 - 15:00 | Deep work: project development |
| 15:00 - 16:00 | Job applications and recruiter messages |
| 16:00 - 17:00 | Fix bugs, tests, documentation |
| 17:00 - 17:30 | Push code and write daily summary |

## Daily Project Checklist

```text
[ ] Review today’s GitHub Issue
[ ] Create branch from issue
[ ] Pull latest code
[ ] Read related docs
[ ] Update API contract if needed
[ ] Implement small feature
[ ] Write or update tests
[ ] Run ./gradlew clean test
[ ] Commit changes
[ ] Push branch to GitHub
[ ] Open or update pull request
[ ] Link pull request to issue
[ ] Check GitHub Actions CI
[ ] Write daily summary
```

## Daily Job Search Checklist

```text
[ ] Apply to 5 backend or remote jobs
[ ] Message 3 recruiters
[ ] Save job applications in tracker
[ ] Improve CV or LinkedIn if needed
[ ] Review one interview topic
```

## Weekly Target

```text
25 job applications per week
15 recruiter messages per week
5 meaningful project commits per week
1 completed module or meaningful module progress per week
```

## Weekly Plan

## Week 1 - Foundation

Goals:

- Create repository
- Add documentation
- Add Codex instructions
- Create base Spring Boot project
- Add Docker Compose PostgreSQL
- Add Flyway setup
- Add CI
- Add initial database migration
- Add OpenAPI foundation

## Week 2 - Core Master Data

Goals:

- Property module with tests
- Unit module with tests
- Tenant module with tests
- Tenant assignment module with tests

## Week 3 - Billing Logic

Goals:

- Invoice generation with tests
- Payment recording with tests
- Invoice status recalculation with tests
- Property expense tracking with tests

## Week 4 - Reports and Portfolio Polish

Goals:

- Cash-flow report with tests
- Swagger documentation
- README improvement
- Deployment guide
- Prepare repo to become public
- Add project to CV and LinkedIn

## End-of-Day Summary Template

Use this at the end of each day:

```md
# Daily Summary - YYYY-MM-DD

## Completed

- 

## Tests

Command:

```bash
./gradlew clean test
```

Result:

```text
Passed / Failed
```

## Pull Request

- PR:
- Issue:

## Blockers

- 

## Tomorrow

- 
```

## Rules

- Do not skip tests.
- Do not jump to future modules.
- Do not work directly on `main`.
- Do not work without a GitHub Issue.
- Do not spend the full day only coding without applying to jobs.
- Do not apply to jobs without improving the project.
- Keep commits small and meaningful.
- Treat documentation as part of the work.

# Daily Summary - 2026-05-14

## Completed

- Added project documentation and workflow.
- Created base Spring Boot Gradle project.
- Added Docker Compose PostgreSQL setup.
- Verified base test passes.
- Added and verified health endpoint test.
- Added GitHub Actions test workflow.

## Tests

Command:

```bash
./gradlew clean test
```

Result:

```text
Targeted tests passed. Full clean test blocked locally by sandbox escalation approval limit.
```

## Pull Request

- PR: https://github.com/Rasyid619/property-billing-api/pull/6
- Issue: https://github.com/Rasyid619/property-billing-api/issues/5

## Blockers

- Docker could not be run in this local WSL distro because Docker is not installed or WSL integration is disabled.

## Tomorrow

- Continue with the next module only after PR checks and review are complete.

# Daily Summary - 2026-05-17

## Completed

- Added the initial Flyway database migrations.
- Added automatic `updated_at` triggers for database tables.
- Made local workflow documents private to local development.
- Added and refined the OpenAPI contract foundation.
- Standardized direct API responses, reusable error responses, `snake_case` fields, index counts, and offset/limit pagination rules.
- Added auth refresh contract support.
- Created one implementation issue for each contracted endpoint.

## Tests

Command:

```bash
./gradlew clean test
```

Result:

```text
Passed
```

## Pull Request

- PR: https://github.com/Rasyid619/property-billing-api/pull/8
- PR: https://github.com/Rasyid619/property-billing-api/pull/10
- PR: https://github.com/Rasyid619/property-billing-api/pull/12
- Issue: https://github.com/Rasyid619/property-billing-api/issues/7
- Issue: https://github.com/Rasyid619/property-billing-api/issues/9
- Issue: https://github.com/Rasyid619/property-billing-api/issues/11

## Blockers

- No blockers.

## Tomorrow

- Start endpoint implementation from the contract issues, beginning with the next module in order.

# Daily Summary - 2026-05-18

## Completed

- Completed four issue and pull request threads today.
- Implemented auth login, refresh-token renewal, and current-user endpoint work.
- Added focused auth unit, controller, and integration coverage.
- Refined auth package structure, exception handling, and JWT validation behavior.
- Strengthened CI to expose separate test and build jobs.

## Tests

Command:

```bash
./gradlew clean test
```

Result:

```text
Passed
```

## Pull Request

- PR: https://github.com/Rasyid619/property-billing-api/pull/51
- PR: https://github.com/Rasyid619/property-billing-api/pull/53
- PR: https://github.com/Rasyid619/property-billing-api/pull/55
- Issue: https://github.com/Rasyid619/property-billing-api/issues/13
- Issue: https://github.com/Rasyid619/property-billing-api/issues/14
- Issue: https://github.com/Rasyid619/property-billing-api/issues/50
- Issue: https://github.com/Rasyid619/property-billing-api/issues/52

## Blockers

- No blockers.

## Tomorrow

- Finish the remaining auth endpoint work, then continue to the next module in order after review and CI are complete.

# Daily Summary - 2026-05-19

## Completed

- Started GitHub Issue #60 on branch `rasyid-60-add-prometheus-and-grafana-monitoring`.
- Pulled latest `main` before implementation.
- Added Prometheus and Grafana monitoring setup for local development.
- Updated OpenAPI and API documentation for actuator health and Prometheus metrics endpoints.

## Tests

Command:

```bash
./gradlew test --tests com.propertybilling.integration.PrometheusMetricsIntegrationTest
./gradlew clean test
```

Result:

```text
Passed
```

## Pull Request

- PR: https://github.com/Rasyid619/property-billing-api/pull/61
- Issue: https://github.com/Rasyid619/property-billing-api/issues/60

## Blockers

- None yet.

## Tomorrow

- Continue with the next module only after Issue #60 tests and review are complete.

# Daily Summary - 2026-05-20

## Completed

- Pulled latest `main` before implementation.
- Reviewed GitHub Issue #27 and required project docs.
- Created branch `rasyid-27-get-properties`.
- Implemented `GET /properties` within the property module scope.
- Added property entity, repository, DTOs, service, controller, and validation.
- Kept the OpenAPI contract and API spec aligned with the documented `200` and `401` responses.
- Removed timestamp fields from property list responses and added a property status filter.
- Added focused service, controller, and integration tests for property listing.
- Started GitHub Issue #62 on branch `rasyid-62-refactor-project-boilerplate-using-lombok`.
- Added Lombok Gradle configuration.
- Refactored repetitive constructor and getter boilerplate using limited Lombok annotations.
- Kept DTO and query result records unchanged.

## Tests

Command:

```bash
./gradlew test --tests com.propertybilling.service.PropertyServiceTest --tests com.propertybilling.controller.PropertyControllerTest --tests com.propertybilling.integration.PropertyIndexIntegrationTest --tests com.propertybilling.service.AuthServiceTest
./gradlew test --tests com.propertybilling.controller.AuthControllerTest --tests com.propertybilling.controller.PropertyControllerTest --tests com.propertybilling.service.AuthServiceTest --tests com.propertybilling.service.PropertyServiceTest --tests com.propertybilling.integration.AuthLoginIntegrationTest --tests com.propertybilling.integration.AuthMeIntegrationTest --tests com.propertybilling.integration.PropertyIndexIntegrationTest
./gradlew clean test
```

Result:

```text
Passed
```

## Pull Request

- PR:
- PR: https://github.com/Rasyid619/property-billing-api/pull/64
- Issue: https://github.com/Rasyid619/property-billing-api/issues/27
- Issue: https://github.com/Rasyid619/property-billing-api/issues/62

## Blockers

- No blockers.

## Tomorrow

- Open the Lombok refactor pull request and wait for CI before moving to the next endpoint issue.

# Daily Summary - 2026-05-21

## Completed

- Pulled latest `main` before implementation.
- Reviewed GitHub Issue #28 and required project docs.
- Created branch `rasyid-28-post-properties`.
- Confirmed the approved `POST /properties` contract and aligned operation descriptions in `openapi.yml` and `docs/API_SPEC.md`.
- Implemented property creation with validation, authentication, service logic, persistence, and lean `201` response.
- Added focused service, controller, and integration tests for property creation.
- Segmented property tests by create and index behavior, moved property integration tests under `integration/property`, and documented Mockito call-count verification expectations.
- Addressed PR review comments by aligning the non-blank property-name contract and normalizing body validation errors to empty `400` responses.
- Started GitHub Issue #29 on branch `rasyid-29-get-properties-property-id`.
- Implemented `GET /properties/{property_id}` with property detail response mapping and empty `404` handling.
- Added focused service, controller, and integration tests for property detail retrieval.
- Started GitHub Issue #31 on branch `rasyid-31-delete-properties-property-id`.
- Implemented soft property deactivation with transaction handling and row locking.
- Added `POST /properties/{property_id}/activate` in the same active-state workflow, with matching transaction handling and row locking.
- Added focused service, controller, and integration tests for property activation and deactivation.

## Tests

Command:

```bash
./gradlew test --tests com.propertybilling.controller.PropertyControllerTest --tests com.propertybilling.service.PropertyServiceTest --tests com.propertybilling.integration.property.PropertyCreateIntegrationTest
./gradlew test --tests com.propertybilling.controller.PropertyControllerTest --tests com.propertybilling.controller.AuthControllerTest --tests com.propertybilling.integration.property.PropertyCreateIntegrationTest --tests com.propertybilling.openapi.OpenApiEndpointTest
./gradlew test --tests com.propertybilling.service.PropertyServiceTest --tests com.propertybilling.controller.PropertyControllerTest --tests com.propertybilling.integration.property.PropertyShowIntegrationTest
./gradlew test --tests com.propertybilling.service.PropertyServiceTest --tests com.propertybilling.controller.PropertyControllerTest --tests com.propertybilling.integration.property.PropertyDeleteIntegrationTest
./gradlew test --tests com.propertybilling.service.PropertyServiceTest --tests com.propertybilling.controller.PropertyControllerTest --tests com.propertybilling.integration.property.PropertyDeleteIntegrationTest --tests com.propertybilling.integration.property.PropertyActivateIntegrationTest
./gradlew clean test
```

Result:

```text
Passed
```

## Pull Request

- PR: https://github.com/Rasyid619/property-billing-api/pull/67
- PR: https://github.com/Rasyid619/property-billing-api/pull/69
- Issue: https://github.com/Rasyid619/property-billing-api/issues/28
- Issue: https://github.com/Rasyid619/property-billing-api/issues/29
- Issue: https://github.com/Rasyid619/property-billing-api/issues/31

## Blockers

- None.

## Tomorrow

- Wait for CI review on Issue #28 before moving to the next endpoint issue.

---

## 2026-05-21 (continued) — Issue #30: PUT /properties/{property_id}

## What I Did

- Changed update method from `PATCH` to `PUT` per convention.
- Updated `openapi.yml`: changed `patch` to `put` for `/properties/{property_id}`, added description.
- Updated `docs/API_SPEC.md` to match.
- Created `PropertyUpdateRequest` DTO with `@NotBlank name` and optional `address`.
- Added `update(String name, String address)` domain method to `Property` entity.
- Added `updateProperty(UUID, PropertyUpdateRequest)` to `PropertyService` with `@Transactional` and `findByIdForUpdate` (pessimistic write lock) — consistent with deactivate/activate pattern.
- Added `PUT /{property_id}` endpoint to `PropertyController` returning `204 No Content`.
- Added `UpdateProperty` nested class to `PropertyServiceTest` (2 tests: success + not found).
- Added `UpdateProperty` nested class to `PropertyControllerTest` (5 tests: 204, 400, 404, 401 missing header, 401 invalid token).
- Created `PropertyUpdateIntegrationTest` (4 tests: success + DB assertion, 404, 401, 400).
- All tests pass, JaCoCo 95% coverage gate passes.

## Test Results

```text
Passed
```

## Pull Request

- Issue: https://github.com/Rasyid619/property-billing-api/issues/30

## Blockers

- None.

## Tomorrow

- Open PR for Issue #30 and wait for CI.

---

## 2026-05-21 (continued) — Issue #65: refactor integration tests to Testcontainers

## What I Did

- Pulled latest `main`, reviewed GitHub Issue #65, and stayed within the integration-test refactor scope.
- Replaced H2-backed auth and property integration tests with a shared PostgreSQL Testcontainers base class.
- Added `AbstractIntegrationTest` to centralize datasource, Flyway placeholder, and JWT test configuration.
- Updated auth and property integration tests to extend the shared PostgreSQL-backed base class.
- Fixed the shared container lifecycle so one PostgreSQL container stays available across all migrated integration test classes.
- Mapped `created_at` and `updated_at` on `User` so repository-backed test fixtures match the real PostgreSQL schema.
- Verified the migrated integration tests against Flyway-managed PostgreSQL instead of per-test H2 schemas.

## Test Results

```text
./gradlew test --tests com.propertybilling.integration.AuthLoginIntegrationTest --tests com.propertybilling.integration.property.PropertyShowIntegrationTest
./gradlew clean test
Passed
```

## Pull Request

- Issue: https://github.com/Rasyid619/property-billing-api/issues/65

## Blockers

- None.

## Tomorrow

- Wait for pull request review and CI before moving to the next module.

---

## 2026-05-21 (continued) — Issue #68: add database defaults for created_at and updated_at

## What I Did

- Pulled latest `main`, reviewed GitHub Issue #68, and re-read the required project docs before implementation.
- Confirmed this issue does not change the public API, so `openapi.yml` and `docs/API_SPEC.md` did not need updates.
- Added a new Flyway migration to set `CURRENT_TIMESTAMP` defaults for `created_at` and `updated_at` on all existing tables.
- Updated the current JPA timestamp mappings so inserts and updates rely on database-managed timestamps instead of application-populated values.
- Refactored property creation to stop generating timestamps in service code and rely on the database defaults.
- Extended migration coverage to verify inserts succeed without explicit timestamp values.
- Updated the property create service test to assert the service no longer populates timestamps before persistence.
- Documented the insert-default rule in `docs/DATABASE_DESIGN.md`.

## Test Results

```text
./gradlew test --tests com.propertybilling.migration.FlywayMigrationTest --tests com.propertybilling.service.PropertyServiceTest --tests com.propertybilling.integration.property.PropertyCreateIntegrationTest --tests com.propertybilling.integration.AuthLoginIntegrationTest
./gradlew clean test
Passed
```

## Pull Request

- Issue: https://github.com/Rasyid619/property-billing-api/issues/68

## Blockers

- None.

## Tomorrow

- Open the Issue #68 pull request and wait for CI before moving to the next module.

---

## 2026-05-22 — Issue #32: GET /properties/{property_id}/units

## What I Did

- Pulled latest `main`, created branch `rasyid-32-get-properties-property-id-units`, and re-read the required project docs.
- Reviewed requirements and database design for the Unit module list-by-property endpoint.
- Updated `openapi.yml` and `docs/API_SPEC.md` before controller implementation.
- Added nullable `status` query filtering for units; omitted, blank, or `status=null` returns all units.
- Implemented Unit entity mapping, repository projection/query, DTOs, service, and authenticated controller endpoint.
- Added validation for `offset` and `limit` query parameters.
- Added service, controller, and PostgreSQL integration tests for listing units by property, status filtering, not found, auth, and validation behavior.
- Stayed within the Unit list-by-property endpoint scope and did not implement future Unit mutations.

## Test Results

```text
./gradlew test --tests com.propertybilling.service.UnitServiceTest --tests com.propertybilling.controller.UnitControllerTest --tests com.propertybilling.integration.unit.UnitIndexByPropertyIntegrationTest
./gradlew clean test
Passed
```

## Pull Request

- Issue: https://github.com/Rasyid619/property-billing-api/issues/32

## Blockers

- None.

## Tomorrow

- Open the Issue #32 pull request and wait for CI before moving to the next endpoint issue.

---

## 2026-05-22 (continued) — Issue #33: POST /properties/{property_id}/units

## What I Did

- Pulled latest `main`, created branch `rasyid-33-post-properties-property-id-units`, and re-read the required project docs.
- Reviewed requirements and database design for creating units inside properties.
- Updated `openapi.yml` and `docs/API_SPEC.md` before controller implementation.
- Changed `UnitCreateRequest.monthly_fee` contract to receive a validated decimal string.
- Implemented create-unit request DTO, duplicate unit-number conflict exception, repository duplicate check, service create workflow, and authenticated controller endpoint.
- Persisted units as active by default and stored `monthly_fee` as a decimal string after parsing with `BigDecimal`.
- Added service, controller, and PostgreSQL integration tests for successful create, duplicate conflict, same unit number in different property, invalid monthly fee, invalid due day, property not found, and auth behavior.
- Stayed within the Unit create endpoint scope and did not implement future Unit update/delete/detail endpoints.

## Test Results

```text
./gradlew test --tests com.propertybilling.service.UnitServiceTest --tests com.propertybilling.controller.UnitControllerTest --tests com.propertybilling.integration.unit.UnitCreateIntegrationTest --tests com.propertybilling.integration.unit.UnitIndexByPropertyIntegrationTest
./gradlew clean test
Passed
```

## Pull Request

- Issue: https://github.com/Rasyid619/property-billing-api/issues/33

## Blockers

- None.

## Tomorrow

- Open the Issue #33 pull request and wait for CI before moving to the next endpoint issue.

---

## 2026-05-22 (continued) — Issue #75: remove redundant endpoint auth tests

## What I Did

- Pulled latest `main`, created branch `rasyid-75-remove-redundant-endpoint-auth-tests`, and re-read the required project docs.
- Reviewed Issue #75 and confirmed this is a test cleanup with no API contract or database changes.
- Audited controller and integration tests for repeated missing-token and invalid-token endpoint cases.
- Removed redundant auth-only tests from Property and Unit endpoint tests.
- Kept endpoint-specific tests for validation, not found, filtering, persistence, response shape, and business behavior.
- Kept auth tests in the Auth module as the representative authentication coverage.
- Updated `docs/TESTING_STRATEGY.md` to document the no-redundant-auth-test rule.

## Test Results

```text
./gradlew test --tests com.propertybilling.controller.PropertyControllerTest --tests com.propertybilling.controller.UnitControllerTest --tests com.propertybilling.integration.property.PropertyCreateIntegrationTest --tests com.propertybilling.integration.property.PropertyUpdateIntegrationTest --tests com.propertybilling.integration.property.PropertyActivateIntegrationTest --tests com.propertybilling.integration.property.PropertyDeleteIntegrationTest --tests com.propertybilling.integration.property.PropertyShowIntegrationTest --tests com.propertybilling.integration.property.PropertyIndexIntegrationTest --tests com.propertybilling.integration.unit.UnitIndexByPropertyIntegrationTest
./gradlew clean test
Passed
```

## Pull Request

- Issue: https://github.com/Rasyid619/property-billing-api/issues/75

## Blockers

- None.

## Tomorrow

- Open the Issue #75 pull request and wait for CI before moving to the next endpoint issue.

---

## 2026-05-22 (continued) — Issue #42: unit activation and deactivation

## What I Did

- Pulled latest `main`, created branch `rasyid-42-delete-units-unit-id`, and re-read the required project docs.
- Reviewed Issue #42, the Unit module requirements, and the database design before changing code, then expanded the issue scope to include unit activation.
- Updated `openapi.yml` and `docs/API_SPEC.md` before controller work to document both unit deactivation history preservation and unit reactivation.
- Followed the existing property activation and deactivation patterns for the unit module by adding row-locked repository lookups, service workflows, and authenticated controller endpoints.
- Added unit-level activation and deactivation support in the entity and reused a dedicated `UnitNotFoundException` mapped to `404`.
- Added service, controller, and PostgreSQL integration tests for successful unit activation and deactivation plus unit-not-found behavior.
- Stayed within the Unit module scope and did not implement other future unit endpoints.

## Test Results

```text
./gradlew test --tests com.propertybilling.service.UnitServiceTest --tests com.propertybilling.controller.UnitControllerTest --tests com.propertybilling.integration.unit.UnitDeleteIntegrationTest --tests com.propertybilling.integration.unit.UnitActivateIntegrationTest
./gradlew clean test
Passed
```

## Pull Request

- Issue: https://github.com/Rasyid619/property-billing-api/issues/42

## Blockers

- None.

## Tomorrow

- Wait for the expanded Issue #42 pull request checks before moving to the next endpoint issue.

---

## 2026-05-22 (continued) — Issue #40: GET /units/{unit_id}

## What I Did

- Pulled latest `main`, created branch `rasyid-40-get-units-unit-id`, and re-read the required project docs.
- Reviewed Issue #40, the Unit module requirements, and the database design before changing code.
- Updated `openapi.yml` and `docs/API_SPEC.md` before controller work to clarify the unit detail response behavior.
- Added `UnitShowResponse` for the contracted detail payload and a repository read method that fetches the owning property for detail mapping.
- Implemented the unit detail service workflow and authenticated controller endpoint for `GET /api/v1/units/{unit_id}`.
- Added service, controller, and PostgreSQL integration tests for successful unit detail retrieval and unit-not-found behavior.
- Stayed within the Unit module scope and did not implement future unit update or tenant-assignment detail behavior.

## Test Results

```text
./gradlew test --tests com.propertybilling.service.UnitServiceTest --tests com.propertybilling.controller.UnitControllerTest --tests com.propertybilling.integration.unit.UnitShowIntegrationTest
./gradlew clean test
Passed
```

## Pull Request

- Issue: https://github.com/Rasyid619/property-billing-api/issues/40

## Blockers

- None.

## Tomorrow

- Open the Issue #40 pull request and wait for CI before moving to the next endpoint issue.

---

## 2026-05-22 (continued) — Issue #41: PUT /units/{unit_id}

## What I Did

- Pulled latest `main`, created branch `rasyid-41-put-units-unit-id`, and re-read the required project docs.
- Updated Issue #41 from `PATCH` to `PUT` so the issue scope matches the implementation pattern used by property updates.
- Reviewed the Unit module requirements and database design before changing code.
- Updated `openapi.yml` and `docs/API_SPEC.md` before controller work to change the unit update operation to `PUT` and document full-field replacement behavior.
- Added `UnitUpdateRequest`, unit entity update behavior, repository duplicate-check support, and the service workflow for updating unit fields under a write lock.
- Implemented the authenticated `PUT /api/v1/units/{unit_id}` controller endpoint with validation, not-found handling, and duplicate unit-number conflict handling.
- Added service, controller, and PostgreSQL integration tests for successful updates, validation failures, not-found behavior, and duplicate-number conflicts.
- Stayed within the Unit module scope and did not implement unrelated future unit behaviors.

## Test Results

```text
./gradlew test --tests com.propertybilling.service.UnitServiceTest --tests com.propertybilling.controller.UnitControllerTest --tests com.propertybilling.integration.unit.UnitUpdateIntegrationTest
./gradlew clean test
Passed
```

## Pull Request

- Issue: https://github.com/Rasyid619/property-billing-api/issues/41

## Blockers

- None.

## Tomorrow

- Open the Issue #41 pull request and wait for CI before moving to the next endpoint issue.
