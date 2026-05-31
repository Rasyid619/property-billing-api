# Database Design

## Overview

This document describes the database design for Property Billing API.

The database stores:

- Users
- Properties
- Units
- Tenants
- Unit tenant assignments
- Invoices
- Payments
- Property expenses
- Cash balances

## General Rules

- Use UUID primary keys.
- Use `TEXT` for money columns.
- Store money as a decimal string, for example `750000.00`.
- Parse money values to `BigDecimal` in Java before validation or calculation.
- Use `DATE` for billing dates.
- Use `TIMESTAMP WITH TIME ZONE` for created and updated timestamps.
- Set `created_at` and `updated_at` defaults to `CURRENT_TIMESTAMP` on insert.
- Keep `updated_at` synchronized automatically with database triggers whenever a row changes.
- Use the first day of the month for monthly fields.
- Use database constraints to protect important business rules.

## Design Decision: Unit and Tenant Separation

Unit and Tenant must be separate tables.

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
- Invoices should be linked to both unit and tenant at the time of billing.

Do not merge Unit and Tenant into one table.

## Tenant Login Decision

Tenant login is not part of MVP.

For MVP:

- Only admin and staff users can login.
- Tenants are managed as data records.
- Tenants do not have application accounts yet.

Future tenant login can be added using one of these options:

1. Email or phone login linked to tenant profile.
2. Invitation-based tenant account creation.
3. Property code, unit number, and secure access code.

Do not use property name and unit name alone as login credentials because they are easy to guess.

## Tables

## users

Stores admin or staff accounts.

| Column | Type | Notes |
|---|---|---|
| id | UUID | Primary key |
| name | TEXT | Required |
| email | TEXT | Required, unique |
| password_hash | TEXT | Required |
| role | TEXT | Required |
| created_at | TIMESTAMP | Required |
| updated_at | TIMESTAMP | Required |

Business rules:

- Email must be unique.
- Password must be stored as hash, never plain text.
- MVP users are admin or staff only.
- Tenant login is not part of MVP.

## properties

Stores property or building data.

| Column | Type | Notes |
|---|---|---|
| id | UUID | Primary key |
| name | TEXT | Required |
| address | TEXT | Optional |
| is_active | BOOLEAN | Required |
| created_at | TIMESTAMP | Required |
| updated_at | TIMESTAMP | Required |

Business rules:

- A property can have many units.
- Inactive property should not be used for new invoice generation.

## units

Stores housing or apartment units.

| Column | Type | Notes |
|---|---|---|
| id | UUID | Primary key |
| property_id | UUID | Foreign key to properties |
| unit_number | TEXT | Required |
| monthly_fee | TEXT | Required, decimal string |
| due_day | INT | Required |
| is_active | BOOLEAN | Required |
| created_at | TIMESTAMP | Required |
| updated_at | TIMESTAMP | Required |

Constraints:

```text
UNIQUE(property_id, unit_number)
```

Business rules:

- Unit number must be unique inside one property.
- Monthly fee must be a valid decimal string greater than zero.
- Due day must be valid.
- Suggested due day range: 1 to 28 for simpler monthly date handling.
- Unit is not the same as Tenant.

## tenants

Stores tenant or resident data.

| Column | Type | Notes |
|---|---|---|
| id | UUID | Primary key |
| name | TEXT | Required |
| phone | TEXT | Optional |
| email | TEXT | Optional |
| created_at | TIMESTAMP | Required |
| updated_at | TIMESTAMP | Required |

Constraints:

```text
UNIQUE(phone) WHERE phone IS NOT NULL
UNIQUE(email) WHERE email IS NOT NULL
```

Business rules:

- Tenant can be assigned to a unit.
- Tenant data should remain even after move out.
- Tenant does not login in MVP.
- Tenant is not the same as Unit.
- Tenant phone must be unique when provided.
- Tenant email must be unique when provided.

## unit_tenants

Stores tenant assignment history.

| Column | Type | Notes |
|---|---|---|
| id | UUID | Primary key |
| unit_id | UUID | Foreign key to units |
| tenant_id | UUID | Foreign key to tenants |
| start_date | DATE | Required |
| end_date | DATE | Optional |
| is_active | BOOLEAN | Required |
| created_at | TIMESTAMP | Required |
| updated_at | TIMESTAMP | Required |

Business rules:

- One unit should only have one active tenant assignment.
- Active assignment has `end_date = null`.
- Assignment history must be preserved.
- Moving out should close the active assignment.

## invoices

Stores monthly invoices.

| Column | Type | Notes |
|---|---|---|
| id | UUID | Primary key |
| unit_id | UUID | Foreign key to units |
| tenant_id | UUID | Foreign key to tenants |
| billing_month | DATE | Required |
| invoice_number | TEXT | Required, unique |
| amount | TEXT | Required, decimal string |
| due_date | DATE | Required |
| status | invoice_status | Required, defaults to `unpaid` |
| created_at | TIMESTAMP | Required |
| updated_at | TIMESTAMP | Required |

Constraints:

```text
UNIQUE(unit_id, billing_month)
UNIQUE(invoice_number)
```

Business rules:

- Billing month must be first day of month.
- One unit can only have one invoice per billing month.
- Invoice amount is copied from unit monthly fee during generation.
- Invoice amount must be a valid decimal string greater than zero.
- Invoice stores both unit and tenant references at billing time.
- Invoice status can be:
  - unpaid
  - partial
  - paid
  - overdue
  - cancelled

## payments

Stores invoice payments.

| Column | Type | Notes |
|---|---|---|
| id | UUID | Primary key |
| invoice_id | UUID | Foreign key to invoices |
| amount | TEXT | Required, decimal string |
| payment_date | DATE | Required |
| payment_method | payment_method | Required |
| reference_number | TEXT | Optional |
| note | TEXT | Optional |
| created_at | TIMESTAMP | Required |
| updated_at | TIMESTAMP | Required |

Business rules:

- Payment amount must be a valid decimal string greater than zero.
- One invoice can have multiple payments.
- Invoice status must be recalculated after payment is created.

## property_expenses

Stores expenses for a property.

| Column | Type | Notes |
|---|---|---|
| id | UUID | Primary key |
| property_id | UUID | Foreign key to properties |
| expense_date | DATE | Required |
| category | TEXT | Required |
| amount | TEXT | Required, decimal string |
| description | TEXT | Optional |
| receipt_url | TEXT | Optional |
| created_at | TIMESTAMP | Required |
| updated_at | TIMESTAMP | Required |

Business rules:

- Expense amount must be a valid decimal string greater than zero.
- Expense must belong to a property.
- Expenses are used for cash-flow report.

## cash_balances

Stores monthly closing balance.

| Column | Type | Notes |
|---|---|---|
| id | UUID | Primary key |
| property_id | UUID | Foreign key to properties |
| month | DATE | Required |
| opening_balance | TEXT | Required, decimal string |
| total_income | TEXT | Required, decimal string |
| total_expense | TEXT | Required, decimal string |
| closing_balance | TEXT | Required, decimal string |
| created_at | TIMESTAMP | Required |
| updated_at | TIMESTAMP | Required |

Constraints:

```text
UNIQUE(property_id, month)
```

Business rules:

- Month must be first day of month.
- Closing balance formula:

```text
closing_balance = opening_balance + total_income - total_expense
```

## Relationships

```text
properties 1--many units
units 1--many unit_tenants
tenants 1--many unit_tenants
units 1--many invoices
tenants 1--many invoices
invoices 1--many payments
properties 1--many property_expenses
properties 1--many cash_balances
```

## Important Query Rules

### Cash Flow Report

Do not directly join payments and expenses because it can duplicate sums.

Parse money strings to decimal values before calculating totals.

Use separate aggregates:

```text
total_income = sum(parse_decimal(payments.amount))
total_expense = sum(parse_decimal(property_expenses.amount))
net_saving = total_income - total_expense
```

### Invoice Status

Invoice status should be calculated from:

```text
parsed invoice amount
parsed total paid amount
due date
current date
```

Rules:

```text
total_paid = 0 → unpaid
total_paid > 0 and total_paid < amount → partial
total_paid >= amount → paid
due_date < today and total_paid < amount → overdue
```

A paid invoice must not become overdue.
