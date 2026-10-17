package com.service_reservation.EAP03.modulos.admin.domain.exception;

/** Datos de entrada inválidos (HTTP 400). */
public class DatosAdminInvalidosException extends AdminDominioException {
    public DatosAdminInvalidosException(String mensaje) {
        super(mensaje);
    }
}
