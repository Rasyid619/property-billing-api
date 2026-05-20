# Daily Schedule

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
Passed
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

## Tests

Command:

```bash
./gradlew test --tests com.propertybilling.service.PropertyServiceTest --tests com.propertybilling.controller.PropertyControllerTest --tests com.propertybilling.integration.PropertyIndexIntegrationTest --tests com.propertybilling.service.AuthServiceTest
./gradlew clean test
```

Result:

```text
Passed
```

## Pull Request

- PR:
- Issue: https://github.com/Rasyid619/property-billing-api/issues/27

## Blockers

- No blockers.

## Tomorrow

- Push branch, open a pull request for Issue #27, and wait for CI before moving to the next endpoint issue.
