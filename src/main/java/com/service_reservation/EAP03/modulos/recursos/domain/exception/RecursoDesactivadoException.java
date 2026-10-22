package com.service_reservation.EAP03.modulos.recursos.domain.exception;

public class RecursoDesactivadoException extends RecursoDominioException {
    public RecursoDesactivadoException(Integer idRecurso) {
        super("El recurso " + idRecurso + " está desactivado y no se puede asociar a un servicio.");
    }
}
