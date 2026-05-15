-- Replace single-file columns with a many-documents join table.

CREATE TABLE IF NOT EXISTS language_camp_visa_form_documents (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),

    visa_form_id uuid NOT NULL REFERENCES language_camp_visa_forms(id) ON DELETE CASCADE,
    stored_file_id uuid NOT NULL REFERENCES stored_files(id)
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_language_camp_visa_form_documents_visa_form_stored_file
    ON language_camp_visa_form_documents(visa_form_id, stored_file_id);

CREATE INDEX IF NOT EXISTS ix_language_camp_visa_form_documents_visa_form_id
    ON language_camp_visa_form_documents(visa_form_id);

DROP INDEX IF EXISTS ux_language_camp_visa_forms_bank_statement_file_id;
DROP INDEX IF EXISTS ux_language_camp_visa_forms_biometric_photo_file_id;

ALTER TABLE language_camp_visa_forms
    DROP COLUMN IF EXISTS bank_statement_file_id,
    DROP COLUMN IF EXISTS biometric_photo_file_id;

-- Backfill empty visa forms for applications created before auto-create.
INSERT INTO language_camp_visa_forms (id, created_at, updated_at, application_id)
SELECT gen_random_uuid(), now(), now(), a.id
FROM language_camp_applications a
WHERE NOT EXISTS (
    SELECT 1 FROM language_camp_visa_forms vf WHERE vf.application_id = a.id
);
