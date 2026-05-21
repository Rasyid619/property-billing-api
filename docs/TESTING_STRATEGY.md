# Testing Strategy

## Purpose

This document defines the testing strategy for Property Billing API.

Every module must include tests before moving to the next module.

## Required Test Command

Before completing a module, run:

```bash
./gradlew clean test
```

For larger changes, also run:

```bash
./gradlew clean build
```

## Testing Goals

Tests should prove that:

- Business rules work correctly
- Validation works correctly
- Database constraints work correctly
- API endpoints behave as expected
- Error scenarios are handled properly
- Existing features do not break

## Test Types

## Unit Tests

Unit tests are used for service-level business logic.

Examples:

- Invoice status calculation
- Payment total calculation
- Cash-flow calculation
- Due date generation
- Tenant assignment rule

Unit tests should be fast and isolated.

Use:

- JUnit
- Mockito
- AssertJ if available

Unit tests should assert the returned result or state change and verify important
collaborator calls. When using Mockito, verify expected calls with call counts
such as `times(1)`, and verify blocked flows with `never()` or
`verifyNoInteractions()` when the dependency must not be called.

## Integration Tests

Integration tests are used when behavior depends on:

- Database
- JPA repository
- Flyway migration
- API endpoint behavior
- Validation
- Security

Use Testcontainers when possible.

## Controller/API Tests

Controller tests should verify:

- Correct HTTP status
- Correct request validation
- Correct response format
- Correct error format
- Authentication behavior
- Service calls happen once on successful requests
- Service calls do not happen when authentication or validation rejects the request

## Migration Tests

Migration tests should verify:

- Application starts with clean database
- Flyway migration succeeds
- Important constraints exist
- Database schema supports required use cases

## Module Testing Checklist

Each module is complete only when:

- Unit tests are added for business logic
- Integration tests are added when needed
- Validation errors are tested
- Not found errors are tested
- Conflict errors are tested when relevant
- Existing tests pass
- GitHub Actions CI passes

## Auth Module Tests

Required tests:

- Seed initial admin successfully
- Login with valid credentials
- Reject login with invalid credentials
- Protected endpoint rejects missing token
- Protected endpoint rejects invalid token
- Protected endpoint accepts valid token
- Tenant login endpoint does not exist in MVP

## Property Module Tests

Required tests:

- Create property successfully
- Reject blank property name
- List properties
- Get property by ID
- Update property
- Handle property not found
- Deactivate property if implemented

## Unit Module Tests

Required tests:

- Create unit successfully
- Reject duplicate unit number in same property
- Allow same unit number in different property
- Reject invalid monthly fee
- Reject invalid due day
- Handle property not found
- List units by property

## Tenant Module Tests

Required tests:

- Create tenant successfully
- Reject blank tenant name
- List tenants
- Get tenant by ID
- Update tenant
- Handle tenant not found
- Tenant does not require login account in MVP

## Tenant Assignment Module Tests

Required tests:

- Assign tenant to empty unit
- Prevent two active tenants in the same unit
- Move tenant out
- Get active tenant by unit
- List tenant assignment history
- Handle unit not found
- Handle tenant not found

## Invoice Module Tests

Required tests:

- Generate invoice successfully
- Skip unit without active tenant
- Prevent duplicate invoice for same unit and month
- Generate due date correctly
- Use unit monthly fee correctly
- Store unit and tenant reference on invoice
- List invoices by month
- List invoices by status
- Get invoice detail
- Validate billing month must be first day of month

## Payment Module Tests

Required tests:

- Record full payment and mark invoice as paid
- Record partial payment and mark invoice as partial
- Record multiple payments and update status correctly
- Reject zero payment
- Reject negative payment
- Handle invoice not found
- Mark unpaid invoice as overdue when due date is past
- Do not mark paid invoice as overdue

## Property Expense Module Tests

Required tests:

- Create expense successfully
- Reject zero expense amount
- Reject negative expense amount
- Reject missing category
- List expenses by month
- Handle property not found
- Update expense
- Delete expense if implemented

## Cash Flow Report Tests

Required tests:

- Calculate total income correctly
- Calculate total expense correctly
- Calculate net saving correctly
- Return zero when no income exists
- Return zero when no expenses exist
- Avoid duplicated sums when multiple payments and multiple expenses exist
- Handle property not found

## Cash Balance Closing Tests

Required tests:

- Close month successfully
- Prevent duplicate closing for same property and month
- Carry previous month closing balance as next month opening balance
- Calculate closing balance correctly
- Handle missing previous month balance

## CI Rule

GitHub Actions must run tests on:

- Push to main
- Pull request to main

The project should not move to the next module when CI is failing.

## Test Naming Convention

Use clear test names.

Example:

```text
shouldCreatePropertySuccessfully
shouldRejectDuplicateUnitNumberInSameProperty
shouldMarkInvoiceAsPaidWhenPaymentIsComplete
shouldCalculateCashFlowWithoutDuplicatingExpense
```

## Testing Priority

Priority order:

1. Business logic tests
2. Validation tests
3. Error scenario tests
4. Repository/database tests
5. API integration tests
6. Security tests

## Rule

No module is considered complete without tests.
