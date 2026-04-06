-- V3__sync_schema.sql
-- Asegurar que estamos trabajando en el esquema public
SET search_path TO public;

-- 1. Sincronizar tabla permissions 
ALTER TABLE permissions ADD COLUMN IF NOT EXISTS module VARCHAR(50);
ALTER TABLE permissions ADD COLUMN IF NOT EXISTS active BOOLEAN DEFAULT TRUE;

-- 2. Sincronizar tabla roles
ALTER TABLE roles ADD COLUMN IF NOT EXISTS active BOOLEAN DEFAULT TRUE;

-- 3. Migración de datos role_permissions -> role_permission
/*DO $$ 
BEGIN
    -- Solo si existe la tabla vieja ()
    IF EXISTS (SELECT FROM pg_tables WHERE schemaname = 'public' AND tablename = 'role_permissions') THEN

        DROP TABLE role_permissions;
    
    END IF;
END $$;
*/