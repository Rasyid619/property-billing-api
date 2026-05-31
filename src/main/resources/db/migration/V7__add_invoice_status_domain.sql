CREATE DOMAIN invoice_status AS TEXT
    CHECK (VALUE IN (
        'unpaid',
        'partial',
        'paid',
        'overdue',
        'cancelled'
    ));

ALTER TABLE invoices
    ALTER COLUMN status TYPE invoice_status USING status::invoice_status,
    ALTER COLUMN status SET DEFAULT 'unpaid'::invoice_status;

CREATE DOMAIN payment_method AS TEXT
    CHECK (VALUE IN (
        'bank_transfer',
        'cash',
        'e_wallet',
        'other'
    ));

ALTER TABLE payments
    ALTER COLUMN payment_method TYPE payment_method USING payment_method::payment_method;
