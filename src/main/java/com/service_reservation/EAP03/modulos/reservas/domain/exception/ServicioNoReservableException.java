package com.service_reservation.EAP03.modulos.reservas.domain.exception;

public class ServicioNoReservableException extends ReservaDominioException {
    public ServicioNoReservableException(Integer idServicio) {
        super("No se encontró el servicio con ID: " + idServicio);
    }
}
