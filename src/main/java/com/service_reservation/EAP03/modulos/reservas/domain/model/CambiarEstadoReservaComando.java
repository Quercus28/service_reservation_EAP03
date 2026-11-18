package com.service_reservation.EAP03.modulos.reservas.domain.model;

/**
 * @param idUsuario  usuario que inició sesión (sale del JWT)
 * @param idReserva  reserva a cambiar
 * @param nuevoEstado estado pedido
 * @param endpoint   "METODO /ruta" de la petición, solo para la auditoría si se rechaza
 */
public record CambiarEstadoReservaComando(
        Long idUsuario,
        Integer idReserva,
        EstadoReserva nuevoEstado,
        String endpoint
) {}
