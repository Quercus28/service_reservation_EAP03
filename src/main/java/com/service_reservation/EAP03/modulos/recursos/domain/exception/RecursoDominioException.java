package com.service_reservation.EAP03.modulos.recursos.domain.exception;

public abstract class RecursoDominioException extends RuntimeException {
    protected RecursoDominioException(String mensaje) {
        super(mensaje);
    }
}
