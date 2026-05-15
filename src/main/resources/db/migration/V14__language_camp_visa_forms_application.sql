-- Link visa forms directly to applications; drop participants table.

ALTER TABLE language_camp_visa_forms
    ADD COLUMN application_id uuid;

UPDATE language_camp_visa_forms vf
SET application_id = p.application_id
FROM language_camp_participants p
WHERE vf.participant_id = p.id;

ALTER TABLE language_camp_visa_forms
    ALTER COLUMN application_id SET NOT NULL;

ALTER TABLE language_camp_visa_forms
    ADD CONSTRAINT fk_language_camp_visa_forms_application_id
        FOREIGN KEY (application_id) REFERENCES language_camp_applications(id);

DROP INDEX IF EXISTS ux_language_camp_visa_forms_participant_id;

ALTER TABLE language_camp_visa_forms
    DROP COLUMN participant_id;

CREATE UNIQUE INDEX IF NOT EXISTS ux_language_camp_visa_forms_application_id
    ON language_camp_visa_forms(application_id);

CREATE INDEX IF NOT EXISTS ix_language_camp_visa_forms_application_id
    ON language_camp_visa_forms(application_id);

DROP TABLE IF EXISTS language_camp_participants;
