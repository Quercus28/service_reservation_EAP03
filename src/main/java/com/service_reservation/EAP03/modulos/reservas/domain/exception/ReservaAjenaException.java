package com.service_reservation.EAP03.modulos.reservas.domain.exception;

public class ReservaAjenaException extends ReservaDominioException {
    public ReservaAjenaException(Integer idReserva) {
        super("No tiene permiso sobre la reserva " + idReserva + ": pertenece a otro cliente.");
    }
}
