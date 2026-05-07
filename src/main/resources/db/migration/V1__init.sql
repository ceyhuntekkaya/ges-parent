-- Initial schema for GES (PostgreSQL)
-- Notes:
-- - Uses UUID PKs with pgcrypto's gen_random_uuid()
-- - Keeps created_at / updated_at defaults for safety (app also sets them)

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- =========================
-- Core auth
-- =========================
CREATE TABLE IF NOT EXISTS user_accounts (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),

    email varchar(255) NOT NULL,
    password_hash varchar(255) NOT NULL,
    role varchar(16) NOT NULL,
    status varchar(16) NOT NULL,
    last_login_at timestamptz
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_user_accounts_email ON user_accounts(email);

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),

    user_account_id uuid NOT NULL REFERENCES user_accounts(id),
    token_hash varchar(128) NOT NULL,
    issued_at timestamptz NOT NULL,
    expires_at timestamptz NOT NULL,
    revoked_at timestamptz,
    replaced_by_token_hash varchar(256),
    user_agent varchar(512),
    ip_address varchar(64)
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_refresh_tokens_token_hash ON refresh_tokens(token_hash);
CREATE INDEX IF NOT EXISTS ix_refresh_tokens_user_account_id ON refresh_tokens(user_account_id);

CREATE TABLE IF NOT EXISTS one_time_tokens (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),

    user_account_id uuid NOT NULL REFERENCES user_accounts(id),
    purpose varchar(32) NOT NULL,
    token_hash varchar(128) NOT NULL,
    issued_at timestamptz NOT NULL,
    expires_at timestamptz NOT NULL,
    used_at timestamptz
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_one_time_tokens_token_hash ON one_time_tokens(token_hash);
CREATE INDEX IF NOT EXISTS ix_one_time_tokens_user_account_id ON one_time_tokens(user_account_id);

-- =========================
-- Storage
-- =========================
CREATE TABLE IF NOT EXISTS stored_files (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),

    purpose varchar(64) NOT NULL,
    storage_key varchar(512) NOT NULL,
    original_filename varchar(255) NOT NULL,
    content_type varchar(128) NOT NULL,
    size_bytes bigint NOT NULL,
    sha256 varchar(64),
    uploaded_by_user_id uuid REFERENCES user_accounts(id)
);

CREATE INDEX IF NOT EXISTS ix_stored_files_uploaded_by_user_id ON stored_files(uploaded_by_user_id);

-- =========================
-- Applicant
-- =========================
CREATE TABLE IF NOT EXISTS applicant_profiles (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),

    user_account_id uuid NOT NULL REFERENCES user_accounts(id),
    first_name varchar(64),
    last_name varchar(64),
    birth_date date,
    phone varchar(32),
    nationality varchar(128),

    addr_country varchar(128),
    addr_city varchar(128),
    addr_district varchar(128),
    addr_line1 varchar(512),
    addr_line2 varchar(512),
    addr_postal_code varchar(32)
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_applicant_profiles_user_account_id ON applicant_profiles(user_account_id);

-- =========================
-- Legal
-- =========================
CREATE TABLE IF NOT EXISTS consent_documents (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),

    type varchar(32) NOT NULL,
    language varchar(16) NOT NULL,
    version varchar(32) NOT NULL,
    active boolean NOT NULL DEFAULT true,
    text text NOT NULL
);

CREATE TABLE IF NOT EXISTS consent_acceptances (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),

    user_account_id uuid NOT NULL REFERENCES user_accounts(id),
    consent_document_id uuid NOT NULL REFERENCES consent_documents(id),
    accepted_at timestamptz,
    ip_address varchar(64),
    user_agent varchar(512),
    module varchar(32),
    application_id uuid
);

CREATE INDEX IF NOT EXISTS ix_consent_acceptances_user_account_id ON consent_acceptances(user_account_id);
CREATE INDEX IF NOT EXISTS ix_consent_acceptances_consent_document_id ON consent_acceptances(consent_document_id);

-- =========================
-- Notifications
-- =========================
CREATE TABLE IF NOT EXISTS notifications (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),

    recipient_user_id uuid NOT NULL REFERENCES user_accounts(id),
    channel varchar(16) NOT NULL,
    type varchar(64) NOT NULL,
    status varchar(16) NOT NULL,
    subject varchar(255),
    body text,
    sent_at timestamptz,
    last_error text
);

CREATE INDEX IF NOT EXISTS ix_notifications_recipient_user_id ON notifications(recipient_user_id);
CREATE INDEX IF NOT EXISTS ix_notifications_status ON notifications(status);

-- =========================
-- Catalog
-- =========================
CREATE TABLE IF NOT EXISTS countries (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),

    code varchar(8) NOT NULL,
    name varchar(128) NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_countries_code ON countries(code);

CREATE TABLE IF NOT EXISTS universities (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),

    country_id uuid NOT NULL REFERENCES countries(id),
    name varchar(255) NOT NULL,
    active boolean NOT NULL DEFAULT true
);

CREATE INDEX IF NOT EXISTS ix_universities_country_id ON universities(country_id);

CREATE TABLE IF NOT EXISTS departments (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),

    name varchar(255) NOT NULL,
    active boolean NOT NULL DEFAULT true
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_departments_name ON departments(name);

-- =========================
-- Programs
-- =========================
CREATE TABLE IF NOT EXISTS programs (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),

    module varchar(16) NOT NULL,
    name varchar(128) NOT NULL,
    active boolean NOT NULL DEFAULT true,
    start_date date,
    end_date date
);

CREATE INDEX IF NOT EXISTS ix_programs_module ON programs(module);

-- =========================
-- University applications
-- =========================
CREATE TABLE IF NOT EXISTS university_applications (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),

    applicant_user_id uuid NOT NULL REFERENCES user_accounts(id),
    applicant_profile_id uuid REFERENCES applicant_profiles(id),

    status varchar(32) NOT NULL,
    education_level varchar(16) NOT NULL,

    department_preferences jsonb,
    country_preferences jsonb,
    university_preferences jsonb,

    start_term_season varchar(8),
    start_year integer,

    yearly_budget_min numeric,
    yearly_budget_max numeric,

    scholarship_requested boolean,
    scholarship_type varchar(128),
    accommodation_type varchar(16),

    notes text,
    preferences_completed_at timestamptz
);

CREATE INDEX IF NOT EXISTS ix_university_applications_applicant_user_id ON university_applications(applicant_user_id);
CREATE INDEX IF NOT EXISTS ix_university_applications_applicant_profile_id ON university_applications(applicant_profile_id);
CREATE INDEX IF NOT EXISTS ix_university_applications_status ON university_applications(status);

CREATE TABLE IF NOT EXISTS portfolio_documents (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),

    university_application_id uuid NOT NULL REFERENCES university_applications(id),
    category varchar(32) NOT NULL,
    type varchar(32) NOT NULL,
    title varchar(255),
    related_program varchar(255),
    file_id uuid REFERENCES stored_files(id),
    external_url varchar(1024)
);

CREATE INDEX IF NOT EXISTS ix_portfolio_documents_university_application_id ON portfolio_documents(university_application_id);
CREATE UNIQUE INDEX IF NOT EXISTS ux_portfolio_documents_file_id ON portfolio_documents(file_id) WHERE file_id IS NOT NULL;

CREATE TABLE IF NOT EXISTS language_exam_results (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),

    university_application_id uuid NOT NULL REFERENCES university_applications(id),
    exam_name varchar(64) NOT NULL,
    score varchar(64),
    exam_date date
);

CREATE INDEX IF NOT EXISTS ix_language_exam_results_university_application_id ON language_exam_results(university_application_id);

CREATE TABLE IF NOT EXISTS reference_letters (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),

    university_application_id uuid NOT NULL REFERENCES university_applications(id),
    reference_type varchar(16) NOT NULL,
    status varchar(16) NOT NULL,
    referee_name varchar(128),
    referee_email varchar(255),
    requested_at timestamptz,
    received_at timestamptz,
    file_id uuid REFERENCES stored_files(id),
    upload_token_hash varchar(128)
);

CREATE INDEX IF NOT EXISTS ix_reference_letters_university_application_id ON reference_letters(university_application_id);
CREATE UNIQUE INDEX IF NOT EXISTS ux_reference_letters_file_id ON reference_letters(file_id) WHERE file_id IS NOT NULL;

-- =========================
-- Document requirements & uploads
-- =========================
CREATE TABLE IF NOT EXISTS document_requirements (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),

    scope varchar(32) NOT NULL,
    category varchar(32),
    requirement_key varchar(128) NOT NULL,
    required boolean NOT NULL,
    allowed_content_types varchar(255),
    max_size_bytes bigint NOT NULL,
    title varchar(255),
    description text,
    active boolean NOT NULL DEFAULT true
);

CREATE INDEX IF NOT EXISTS ix_document_requirements_scope ON document_requirements(scope);
CREATE UNIQUE INDEX IF NOT EXISTS ux_document_requirements_scope_key ON document_requirements(scope, requirement_key);

CREATE TABLE IF NOT EXISTS application_documents (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),

    scope varchar(32) NOT NULL,
    application_id uuid NOT NULL,
    related_entity_id uuid,

    requirement_id uuid REFERENCES document_requirements(id),
    requirement_key varchar(128) NOT NULL,

    file_id uuid NOT NULL REFERENCES stored_files(id),
    status varchar(16) NOT NULL,
    uploaded_at timestamptz,
    uploaded_by_user_id uuid REFERENCES user_accounts(id),
    review_note text
);

CREATE INDEX IF NOT EXISTS ix_application_documents_scope_application_id ON application_documents(scope, application_id);
CREATE INDEX IF NOT EXISTS ix_application_documents_requirement_id ON application_documents(requirement_id);
CREATE INDEX IF NOT EXISTS ix_application_documents_uploaded_by_user_id ON application_documents(uploaded_by_user_id);
CREATE UNIQUE INDEX IF NOT EXISTS ux_application_documents_file_id ON application_documents(file_id);

-- =========================
-- Admin workflow
-- =========================
CREATE TABLE IF NOT EXISTS missing_items (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),

    module varchar(32) NOT NULL,
    application_id uuid NOT NULL,
    related_entity_id uuid,
    item_key varchar(128) NOT NULL,
    message text,
    status varchar(16) NOT NULL,
    opened_at timestamptz,
    resolved_at timestamptz,
    opened_by_user_id uuid REFERENCES user_accounts(id),
    resolved_by_user_id uuid REFERENCES user_accounts(id)
);

CREATE INDEX IF NOT EXISTS ix_missing_items_module_application_id ON missing_items(module, application_id);
CREATE INDEX IF NOT EXISTS ix_missing_items_status ON missing_items(status);

CREATE TABLE IF NOT EXISTS counselor_assignments (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),

    module varchar(32) NOT NULL,
    application_id uuid NOT NULL,
    counselor_user_id uuid NOT NULL REFERENCES user_accounts(id),
    assigned_at timestamptz,
    assigned_by_user_id uuid REFERENCES user_accounts(id)
);

CREATE INDEX IF NOT EXISTS ix_counselor_assignments_module_application_id ON counselor_assignments(module, application_id);
CREATE INDEX IF NOT EXISTS ix_counselor_assignments_counselor_user_id ON counselor_assignments(counselor_user_id);

-- =========================
-- Language camp
-- =========================
CREATE TABLE IF NOT EXISTS language_camp_applications (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),

    applicant_user_id uuid NOT NULL REFERENCES user_accounts(id),
    applicant_profile_id uuid REFERENCES applicant_profiles(id),

    category varchar(16) NOT NULL,
    status varchar(32) NOT NULL,

    program_id uuid REFERENCES programs(id),
    start_date date,
    end_date date,
    accommodation_type varchar(16),

    visa_needed boolean,
    visa_follow_by_ges boolean,

    emergency_contact_full_name varchar(128),
    emergency_contact_phone varchar(32),
    emergency_contact_relationship varchar(64),

    payment_preference varchar(16),
    kvkk_accepted_at timestamptz,

    guardian_consent_file_id uuid REFERENCES stored_files(id),

    company_name varchar(255),
    tax_number varchar(64),
    company_contact_full_name varchar(128),
    company_contact_phone varchar(32),
    company_contact_email varchar(255),

    invoice_country varchar(128),
    invoice_city varchar(128),
    invoice_district varchar(128),
    invoice_line1 varchar(512),
    invoice_line2 varchar(512),
    invoice_postal_code varchar(32),

    bulk_participants_file_id uuid REFERENCES stored_files(id)
);

CREATE INDEX IF NOT EXISTS ix_language_camp_applications_applicant_user_id ON language_camp_applications(applicant_user_id);
CREATE INDEX IF NOT EXISTS ix_language_camp_applications_applicant_profile_id ON language_camp_applications(applicant_profile_id);
CREATE INDEX IF NOT EXISTS ix_language_camp_applications_program_id ON language_camp_applications(program_id);
CREATE UNIQUE INDEX IF NOT EXISTS ux_language_camp_applications_guardian_consent_file_id ON language_camp_applications(guardian_consent_file_id) WHERE guardian_consent_file_id IS NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS ux_language_camp_applications_bulk_participants_file_id ON language_camp_applications(bulk_participants_file_id) WHERE bulk_participants_file_id IS NOT NULL;

CREATE TABLE IF NOT EXISTS language_camp_participants (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),

    application_id uuid NOT NULL REFERENCES language_camp_applications(id),

    first_name varchar(64) NOT NULL,
    last_name varchar(64) NOT NULL,
    birth_date date,
    nationality varchar(128),
    identity_number varchar(64),
    passport_series varchar(16),
    passport_number varchar(32),
    passport_expiry_date date,
    allergies_and_health text,
    medication_usage text,
    under18 boolean,
    guardian_consent_file_id uuid REFERENCES stored_files(id),

    program_id uuid REFERENCES programs(id),
    start_date date,
    end_date date,
    accommodation_type varchar(16),
    visa_needed boolean,
    visa_follow_by_ges boolean
);

CREATE INDEX IF NOT EXISTS ix_language_camp_participants_application_id ON language_camp_participants(application_id);
CREATE INDEX IF NOT EXISTS ix_language_camp_participants_program_id ON language_camp_participants(program_id);
CREATE UNIQUE INDEX IF NOT EXISTS ux_language_camp_participants_guardian_consent_file_id ON language_camp_participants(guardian_consent_file_id) WHERE guardian_consent_file_id IS NOT NULL;

CREATE TABLE IF NOT EXISTS language_camp_visa_forms (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),

    participant_id uuid NOT NULL REFERENCES language_camp_participants(id),
    birth_place varchar(128),
    birth_country varchar(128),

    res_country varchar(128),
    res_city varchar(128),
    res_district varchar(128),
    res_line1 varchar(512),
    res_line2 varchar(512),
    res_postal_code varchar(32),

    visa_rejected_before boolean,
    visa_rejection_details text,
    visited_countries jsonb,

    bank_statement_file_id uuid REFERENCES stored_files(id),
    biometric_photo_file_id uuid REFERENCES stored_files(id),

    appointment_city_preference varchar(128)
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_language_camp_visa_forms_participant_id ON language_camp_visa_forms(participant_id);
CREATE UNIQUE INDEX IF NOT EXISTS ux_language_camp_visa_forms_bank_statement_file_id ON language_camp_visa_forms(bank_statement_file_id) WHERE bank_statement_file_id IS NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS ux_language_camp_visa_forms_biometric_photo_file_id ON language_camp_visa_forms(biometric_photo_file_id) WHERE biometric_photo_file_id IS NOT NULL;

