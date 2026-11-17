package com.service_reservation.EAP03.modulos.admin.domain.model;

/**
 * Lo que otro módulo (reservas, agendas...) pide guardar en la auditoría de admin, por ejemplo
 * un intento de ver o cambiar algo que no le pertenece.
 *
 * Nunca debe llevar datos personales (correos, nombres, teléfonos). Las personas se identifican
 * solo por su id.
 *
 * @param idActor           id del usuario que hizo la petición (null si no se pudo saber)
 * @param idUsuarioAfectado id del usuario sobre el que recae la acción (null si no aplica)
 * @param accion            qué se intentó hacer
 * @param resultado         cómo terminó (por ejemplo DENEGADO)
 * @param endpoint          "METODO /ruta" de la petición, sin parámetros
 * @param detalle           texto corto y fijo que explica el motivo, por ejemplo "motivo=reserva de otro cliente"
 */
public record EventoAuditoria(
        Long idActor,
        Long idUsuarioAfectado,
        AccionAdmin accion,
        ResultadoAuditoria resultado,
        String endpoint,
        String detalle
) {
}
