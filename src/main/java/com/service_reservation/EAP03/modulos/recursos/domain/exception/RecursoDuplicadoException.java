package com.service_reservation.EAP03.modulos.recursos.domain.exception;

public class RecursoDuplicadoException extends RecursoDominioException {
    public RecursoDuplicadoException(String nombre) {
        super("Ya existe un recurso activo con el nombre '" + nombre + "' en este proveedor.");
    }
}
