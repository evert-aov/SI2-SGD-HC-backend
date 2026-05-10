-- 1. ROLES: El nombre de rol debe ser ÚNICO POR TENANT
ALTER TABLE roles ADD CONSTRAINT uq_roles_name_tenant UNIQUE (tenant_id, name);

-- 2. SUSCRIPCIÓN: estado PENDING_PAYMENT 
ALTER TYPE subscription_status_enum ADD VALUE IF NOT EXISTS 'PENDING_PAYMENT';

-- 3. USUARIOS: Ampliar longitud de campos
ALTER TABLE users ALTER COLUMN username TYPE varchar(50);
ALTER TABLE users ALTER COLUMN document_number TYPE varchar(20);
