package com.service_reservation.EAP03.modulos.recursos.domain.exception;

public class ServicioNoExisteException extends RecursoDominioException {
    public ServicioNoExisteException(Integer idServicio) {
        super("No se encontró el servicio con ID: " + idServicio);
    }
}
