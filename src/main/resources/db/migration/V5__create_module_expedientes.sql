-- V5__create_module_expedientes.sql
-- Creación de la tabla de expedientes clínicos y sus índices optimizados

CREATE TABLE IF NOT EXISTS expedientes (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    numero_expediente VARCHAR(50) UNIQUE NOT NULL,
    patient_id BIGINT NOT NULL,
    estado VARCHAR(50) NOT NULL DEFAULT 'ACTIVO',
    fecha_apertura DATE NOT NULL DEFAULT CURRENT_DATE,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_ultima_modificacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Datos clínicos sensibles (solo accesibles para usuarios autorizados/autenticados)
    diagnostico TEXT,
    tratamiento TEXT,
    observaciones TEXT,
    antecedentes_medicos TEXT,
    
    medico_id BIGINT,
    
    CONSTRAINT fk_expedientes_patient FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    CONSTRAINT fk_expedientes_medico FOREIGN KEY (medico_id) REFERENCES users(id) ON DELETE SET NULL
);

-- Índices optimizados para búsquedas frecuentes y dinámicas
CREATE INDEX IF NOT EXISTS idx_expedientes_patient ON expedientes(patient_id);
CREATE INDEX IF NOT EXISTS idx_expedientes_estado ON expedientes(estado);
CREATE INDEX IF NOT EXISTS idx_expedientes_fecha_apertura ON expedientes(fecha_apertura);
CREATE UNIQUE INDEX IF NOT EXISTS idx_expedientes_numero ON expedientes(numero_expediente);
