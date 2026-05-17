CREATE TABLE users (
    id UUID PRIMARY KEY,
    name TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    role TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE properties (
    id UUID PRIMARY KEY,
    name TEXT NOT NULL,
    address TEXT,
    is_active BOOLEAN NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE units (
    id UUID PRIMARY KEY,
    property_id UUID NOT NULL REFERENCES properties(id),
    unit_number TEXT NOT NULL,
    monthly_fee TEXT NOT NULL,
    due_day INTEGER NOT NULL,
    is_active BOOLEAN NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT units_property_id_unit_number_key UNIQUE (property_id, unit_number)
);

CREATE TABLE tenants (
    id UUID PRIMARY KEY,
    name TEXT NOT NULL,
    phone TEXT,
    email TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE unit_tenants (
    id UUID PRIMARY KEY,
    unit_id UUID NOT NULL REFERENCES units(id),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    start_date DATE NOT NULL,
    end_date DATE,
    is_active BOOLEAN NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE invoices (
    id UUID PRIMARY KEY,
    unit_id UUID NOT NULL REFERENCES units(id),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    billing_month DATE NOT NULL,
    invoice_number TEXT NOT NULL UNIQUE,
    amount TEXT NOT NULL,
    due_date DATE NOT NULL,
    status TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT invoices_unit_id_billing_month_key UNIQUE (unit_id, billing_month)
);

CREATE TABLE payments (
    id UUID PRIMARY KEY,
    invoice_id UUID NOT NULL REFERENCES invoices(id),
    amount TEXT NOT NULL,
    payment_date DATE NOT NULL,
    payment_method TEXT NOT NULL,
    reference_number TEXT,
    note TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE property_expenses (
    id UUID PRIMARY KEY,
    property_id UUID NOT NULL REFERENCES properties(id),
    expense_date DATE NOT NULL,
    category TEXT NOT NULL,
    amount TEXT NOT NULL,
    description TEXT,
    receipt_url TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE cash_balances (
    id UUID PRIMARY KEY,
    property_id UUID NOT NULL REFERENCES properties(id),
    month DATE NOT NULL,
    opening_balance TEXT NOT NULL,
    total_income TEXT NOT NULL,
    total_expense TEXT NOT NULL,
    closing_balance TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT cash_balances_property_id_month_key UNIQUE (property_id, month)
);
