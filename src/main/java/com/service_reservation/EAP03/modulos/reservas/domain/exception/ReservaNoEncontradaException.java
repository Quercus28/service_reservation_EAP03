package com.service_reservation.EAP03.modulos.reservas.domain.exception;

public class ReservaNoEncontradaException extends ReservaDominioException {
    public ReservaNoEncontradaException(Integer idReserva) {
        super("No se encontró la reserva con ID: " + idReserva);
    }
}
