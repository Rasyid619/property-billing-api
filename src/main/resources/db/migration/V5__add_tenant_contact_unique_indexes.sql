CREATE UNIQUE INDEX tenants_phone_unique
ON tenants (phone)
WHERE phone IS NOT NULL;

CREATE UNIQUE INDEX tenants_email_unique
ON tenants (email)
WHERE email IS NOT NULL;
