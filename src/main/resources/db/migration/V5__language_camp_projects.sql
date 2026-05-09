-- language camp projects (school/company specific; supports individual projects)

CREATE TABLE IF NOT EXISTS language_camp_projects (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),

    title varchar(256) NOT NULL UNIQUE,
    company_id uuid REFERENCES companies(id),

    quota int,

    application_start_at timestamptz,
    application_end_at timestamptz,

    project_start_at timestamptz,
    project_end_at timestamptz,

    project_status varchar(16),
    project_type varchar(64),

    banner varchar(1024),
    small_banner varchar(1024),
    images jsonb,
    presentation_video_url varchar(1024),
    presentation_document_url varchar(1024),

    description text,
    duration varchar(64),
    max_people int,
    primary_locations jsonb,
    locations jsonb,
    location varchar(256),

    price numeric,
    original_price numeric,
    currency varchar(8),

    included jsonb,
    excluded jsonb,
    highlights jsonb,
    itinerary jsonb,

    grades varchar(256),
    lesson_groups varchar(256),

    allow_parent boolean,
    allow_teacher boolean,
    allow_manager boolean,

    individual boolean
);

CREATE INDEX IF NOT EXISTS ix_language_camp_projects_company_id ON language_camp_projects(company_id);
CREATE INDEX IF NOT EXISTS ix_language_camp_projects_project_status ON language_camp_projects(project_status);
CREATE INDEX IF NOT EXISTS ix_language_camp_projects_project_type ON language_camp_projects(project_type);

