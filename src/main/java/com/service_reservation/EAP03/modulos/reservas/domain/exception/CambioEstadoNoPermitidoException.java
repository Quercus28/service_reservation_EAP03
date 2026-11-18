package com.service_reservation.EAP03.modulos.reservas.domain.exception;

import com.service_reservation.EAP03.modulos.reservas.domain.model.EstadoReserva;

// El proveedor pidió un cambio de estado que no está entre los permitidos desde el estado actual.
public class CambioEstadoNoPermitidoException extends ReservaDominioException {
    public CambioEstadoNoPermitidoException(Integer idReserva, EstadoReserva actual, EstadoReserva pedido) {
        super("La acción no es válida para el estado actual de la reserva "
                + idReserva + ": no se puede pasar de " + actual + " a " + pedido + ".");
    }
}
