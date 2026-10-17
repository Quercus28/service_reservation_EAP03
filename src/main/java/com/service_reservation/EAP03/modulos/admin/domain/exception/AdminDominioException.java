package com.service_reservation.EAP03.modulos.admin.domain.exception;

public abstract class AdminDominioException extends RuntimeException {
    protected AdminDominioException(String mensaje) {
        super(mensaje);
    }
}
