# Daily Schedule

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
