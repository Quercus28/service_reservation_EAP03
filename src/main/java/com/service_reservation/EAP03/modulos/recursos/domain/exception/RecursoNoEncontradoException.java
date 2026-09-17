package com.service_reservation.EAP03.modulos.recursos.domain.exception;

public class RecursoNoEncontradoException extends RecursoDominioException {
    public RecursoNoEncontradoException(Integer id) {
        super("No se encontró el recurso con ID: " + id);
    }
}
