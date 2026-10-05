package com.service_reservation.EAP03.modulos.agendas_horarios.domain.exception;

public class AgendaDuplicadaException extends RuntimeException {
    public AgendaDuplicadaException(Integer servicioId) {
        super("El servicio " + servicioId + " ya tiene una agenda activa.");
    }
}
