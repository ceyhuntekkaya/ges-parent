-- Language camp application payments and payment-completed flag

ALTER TABLE language_camp_applications
    ADD COLUMN IF NOT EXISTS payment_completed boolean NOT NULL DEFAULT false;

CREATE TABLE IF NOT EXISTS language_camp_application_payments (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),

    application_id uuid NOT NULL REFERENCES language_camp_applications(id) ON DELETE CASCADE,
    payment_at timestamptz NOT NULL,
    amount numeric NOT NULL,
    currency varchar(8) NOT NULL,
    received_by varchar(128)
);

CREATE INDEX IF NOT EXISTS ix_lcap_application_id
    ON language_camp_application_payments(application_id);
