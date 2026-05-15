-- Replace program_id with required language_camp_project_id on language_camp_applications
ALTER TABLE language_camp_applications
    ADD COLUMN IF NOT EXISTS language_camp_project_id uuid REFERENCES language_camp_projects(id);

-- Existing rows cannot be mapped from programs to language_camp_projects; remove orphans before NOT NULL.
DELETE FROM language_camp_applications
WHERE language_camp_project_id IS NULL;

DROP INDEX IF EXISTS ix_language_camp_applications_program_id;

ALTER TABLE language_camp_applications
    DROP COLUMN IF EXISTS program_id;

ALTER TABLE language_camp_applications
    ALTER COLUMN language_camp_project_id SET NOT NULL;

CREATE INDEX IF NOT EXISTS ix_language_camp_applications_language_camp_project_id
    ON language_camp_applications(language_camp_project_id);
