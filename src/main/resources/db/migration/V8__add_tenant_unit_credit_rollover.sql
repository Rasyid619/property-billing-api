CREATE TABLE tenant_unit_credits (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    unit_id UUID NOT NULL REFERENCES units(id),
    balance TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT tenant_unit_credits_tenant_id_unit_id_key UNIQUE (tenant_id, unit_id)
);

CREATE TABLE credit_applications (
    id UUID PRIMARY KEY,
    tenant_unit_credit_id UUID NOT NULL REFERENCES tenant_unit_credits(id),
    invoice_id UUID NOT NULL REFERENCES invoices(id),
    amount TEXT NOT NULL,
    applied_date DATE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TRIGGER tenant_unit_credits_set_updated_at
BEFORE UPDATE ON tenant_unit_credits
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER credit_applications_set_updated_at
BEFORE UPDATE ON credit_applications
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();
