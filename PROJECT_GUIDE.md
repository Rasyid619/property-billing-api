# Property Billing API - Project Guide

## Project Overview

Property Billing API is a backend REST API for tracking monthly housing or apartment fees.

The application helps property owners or administrators manage:

- Properties
- Units
- Tenants
- Monthly invoices
- Tenant payments
- Property expenses
- Monthly cash flow
- Monthly savings or cash balance

The goal of this project is to demonstrate real backend engineering skills using Spring Boot, PostgreSQL, Docker, Gradle, Flyway, OpenAPI, GitHub Issues, Pull Requests, CI, and clean REST API design.

## Main Business Problem

Property owners often need to track:

- Which tenants have paid monthly fees
- Which tenants have not paid
- Which units are overdue
- How much income was collected
- How much money was spent for property expenses
- How much cash remains at the end of the month

This system solves that problem by managing invoices, payments, expenses, and cash-flow reporting.

## Main Users

### Admin

The admin manages properties, units, tenants, invoices, payments, and reports.

### Property Staff

Property staff can help record payments and expenses.

### Tenant

Tenant access can be added later.

For MVP, tenant login is not included.

## Main Business Flow

1. Admin creates a property.
2. Admin creates units inside the property.
3. Admin creates tenant data.
4. Admin assigns a tenant to a unit.
5. Every month, admin generates invoices for active units.
6. Tenant makes payment.
7. Admin records payment.
8. System updates invoice status.
9. Admin records property expenses.
10. System calculates monthly cash flow.

## Main Entities

### User

Represents admin or staff who manages the system.

For MVP, only admin or staff users can login.

### Property

Represents an apartment, housing area, building, or property group.

Example:

```text
Green Residence
```

### Unit

Represents one house, apartment unit, room, or rented space.

Each unit has:

- Property
- Unit number
- Monthly fee
- Due day
- Active status

Example:

```text
Unit A-101
Monthly fee: Rp750.000
Due day: 10
```

### Tenant

Represents a person who lives in or rents a unit.

A tenant is data only in MVP.

A tenant does not login in MVP.

### UnitTenant

Represents tenant assignment to a unit.

This table is needed because tenants can move in and move out.

Example:

```text
January 2026:
Unit A-101 -> Budi

July 2026:
Budi moves out
Unit A-101 -> Andi
```

### Invoice

Represents a monthly bill for a tenant and unit.

Each invoice has:

- Unit
- Tenant
- Billing month
- Invoice number
- Amount
- Due date
- Status

### Payment

Represents payment made for an invoice.

A single invoice can have multiple payments.

Overpayment beyond the selected invoice amount becomes tenant/unit credit.
Credit can settle future invoices for the same tenant and unit, but applied credit
is not new cash income because the money was already received.

### PropertyExpense

Represents money spent from collected property fees.

Example categories:

- Electricity
- Water
- Security salary
- Cleaning
- Repair
- Maintenance
- Other

### CashBalance

Represents monthly closing balance.

This is useful when the system supports monthly closing.

## Unit and Tenant Design Decision

Unit and Tenant must remain separate.

Recommended model:

```text
Property = building or housing area
Unit = room, house, apartment number, or rented space
Tenant = person who lives in or rents the unit
UnitTenant = assignment history between unit and tenant
```

Reasons:

- A unit can exist without a tenant.
- A tenant can move out.
- Another tenant can move into the same unit.
- Assignment history must be preserved.
- Invoices should be linked to the tenant and unit at the time of billing.

Do not merge Unit and Tenant into one table.

## Tenant Login Decision

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

## Invoice Status

Invoice status can be:

- `unpaid`
- `partial`
- `paid`
- `overdue`
- `cancelled`

## Invoice Generation Rule

Monthly invoices are generated per active unit.

Rules:

1. Only active units can receive invoices.
2. Unit must have an active tenant.
3. One unit can only have one invoice per billing month.
4. Invoice amount uses the unit monthly fee.
5. Due date is calculated from billing month and unit due day.
6. Billing month must be stored as the first day of the month.

Example:

```text
Billing month: 2026-05-01
Due day: 10
Due date: 2026-05-10
```

## Automated Invoice Generation Rule

The system can run an internal scheduled job that generates invoices for the
next billing month before the tenant payment window starts.

Rules:

1. The scheduled job is disabled by default and must be enabled through configuration.
2. The schedule is configurable through application configuration.
3. The scheduler generates invoices for active properties only.
4. The scheduler uses the same active-unit and active-tenant eligibility rules as manual generation.
5. Duplicate property-month generation is skipped and must not create duplicate invoice rows.
6. The scheduler calls application service logic directly and does not require JWT authentication.
7. No unauthenticated public invoice generation endpoint should be exposed.

Example:

```text
Current date: 2026-05-25
Generated billing month: 2026-06-01
Due day: 5
Due date: 2026-06-05
```

## Payment Rule

After payment is created, recalculate invoice status.

Rules:

- If total paid is `0`, status is `unpaid`.
- If total settlement is more than `0` and less than invoice amount, status is `partial`.
- If total settlement is greater than or equal to invoice amount, status is `paid`.
- If due date is past and total settlement is less than invoice amount, status is `overdue`.

Total settlement is cash payments plus applied tenant/unit credit.

Important:

A paid invoice must not become overdue.

## Expense Rule

Expenses are recorded per property.

Each expense must have:

- Property
- Expense date
- Category
- Amount
- Optional description

Expense amount must be greater than zero.

## Cash Flow Rule

Cash flow is calculated from payments and expenses.

Formula:

```text
net_saving = total_income - total_expense
```

Where:

```text
total_income = sum of payments
total_expense = sum of property expenses
```

For MVP, cash flow can be calculated dynamically.

Later, monthly closing can be added with this formula:

```text
closing_balance = opening_balance + total_income - total_expense
```

## MVP Scope

The MVP should include:

- Admin/staff auth
- Property CRUD
- Unit CRUD
- Tenant CRUD
- Tenant assignment
- Monthly invoice generation
- Invoice listing and filtering
- Payment recording
- Invoice status recalculation
- Property expense tracking
- Monthly cash-flow report
- OpenAPI documentation
- Tests
- Docker Compose
- GitHub Actions CI
- GitHub Issue and Pull Request workflow

## Engineering Workflow Scope

This project should also demonstrate professional workflow:

- GitHub Issues for every task
- Branch naming convention
- Pull requests
- Conventional commits
- CI checks
- Tests before moving modules
- API contract before implementation

## Out of Scope for MVP

These features can be added later:

- Tenant login
- Online payment gateway
- WhatsApp notification
- Email notification
- Receipt image upload
- Multi-currency support
- Advanced accounting
- Mobile app
- Frontend dashboard
