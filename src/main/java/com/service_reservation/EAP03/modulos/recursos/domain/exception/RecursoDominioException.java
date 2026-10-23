package com.service_reservation.EAP03.modulos.recursos.domain.exception;

public abstract class RecursoDominioException extends RuntimeException {
    protected RecursoDominioException(String mensaje) {
        super(mensaje);
    }

    // "1 unidad", "2 unidades", "0 unidades".
    protected static String unidades(int cantidad) {
        return cantidad + (cantidad == 1 ? " unidad" : " unidades");
    }
}
