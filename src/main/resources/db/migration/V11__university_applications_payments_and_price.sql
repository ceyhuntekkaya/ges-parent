-- University application pricing fields and payments table

ALTER TABLE university_applications
    ADD COLUMN IF NOT EXISTS price_amount numeric,
    ADD COLUMN IF NOT EXISTS price_currency varchar(8);

CREATE TABLE IF NOT EXISTS university_application_payments (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),

    application_id uuid NOT NULL REFERENCES university_applications(id),
    payment_at timestamptz NOT NULL,
    amount numeric NOT NULL,
    currency varchar(8) NOT NULL,
    received_by varchar(128)
);

CREATE INDEX IF NOT EXISTS ix_uap_application_id
    ON university_application_payments(application_id);

