-- Add guardian/parent info + user notes to language camp applications
-- Also drop unused start_date/end_date columns (kept at program/participant level)
ALTER TABLE language_camp_applications
    ADD COLUMN IF NOT EXISTS is_under_18 boolean,
    ADD COLUMN IF NOT EXISTS parent_full_name varchar(128),
    ADD COLUMN IF NOT EXISTS parent_phone_number varchar(32),
    ADD COLUMN IF NOT EXISTS parent_email_address varchar(256),
    ADD COLUMN IF NOT EXISTS parent_relationship varchar(64),
    ADD COLUMN IF NOT EXISTS user_notes text;

ALTER TABLE language_camp_applications
    DROP COLUMN IF EXISTS start_date,
    DROP COLUMN IF EXISTS end_date;

