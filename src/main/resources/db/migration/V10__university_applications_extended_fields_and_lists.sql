-- Extend university_applications with applicant snapshot fields and add list tables (notes, meetings, tasks, documents, portfolio)

ALTER TABLE university_applications
    ADD COLUMN IF NOT EXISTS first_name varchar(64),
    ADD COLUMN IF NOT EXISTS last_name varchar(64),
    ADD COLUMN IF NOT EXISTS birth_date date,
    ADD COLUMN IF NOT EXISTS phone varchar(32),
    ADD COLUMN IF NOT EXISTS nationality varchar(128),
    ADD COLUMN IF NOT EXISTS address text,
    ADD COLUMN IF NOT EXISTS current_school varchar(128),
    ADD COLUMN IF NOT EXISTS student boolean,
    ADD COLUMN IF NOT EXISTS class_level varchar(64),
    ADD COLUMN IF NOT EXISTS reference_person varchar(128),
    ADD COLUMN IF NOT EXISTS consultancy boolean,
    ADD COLUMN IF NOT EXISTS follower_person varchar(128);

-- Best-effort backfill from applicant_profiles
UPDATE university_applications ua
SET
    first_name = COALESCE(ua.first_name, ap.first_name),
    last_name = COALESCE(ua.last_name, ap.last_name),
    birth_date = COALESCE(ua.birth_date, ap.birth_date),
    phone = COALESCE(ua.phone, ap.phone),
    nationality = COALESCE(ua.nationality, ap.nationality),
    address = COALESCE(
        ua.address,
        NULLIF(
            concat_ws(
                ', ',
                ap.addr_line1,
                ap.addr_line2,
                ap.addr_district,
                ap.addr_city,
                ap.addr_postal_code,
                ap.addr_country
            ),
            ''
        )
    )
FROM applicant_profiles ap
WHERE ua.applicant_profile_id = ap.id;

-- =========================
-- Portfolio section catalog
-- =========================
CREATE TABLE IF NOT EXISTS portfolio_sections (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    name varchar(128) NOT NULL,
    description text
);

-- =========================================
-- Per-application assigned portfolio sections
-- =========================================
CREATE TABLE IF NOT EXISTS university_application_portfolio_sections (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),

    application_id uuid NOT NULL REFERENCES university_applications(id),
    portfolio_section_id uuid REFERENCES portfolio_sections(id),

    required boolean NOT NULL DEFAULT false,
    section_name_override varchar(128),
    section_description_override text,
    sort_order integer NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS ix_uaps_application_id
    ON university_application_portfolio_sections(application_id);
CREATE INDEX IF NOT EXISTS ix_uaps_portfolio_section_id
    ON university_application_portfolio_sections(portfolio_section_id);

-- =========================
-- Portfolio files
-- =========================
CREATE TABLE IF NOT EXISTS university_application_portfolio_files (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),

    application_portfolio_section_id uuid NOT NULL REFERENCES university_application_portfolio_sections(id),

    type varchar(16) NOT NULL,
    name varchar(256) NOT NULL,
    description text,
    file_url varchar(1024) NOT NULL
);

CREATE INDEX IF NOT EXISTS ix_uapf_application_portfolio_section_id
    ON university_application_portfolio_files(application_portfolio_section_id);

-- =========================
-- Notes
-- =========================
CREATE TABLE IF NOT EXISTS university_application_notes (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),

    application_id uuid NOT NULL REFERENCES university_applications(id),
    written_by varchar(128) NOT NULL,
    written_at timestamptz NOT NULL,
    todo_text text NOT NULL
);

CREATE INDEX IF NOT EXISTS ix_uan_application_id
    ON university_application_notes(application_id);

-- =========================
-- Meetings
-- =========================
CREATE TABLE IF NOT EXISTS university_application_meetings (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),

    application_id uuid NOT NULL REFERENCES university_applications(id),
    person varchar(128) NOT NULL,
    meeting_at timestamptz NOT NULL,
    meeting_note text,
    meeting_result text
);

CREATE INDEX IF NOT EXISTS ix_uam_application_id
    ON university_application_meetings(application_id);

-- =========================
-- Tasks
-- =========================
CREATE TABLE IF NOT EXISTS university_application_tasks (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),

    application_id uuid NOT NULL REFERENCES university_applications(id),
    scheduled_at timestamptz NOT NULL,
    with_whom varchar(256) NOT NULL,
    what_to_do text NOT NULL,
    status varchar(16) NOT NULL,
    performed_by_user varchar(128)
);

CREATE INDEX IF NOT EXISTS ix_uat_application_id
    ON university_application_tasks(application_id);

-- =========================
-- Documents
-- =========================
CREATE TABLE IF NOT EXISTS university_application_documents (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),

    application_id uuid NOT NULL REFERENCES university_applications(id),
    required boolean NOT NULL DEFAULT false,
    document_name varchar(128) NOT NULL,
    document_description text,
    document_url varchar(1024) NOT NULL,
    uploaded_at timestamptz NOT NULL
);

CREATE INDEX IF NOT EXISTS ix_uad_application_id
    ON university_application_documents(application_id);

