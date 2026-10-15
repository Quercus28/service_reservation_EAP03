package com.service_reservation.EAP03.modulos.availability.domain.exception;

public class ServicioParaDisponibilidadNoEncontradoException extends RuntimeException {
    public ServicioParaDisponibilidadNoEncontradoException(Integer servicioId) {
        super("Servicio con ID " + servicioId + " no fue encontrado o no está disponible");
    }
}
