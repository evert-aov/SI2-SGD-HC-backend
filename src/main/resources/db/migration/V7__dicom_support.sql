-- =============================================================================
-- V7: Soporte para estudios DICOM
-- Añade la categoría de documento para distinguir entre formularios clínicos,
-- archivos externos genéricos y estudios de imágenes DICOM.
-- =============================================================================


-- PASO 1: Crear el nuevo tipo ENUM

-- Definimos los tres tipos de documento que puede tener el sistema.
DO $$ BEGIN
    CREATE TYPE document_category_enum AS ENUM (
        'CLINICAL_FORM',
        'EXTERNAL_FILE',
        'DICOM_STUDY' -- Estudio de imagen medica en formato DICOM
    );
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;


-- PASO 2: Añadir la columna a la tabla documents
ALTER TABLE documents
    ADD COLUMN IF NOT EXISTS document_category document_category_enum;


-- PASO 3: Backfill — Poblar los datos existentes
-- Regla:
--   - Si is_external_source = true  → es un archivo externo → EXTERNAL_FILE
--   - Si is_external_source = false → fue creado desde plantilla → CLINICAL_FORM
UPDATE documents
SET document_category = CASE
    WHEN is_external_source = true THEN
    'EXTERNAL_FILE'::document_category_enum
    WHEN is_external_source = false THEN
    'CLINICAL_FORM'::document_category_enum
END
WHERE document_category IS NULL;


-- PASO 4: Hacer la columna NOT NULL con un DEFAULT para nuevas filas
ALTER TABLE documents
    ALTER COLUMN document_category SET NOT NULL,
    ALTER COLUMN document_category SET DEFAULT 'EXTERNAL_FILE';


-- PASO 5: Índice para búsquedas por categoría
-- ─────────────────────────────────────────────────────────────────────────────
-- Cuando el médico pida "todos los estudios DICOM del paciente X",
-- la query filtrará por document_category = 'DICOM_STUDY'.
-- Este índice hace esa búsqueda eficiente.
CREATE INDEX IF NOT EXISTS idx_documents_category ON documents(document_category);


CREATE INDEX IF NOT EXISTS idx_documents_dicom_patient
    ON documents(tenant_id, patient_id, document_category);