-- Sentencias ALTER TABLE separadas: H2 (usado en tests) no acepta varias cláusulas ADD
-- en un mismo ALTER TABLE, ni siquiera con MODE=PostgreSQL.
ALTER TABLE recurso_prestado ADD COLUMN fecha_inicio TIMESTAMP NOT NULL;
ALTER TABLE recurso_prestado ADD COLUMN fecha_fin TIMESTAMP NOT NULL;
ALTER TABLE recurso_prestado ADD CONSTRAINT chk_recurso_prestado_fechas CHECK (fecha_fin > fecha_inicio);

CREATE INDEX idx_recurso_prestado_disponibilidad ON recurso_prestado (id_recurso, fecha_inicio, fecha_fin);
