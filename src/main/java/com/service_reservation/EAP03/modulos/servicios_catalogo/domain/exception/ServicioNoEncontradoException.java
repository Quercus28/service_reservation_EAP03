package com.service_reservation.EAP03.modulos.servicios_catalogo.domain.exception;

public class ServicioNoEncontradoException extends DomainException {

    public ServicioNoEncontradoException(Integer id) {
        super("No se encontró el servicio con ID: " + id);
    }

    public ServicioNoEncontradoException(String message) {
        super(message);
    }
}
