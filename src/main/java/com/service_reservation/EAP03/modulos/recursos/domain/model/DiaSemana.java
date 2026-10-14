package com.service_reservation.EAP03.modulos.recursos.domain.model;

import java.time.DayOfWeek;

// Propio del módulo recursos: no se reutiliza el enum de agendas_horarios para no acoplar módulos.
public enum DiaSemana {
    LUNES,
    MARTES,
    MIERCOLES,
    JUEVES,
    VIERNES,
    SABADO,
    DOMINGO;

    public static DiaSemana desde(DayOfWeek dia) {
        return switch (dia) {
            case MONDAY -> LUNES;
            case TUESDAY -> MARTES;
            case WEDNESDAY -> MIERCOLES;
            case THURSDAY -> JUEVES;
            case FRIDAY -> VIERNES;
            case SATURDAY -> SABADO;
            case SUNDAY -> DOMINGO;
        };
    }
}
