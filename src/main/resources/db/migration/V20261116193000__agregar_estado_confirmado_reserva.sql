-- Módulo: reservas (HU20 - el proveedor cambia el estado de sus reservas)
--
-- Agrega el estado CONFIRMADO a servicio_prestado. No se renombra ningún estado:
--   RESERVADO  = "Pendiente" de la HU (con este estado se crea toda reserva nueva)
--   CONFIRMADO = "Confirmada" (nuevo: el proveedor aceptó la reserva)
--   COMPLETADO = "Finalizada"
--
-- Se borra el CHECK y se vuelve a crear con la lista nueva. Dos sentencias separadas para
-- que el script corra igual en PostgreSQL y en H2 (tests).

ALTER TABLE servicio_prestado DROP CONSTRAINT chk_servicio_prestado_estado;
ALTER TABLE servicio_prestado ADD CONSTRAINT chk_servicio_prestado_estado
    CHECK (estado IN ('RESERVADO', 'CONFIRMADO', 'EN_PROCESO', 'COMPLETADO', 'CANCELADO', 'NO_ASISTIO'));
