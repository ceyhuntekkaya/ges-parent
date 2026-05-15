-- Replace legacy visa form fields with passport and visa details.

ALTER TABLE language_camp_visa_forms
    DROP COLUMN IF EXISTS birth_place,
    DROP COLUMN IF EXISTS birth_country,
    DROP COLUMN IF EXISTS res_country,
    DROP COLUMN IF EXISTS res_city,
    DROP COLUMN IF EXISTS res_district,
    DROP COLUMN IF EXISTS res_line1,
    DROP COLUMN IF EXISTS res_line2,
    DROP COLUMN IF EXISTS res_postal_code,
    DROP COLUMN IF EXISTS visa_rejected_before,
    DROP COLUMN IF EXISTS visa_rejection_details,
    DROP COLUMN IF EXISTS visited_countries,
    DROP COLUMN IF EXISTS appointment_city_preference;

ALTER TABLE language_camp_visa_forms
    ADD COLUMN passport_number varchar(64),
    ADD COLUMN passport_valid_until date,
    ADD COLUMN passport_type varchar(32),
    ADD COLUMN visa_valid_from date,
    ADD COLUMN visa_valid_until date,
    ADD COLUMN visa_issuing_country varchar(128),
    ADD COLUMN visa_type varchar(128);
