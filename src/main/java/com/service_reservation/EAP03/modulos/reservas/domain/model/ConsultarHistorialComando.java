package com.service_reservation.EAP03.modulos.reservas.domain.model;

/**
 * Datos de una consulta de historial.
 *
 * @param idUsuario           usuario autenticado (viene del JWT)
 * @param idClienteSolicitado cliente que pidió ver en la solicitud; null si no indicó ninguno
 * @param estado              filtro opcional por estado; null si no se filtra
 */
public record ConsultarHistorialComando(
        Long idUsuario,
        Long idClienteSolicitado,
        EstadoReserva estado,
        int page,
        int size
) {
}