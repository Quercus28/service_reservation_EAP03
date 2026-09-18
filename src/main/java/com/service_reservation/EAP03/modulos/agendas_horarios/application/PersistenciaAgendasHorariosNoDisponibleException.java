package com.service_reservation.EAP03.modulos.agendas_horarios.application;

public class PersistenciaAgendasHorariosNoDisponibleException extends RuntimeException {
    public PersistenciaAgendasHorariosNoDisponibleException() {
        super("La persistencia de agendas y horarios no está disponible: el esquema PostgreSQL actual no contiene las estructuras requeridas y no puede modificarse en esta implementación académica.");
    }
}
