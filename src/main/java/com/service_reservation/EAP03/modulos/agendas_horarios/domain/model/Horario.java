package com.service_reservation.EAP03.modulos.agendas_horarios.domain.model;

import com.service_reservation.EAP03.modulos.agendas_horarios.domain.exception.DatosAgendaInvalidosException;

import java.time.LocalTime;
import java.util.Objects;

public record Horario(Long id, DiaSemana diaSemana, LocalTime horaInicio, LocalTime horaFin) {
    public Horario {
        Objects.requireNonNull(diaSemana, "El día de la semana es obligatorio");
        Objects.requireNonNull(horaInicio, "La hora de inicio es obligatoria");
        Objects.requireNonNull(horaFin, "La hora de fin es obligatoria");

        if (!horaFin.isAfter(horaInicio)) {
            throw new DatosAgendaInvalidosException("La hora de finalización debe ser posterior a la hora de inicio");
        }
    }

    public Horario(DiaSemana diaSemana, LocalTime horaInicio, LocalTime horaFin) {
        this(null, diaSemana, horaInicio, horaFin);
    }

    public boolean seSuperponeCon(Horario otro) {
        if (otro == null || diaSemana != otro.diaSemana) {
            return false;
        }
        return horaInicio.isBefore(otro.horaFin) && horaFin.isAfter(otro.horaInicio);
    }
}
