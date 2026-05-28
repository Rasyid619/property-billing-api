CREATE UNIQUE INDEX unit_tenants_active_unit_id_key
ON unit_tenants (unit_id)
WHERE is_active = TRUE;
