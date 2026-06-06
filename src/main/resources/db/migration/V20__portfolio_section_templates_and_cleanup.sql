-- Extend portfolio section catalog with template matching fields
ALTER TABLE portfolio_sections
    ADD COLUMN IF NOT EXISTS education_level varchar(16),
    ADD COLUMN IF NOT EXISTS department_keyword varchar(128),
    ADD COLUMN IF NOT EXISTS sort_order integer NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS default_required boolean NOT NULL DEFAULT false,
    ADD COLUMN IF NOT EXISTS active boolean NOT NULL DEFAULT true;

CREATE INDEX IF NOT EXISTS ix_portfolio_sections_active_sort
    ON portfolio_sections(active, sort_order);

-- Seed default catalog templates (idempotent by name)
INSERT INTO portfolio_sections (name, description, education_level, department_keyword, sort_order, default_required, active)
SELECT 'Proje Çalışmaları', 'Akademik veya kişisel proje örnekleri', NULL, NULL, 10, false, true
WHERE NOT EXISTS (SELECT 1 FROM portfolio_sections WHERE name = 'Proje Çalışmaları');

INSERT INTO portfolio_sections (name, description, education_level, department_keyword, sort_order, default_required, active)
SELECT 'GitHub / Teknik Link', 'GitHub, Behance veya benzeri profil bağlantısı', NULL, NULL, 20, false, true
WHERE NOT EXISTS (SELECT 1 FROM portfolio_sections WHERE name = 'GitHub / Teknik Link');

INSERT INTO portfolio_sections (name, description, education_level, department_keyword, sort_order, default_required, active)
SELECT 'Sanat / Tasarım Portfolyosu', 'Görsel çalışma örnekleri (resim, PDF)', NULL, 'mimar', 30, true, true
WHERE NOT EXISTS (SELECT 1 FROM portfolio_sections WHERE name = 'Sanat / Tasarım Portfolyosu');

INSERT INTO portfolio_sections (name, description, education_level, department_keyword, sort_order, default_required, active)
SELECT 'Araştırma Önerisi', 'Yüksek lisans / doktora araştırma önerisi veya yazım örneği', 'MASTER', NULL, 40, false, true
WHERE NOT EXISTS (SELECT 1 FROM portfolio_sections WHERE name = 'Araştırma Önerisi');

INSERT INTO portfolio_sections (name, description, education_level, department_keyword, sort_order, default_required, active)
SELECT 'Araştırma Önerisi', 'Yüksek lisans / doktora araştırma önerisi veya yazım örneği', 'PHD', NULL, 40, false, true
WHERE NOT EXISTS (SELECT 1 FROM portfolio_sections WHERE name = 'Araştırma Önerisi' AND education_level = 'PHD');

INSERT INTO portfolio_sections (name, description, education_level, department_keyword, sort_order, default_required, active)
SELECT 'Video Tanıtım', 'Kısa tanıtım veya proje sunumu videosu', NULL, NULL, 50, false, true
WHERE NOT EXISTS (SELECT 1 FROM portfolio_sections WHERE name = 'Video Tanıtım');

-- Legacy unused table from V1
DROP TABLE IF EXISTS portfolio_documents;
