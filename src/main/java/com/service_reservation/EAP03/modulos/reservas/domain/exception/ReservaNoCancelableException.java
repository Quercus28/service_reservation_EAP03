package com.service_reservation.EAP03.modulos.reservas.domain.exception;

import com.service_reservation.EAP03.modulos.reservas.domain.model.EstadoReserva;

public class ReservaNoCancelableException extends ReservaDominioException {

    private ReservaNoCancelableException(String mensaje) {
        super(mensaje);
    }

    public static ReservaNoCancelableException yaCancelada(Integer idReserva) {
        return new ReservaNoCancelableException("La reserva " + idReserva + " ya estaba cancelada.");
    }

    public static ReservaNoCancelableException noActiva(Integer idReserva, EstadoReserva estado) {
        return new ReservaNoCancelableException("La reserva " + idReserva
                + " ya no se puede cancelar porque no está pendiente (su estado es " + estado + ").");
    }
}
