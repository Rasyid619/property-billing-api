INSERT INTO users (
    id,
    name,
    email,
    password_hash,
    role,
    created_at,
    updated_at
)
VALUES (
    '${initial_admin_id}',
    '${initial_admin_name}',
    '${initial_admin_email}',
    '${initial_admin_password_hash}',
    '${initial_admin_role}',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (email) DO NOTHING;
