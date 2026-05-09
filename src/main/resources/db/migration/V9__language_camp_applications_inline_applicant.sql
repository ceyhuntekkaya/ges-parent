-- Inline applicant basic info on language_camp_applications and drop applicant_profile_id relation
ALTER TABLE language_camp_applications
    ADD COLUMN IF NOT EXISTS first_name varchar(64),
    ADD COLUMN IF NOT EXISTS last_name varchar(64),
    ADD COLUMN IF NOT EXISTS birth_date date,
    ADD COLUMN IF NOT EXISTS phone varchar(32),
    ADD COLUMN IF NOT EXISTS is_it_self boolean,
    ADD COLUMN IF NOT EXISTS number_of_applicant integer;

-- Best-effort backfill from applicant_profiles (if existing rows still reference it)
UPDATE language_camp_applications a
SET
    first_name = COALESCE(a.first_name, ap.first_name),
    last_name = COALESCE(a.last_name, ap.last_name),
    birth_date = COALESCE(a.birth_date, ap.birth_date),
    phone = COALESCE(a.phone, ap.phone)
FROM applicant_profiles ap
WHERE a.applicant_profile_id = ap.id;

ALTER TABLE language_camp_applications
    DROP CONSTRAINT IF EXISTS language_camp_applications_applicant_profile_id_fkey;

DROP INDEX IF EXISTS ix_language_camp_applications_applicant_profile_id;

ALTER TABLE language_camp_applications
    DROP COLUMN IF EXISTS applicant_profile_id;

