-- Snapshot project fee on each language camp application

ALTER TABLE language_camp_applications
    ADD COLUMN IF NOT EXISTS price_amount numeric,
    ADD COLUMN IF NOT EXISTS price_currency varchar(8);

UPDATE language_camp_applications a
SET
    price_amount = p.price,
    price_currency = p.currency
FROM language_camp_projects p
WHERE a.language_camp_project_id = p.id
  AND a.price_amount IS NULL;
