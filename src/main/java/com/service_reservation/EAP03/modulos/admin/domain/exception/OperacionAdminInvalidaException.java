package com.service_reservation.EAP03.modulos.admin.domain.exception;

/** La operación no es válida en el estado actual (HTTP 409). */
public class OperacionAdminInvalidaException extends AdminDominioException {
    public OperacionAdminInvalidaException(String mensaje) {
        super(mensaje);
    }
}
