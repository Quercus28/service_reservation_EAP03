package com.service_reservation.EAP03.modulos.admin.domain.model;

import java.time.LocalDateTime;

/**
 * Entrada de auditoría administrativa.
 *
 * @param idActor           usuario que ejecutó la acción (null = SISTEMA, p. ej. el job de revocaciones)
 * @param idUsuarioAfectado usuario sobre el que recae la acción (null si no aplica)
 * @param rol               rol operativo involucrado (null si no aplica)
 * @param endpoint          "METODO /ruta" invocado (null para acciones del sistema)
 */
public record RegistroAuditoria(
        LocalDateTime fecha,
        Long idActor,
        Long idUsuarioAfectado,
        AccionAdmin accion,
        RolOperativo rol,
        ResultadoAuditoria resultado,
        String endpoint,
        String detalle
) {
    public static RegistroAuditoria exito(LocalDateTime fecha, Long idActor, Long idUsuarioAfectado,
                                          AccionAdmin accion, RolOperativo rol, String detalle) {
        return new RegistroAuditoria(fecha, idActor, idUsuarioAfectado, accion, rol,
                ResultadoAuditoria.EXITO, null, detalle);
    }
}
