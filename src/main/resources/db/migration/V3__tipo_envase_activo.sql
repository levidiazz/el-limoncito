ALTER TABLE tipo_envase ADD COLUMN activo BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE tipo_envase ADD CONSTRAINT uq_tipo_envase_nombre UNIQUE (nombre);
