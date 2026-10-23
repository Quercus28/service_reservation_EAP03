package com.service_reservation.EAP03.modulos.recursos.domain.exception;

public class AsociacionNoEncontradaException extends RecursoDominioException {
    public AsociacionNoEncontradaException(Integer idServicio, Integer idRecurso) {
        super("El recurso " + idRecurso + " no está asociado al servicio " + idServicio + ".");
    }
}
