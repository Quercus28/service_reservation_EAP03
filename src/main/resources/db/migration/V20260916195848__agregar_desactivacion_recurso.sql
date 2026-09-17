ALTER TABLE recurso
    ADD COLUMN eliminado_en TIMESTAMP;

CREATE INDEX idx_recurso_proveedor_activos ON recurso (id_proveedor, eliminado_en);
