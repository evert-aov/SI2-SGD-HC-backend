-- V6__add_patient_columns.sql
-- Agregar columnas faltantes a la tabla patients (solo las que le pertenecen)

-- Agregar columna phone (teléfono)
ALTER TABLE patients 
ADD COLUMN IF NOT EXISTS phone VARCHAR(20);

-- Agregar columna address (dirección)
ALTER TABLE patients 
ADD COLUMN IF NOT EXISTS address VARCHAR(255);

-- Agregar columna birth_date (fecha de nacimiento)
ALTER TABLE patients 
ADD COLUMN IF NOT EXISTS birth_date DATE;