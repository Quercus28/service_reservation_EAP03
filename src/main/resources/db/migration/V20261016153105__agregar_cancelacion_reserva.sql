-- Módulo: reservas (HU09 - cancelar reserva)
--
-- Agrega a servicio_prestado los datos de la cancelación:
--   cancelada_en:       fecha y hora en que el cliente canceló. Queda vacía (NULL) mientras
--                       la reserva no esté cancelada.
--   motivo_cancelacion: texto opcional que el cliente puede dejar al cancelar.
--
-- Una sentencia por columna porque H2 (la base de las pruebas) no acepta varios ADD en el
-- mismo ALTER TABLE.

ALTER TABLE servicio_prestado ADD COLUMN cancelada_en TIMESTAMP;
ALTER TABLE servicio_prestado ADD COLUMN motivo_cancelacion VARCHAR(255);
