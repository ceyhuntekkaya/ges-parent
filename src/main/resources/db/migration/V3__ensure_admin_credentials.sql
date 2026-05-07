-- Ensure the initial admin user exists and has the expected credentials.
-- This is intentionally an UPSERT because earlier installs may have inserted the user with a different hash.
--
-- Flyway placeholders:
--  - ${admin_email}
--  - ${admin_password_hash}

INSERT INTO user_accounts (id, created_at, updated_at, email, password_hash, role, status, last_login_at)
VALUES (gen_random_uuid(), now(), now(), '${admin_email}', '${admin_password_hash}', 'ADMIN', 'ACTIVE', NULL)
ON CONFLICT (email) DO UPDATE SET
  password_hash = EXCLUDED.password_hash,
  role = EXCLUDED.role,
  status = EXCLUDED.status,
  updated_at = now();

