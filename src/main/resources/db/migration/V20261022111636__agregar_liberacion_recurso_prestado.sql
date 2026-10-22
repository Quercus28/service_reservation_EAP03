-- Módulo: recursos
--
-- liberado_en: fecha y hora en que se devolvieron las unidades de un préstamo, por ejemplo
-- porque se canceló la reserva. Vacía (NULL) quiere decir que el préstamo sigue activo y sus
-- unidades siguen ocupadas. No se borran los préstamos liberados para no perder el historial.

ALTER TABLE recurso_prestado ADD COLUMN liberado_en TIMESTAMP;

CREATE INDEX idx_recurso_prestado_reserva_activo ON recurso_prestado (id_servicio_prestado, liberado_en);
