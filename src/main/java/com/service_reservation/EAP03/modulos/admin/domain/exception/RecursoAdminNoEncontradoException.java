package com.service_reservation.EAP03.modulos.admin.domain.exception;

/** Solicitud, usuario o rol inexistente (HTTP 404). */
public class RecursoAdminNoEncontradoException extends AdminDominioException {
    public RecursoAdminNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
