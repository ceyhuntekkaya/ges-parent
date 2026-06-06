-- CRM fields and child tables for language camp applications (notes, meetings, tasks, documents)

ALTER TABLE language_camp_applications
    ADD COLUMN IF NOT EXISTS follower_person varchar(128),
    ADD COLUMN IF NOT EXISTS notes text;

CREATE TABLE IF NOT EXISTS language_camp_application_notes (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),

    application_id uuid NOT NULL REFERENCES language_camp_applications(id),
    written_by varchar(128) NOT NULL,
    written_at timestamptz NOT NULL,
    todo_text text NOT NULL
);

CREATE INDEX IF NOT EXISTS ix_lcan_application_id
    ON language_camp_application_notes(application_id);

CREATE TABLE IF NOT EXISTS language_camp_application_meetings (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),

    application_id uuid NOT NULL REFERENCES language_camp_applications(id),
    person varchar(128) NOT NULL,
    meeting_at timestamptz NOT NULL,
    meeting_note text,
    meeting_result text
);

CREATE INDEX IF NOT EXISTS ix_lcam_application_id
    ON language_camp_application_meetings(application_id);

CREATE TABLE IF NOT EXISTS language_camp_application_tasks (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),

    application_id uuid NOT NULL REFERENCES language_camp_applications(id),
    scheduled_at timestamptz NOT NULL,
    with_whom varchar(256) NOT NULL,
    what_to_do text NOT NULL,
    status varchar(16) NOT NULL,
    performed_by_user varchar(128)
);

CREATE INDEX IF NOT EXISTS ix_lcat_application_id
    ON language_camp_application_tasks(application_id);

CREATE TABLE IF NOT EXISTS language_camp_application_documents (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),

    application_id uuid NOT NULL REFERENCES language_camp_applications(id),
    required boolean NOT NULL DEFAULT false,
    document_name varchar(128) NOT NULL,
    document_description text,
    document_url varchar(1024),
    uploaded_at timestamptz
);

CREATE INDEX IF NOT EXISTS ix_lcad_application_id
    ON language_camp_application_documents(application_id);
