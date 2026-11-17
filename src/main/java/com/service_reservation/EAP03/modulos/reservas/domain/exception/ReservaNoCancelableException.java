package com.service_reservation.EAP03.modulos.reservas.domain.exception;

import com.service_reservation.EAP03.modulos.reservas.domain.model.EstadoReserva;

// El estado actual de la reserva no permite la acción pedida (cancelar o cambiar de estado).
public class ReservaNoCancelableException extends ReservaDominioException {

    private ReservaNoCancelableException(String mensaje) {
        super(mensaje);
    }

    public static ReservaNoCancelableException yaCancelada(Integer idReserva) {
        return new ReservaNoCancelableException("La reserva " + idReserva + " ya estaba cancelada.");
    }

    public static ReservaNoCancelableException noActiva(Integer idReserva, EstadoReserva estado) {
        return new ReservaNoCancelableException("La reserva " + idReserva
                + " ya no se puede cancelar porque no está pendiente ni confirmada (su estado es " + estado + ").");
    }

    // Cambio de estado que no está entre los permitidos para el proveedor.
    public static ReservaNoCancelableException accionNoValida(Integer idReserva, EstadoReserva actual,
                                                              EstadoReserva pedido) {
        return new ReservaNoCancelableException("La acción no es válida para el estado actual de la reserva "
                + idReserva + ": no se puede pasar de " + actual + " a " + pedido + ".");
    }
}
