package com.service_reservation.EAP03.modulos.recursos.domain.exception;

public class RecursoYaAsociadoException extends RecursoDominioException {
    public RecursoYaAsociadoException(Integer idRecurso, Integer idServicio) {
        super("El recurso " + idRecurso + " ya está asociado al servicio " + idServicio + ".");
    }
}
