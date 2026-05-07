-- Seed data (idempotent) for first install.
-- Uses Flyway placeholders:
--  - ${admin_email}
--  - ${admin_password_hash}

-- =========================
-- Admin user
-- =========================
INSERT INTO user_accounts (id, created_at, updated_at, email, password_hash, role, status, last_login_at)
VALUES (gen_random_uuid(), now(), now(), '${admin_email}', '${admin_password_hash}', 'ADMIN', 'ACTIVE', NULL)
ON CONFLICT (email) DO NOTHING;

-- =========================
-- Catalog: countries
-- =========================
INSERT INTO countries (id, created_at, updated_at, code, name)
VALUES
  (gen_random_uuid(), now(), now(), 'TR', 'Türkiye'),
  (gen_random_uuid(), now(), now(), 'DE', 'Germany'),
  (gen_random_uuid(), now(), now(), 'NL', 'Netherlands'),
  (gen_random_uuid(), now(), now(), 'US', 'United States'),
  (gen_random_uuid(), now(), now(), 'GB', 'United Kingdom')
ON CONFLICT (code) DO NOTHING;

-- Catalog: universities (minimal examples)
INSERT INTO universities (id, created_at, updated_at, country_id, name, active)
SELECT gen_random_uuid(), now(), now(), c.id, u.name, true
FROM (VALUES
    ('TR', 'Boğaziçi University'),
    ('TR', 'İstanbul Technical University'),
    ('DE', 'Technical University of Munich'),
    ('NL', 'Delft University of Technology'),
    ('GB', 'University of Manchester'),
    ('US', 'University of California, Berkeley')
) AS u(country_code, name)
JOIN countries c ON c.code = u.country_code
WHERE NOT EXISTS (
  SELECT 1 FROM universities x
  WHERE x.country_id = c.id AND x.name = u.name
);

-- Catalog: departments
INSERT INTO departments (id, created_at, updated_at, name, active)
VALUES
  (gen_random_uuid(), now(), now(), 'Computer Engineering', true),
  (gen_random_uuid(), now(), now(), 'Electrical & Electronics Engineering', true),
  (gen_random_uuid(), now(), now(), 'Business Administration', true),
  (gen_random_uuid(), now(), now(), 'International Relations', true)
ON CONFLICT (name) DO NOTHING;

-- =========================
-- Programs
-- =========================
INSERT INTO programs (id, created_at, updated_at, module, name, active, start_date, end_date)
SELECT gen_random_uuid(), now(), now(), p.module, p.name, true, NULL, NULL
FROM (VALUES
  ('LANGUAGE_CAMP', 'General English (4 Weeks)'),
  ('LANGUAGE_CAMP', 'IELTS Preparation (4 Weeks)'),
  ('UNIVERSITY', 'University Application Package')
) AS p(module, name)
WHERE NOT EXISTS (
  SELECT 1 FROM programs x WHERE x.module = p.module AND x.name = p.name
);

-- =========================
-- Document requirements (examples)
-- =========================
INSERT INTO document_requirements (
  id, created_at, updated_at,
  scope, category, requirement_key,
  required, allowed_content_types, max_size_bytes,
  title, description, active
)
SELECT
  gen_random_uuid(), now(), now(),
  r.scope, r.category, r.requirement_key,
  r.required, r.allowed_content_types, r.max_size_bytes,
  r.title, r.description, true
FROM (VALUES
  ('LANGUAGE_CAMP_APPLICATION', NULL, 'PASSPORT_COPY', true,  'application/pdf,image/jpeg,image/png', 20971520::bigint, 'Passport copy', 'Passport main page scan (PDF/JPG/PNG).'),
  ('LANGUAGE_CAMP_APPLICATION', NULL, 'GUARDIAN_CONSENT', false,'application/pdf,image/jpeg,image/png', 20971520::bigint, 'Guardian consent', 'Required if participant is under 18.'),
  ('LANGUAGE_CAMP_PARTICIPANT', NULL, 'BIOMETRIC_PHOTO', false,'image/jpeg,image/png',               10485760::bigint, 'Biometric photo', 'Recent biometric photo.'),
  ('LANGUAGE_CAMP_PARTICIPANT', NULL, 'BANK_STATEMENT', false,'application/pdf,image/jpeg,image/png', 20971520::bigint, 'Bank statement', 'If visa process is required.'),
  ('UNIVERSITY_APPLICATION',    NULL, 'TRANSCRIPT',     true, 'application/pdf,image/jpeg,image/png', 20971520::bigint, 'Transcript', 'Latest transcript.'),
  ('UNIVERSITY_APPLICATION',    NULL, 'DIPLOMA',        false,'application/pdf,image/jpeg,image/png', 20971520::bigint, 'Diploma', 'If available.'),
  ('UNIVERSITY_APPLICATION',    NULL, 'CV',            false,'application/pdf',                      20971520::bigint, 'CV', 'Academic/professional CV.'),
  ('UNIVERSITY_REFERENCE',      NULL, 'REFERENCE_LETTER',false,'application/pdf,image/jpeg,image/png',20971520::bigint,'Reference letter','Uploaded by referee or applicant.')
) AS r(scope, category, requirement_key, required, allowed_content_types, max_size_bytes, title, description)
WHERE NOT EXISTS (
  SELECT 1 FROM document_requirements x
  WHERE x.scope = r.scope AND x.requirement_key = r.requirement_key
);

-- =========================
-- Consent documents (examples)
-- =========================
INSERT INTO consent_documents (id, created_at, updated_at, type, language, version, active, text)
SELECT gen_random_uuid(), now(), now(), c.type, c.language, c.version, true, c.text
FROM (VALUES
  ('KVKK', 'tr', '2026-05-07-v1', 'Kişisel verileriniz, başvuru süreçlerinin yürütülmesi ve hizmetlerin sunulması amacıyla KVKK kapsamında işlenir.'),
  ('EXPLICIT_CONSENT', 'tr', '2026-05-07-v1', 'Açık rıza metni: iletişim ve bilgilendirme amaçlı onay.'),
  ('HEALTH_DATA_PROCESSING', 'tr', '2026-05-07-v1', 'Sağlık verileriniz yalnızca vize/konaklama süreçleri için gerekli olması halinde ve sınırlı şekilde işlenir.'),
  ('PASSPORT_DATA_PROCESSING', 'tr', '2026-05-07-v1', 'Pasaport bilgileriniz vize ve kayıt süreçleri için işlenir.')
) AS c(type, language, version, text)
WHERE NOT EXISTS (
  SELECT 1 FROM consent_documents x
  WHERE x.type = c.type AND x.language = c.language AND x.version = c.version
);

