package com.service_reservation.EAP03.modulos.reservas.domain.exception;

public abstract class ReservaDominioException extends RuntimeException {
    protected ReservaDominioException(String mensaje) {
        super(mensaje);
    }
}
