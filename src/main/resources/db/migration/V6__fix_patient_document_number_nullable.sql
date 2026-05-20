-- V6: Allow null document_number in patients (column inherited NOT NULL from national_id rename in V3,
--     but the entity and form treat it as optional)
ALTER TABLE patients ALTER COLUMN document_number DROP NOT NULL;
