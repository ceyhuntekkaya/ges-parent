-- Introduce companies table; migrate corporate fields from language_camp_applications.

CREATE TABLE IF NOT EXISTS companies (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),

    owner_user_id uuid NOT NULL REFERENCES user_accounts(id),

    name varchar(255) NOT NULL,
    tax_number varchar(64),
    contact_full_name varchar(128),
    contact_phone varchar(32),
    contact_email varchar(255)
);

CREATE INDEX IF NOT EXISTS ix_companies_owner_user_id ON companies(owner_user_id);
CREATE INDEX IF NOT EXISTS ix_companies_name ON companies(name);

ALTER TABLE language_camp_applications
    ADD COLUMN IF NOT EXISTS company_id uuid;

ALTER TABLE language_camp_applications
    ADD CONSTRAINT fk_language_camp_applications_company_id
    FOREIGN KEY (company_id) REFERENCES companies(id);

CREATE INDEX IF NOT EXISTS ix_language_camp_applications_company_id ON language_camp_applications(company_id);

-- Backfill: create a company row per application with any corporate fields.
-- Owner is the applicant_user_id to keep portal ownership checks consistent.
INSERT INTO companies (id, created_at, updated_at, owner_user_id, name, tax_number, contact_full_name, contact_phone, contact_email)
SELECT
    gen_random_uuid(),
    now(),
    now(),
    a.applicant_user_id,
    COALESCE(NULLIF(a.company_name, ''), 'Company'),
    a.tax_number,
    a.company_contact_full_name,
    a.company_contact_phone,
    a.company_contact_email
FROM language_camp_applications a
WHERE a.company_id IS NULL
  AND (
      a.company_name IS NOT NULL
      OR a.tax_number IS NOT NULL
      OR a.company_contact_full_name IS NOT NULL
      OR a.company_contact_phone IS NOT NULL
      OR a.company_contact_email IS NOT NULL
  );

-- Set language_camp_applications.company_id for those newly inserted rows.
-- Uses a best-effort match on owner+all fields to link 1:1 per application.
UPDATE language_camp_applications a
SET company_id = c.id
FROM companies c
WHERE a.company_id IS NULL
  AND c.owner_user_id = a.applicant_user_id
  AND c.tax_number IS NOT DISTINCT FROM a.tax_number
  AND c.contact_full_name IS NOT DISTINCT FROM a.company_contact_full_name
  AND c.contact_phone IS NOT DISTINCT FROM a.company_contact_phone
  AND c.contact_email IS NOT DISTINCT FROM a.company_contact_email
  AND c.name = COALESCE(NULLIF(a.company_name, ''), 'Company');

-- Drop old columns (now represented by company_id).
ALTER TABLE language_camp_applications
    DROP COLUMN IF EXISTS company_name,
    DROP COLUMN IF EXISTS tax_number,
    DROP COLUMN IF EXISTS company_contact_full_name,
    DROP COLUMN IF EXISTS company_contact_phone,
    DROP COLUMN IF EXISTS company_contact_email;

