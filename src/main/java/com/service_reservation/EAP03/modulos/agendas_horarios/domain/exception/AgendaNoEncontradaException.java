package com.service_reservation.EAP03.modulos.agendas_horarios.domain.exception;

public class AgendaNoEncontradaException extends RuntimeException {
    public AgendaNoEncontradaException(Long agendaId) {
        super("No existe una agenda activa con id " + agendaId + " para el proveedor autenticado.");
    }
}
