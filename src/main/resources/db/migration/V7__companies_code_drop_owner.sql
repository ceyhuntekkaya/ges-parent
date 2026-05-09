-- Add company code and remove owner_user_id ownership.
-- code is a stable external identifier for linking/selecting companies.

ALTER TABLE companies
    ADD COLUMN IF NOT EXISTS code varchar(64);

-- Backfill for existing rows (deterministic, short-ish, unique).
UPDATE companies
SET code = COALESCE(code, substring(replace(id::text, '-', '') for 12))
WHERE code IS NULL OR code = '';

ALTER TABLE companies
    ALTER COLUMN code SET NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS ux_companies_code ON companies(code);

-- Drop ownership column + index (portal ownership will be handled via code).
DROP INDEX IF EXISTS ix_companies_owner_user_id;

ALTER TABLE companies
    DROP COLUMN IF EXISTS owner_user_id;

