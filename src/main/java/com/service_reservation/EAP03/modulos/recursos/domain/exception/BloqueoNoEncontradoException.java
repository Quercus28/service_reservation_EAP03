package com.service_reservation.EAP03.modulos.recursos.domain.exception;

public class BloqueoNoEncontradoException extends RecursoDominioException {
    public BloqueoNoEncontradoException(Integer idRecurso, Integer idBloqueo) {
        super("No se encontró el bloqueo con ID " + idBloqueo + " para el recurso " + idRecurso);
    }
}
