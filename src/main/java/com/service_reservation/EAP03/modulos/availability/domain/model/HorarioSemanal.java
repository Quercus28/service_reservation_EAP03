package com.service_reservation.EAP03.modulos.availability.domain.model;

import com.service_reservation.EAP03.modulos.agendas_horarios.domain.model.DiaSemana;
import com.service_reservation.EAP03.modulos.availability.domain.exception.DisponibilidadInvalidaException;

import java.time.LocalTime;
import java.util.Objects;

/**
 * Value Object inmutable que representa la ventana de atención semanal definida por el proveedor.
 */
public record HorarioSemanal(DiaSemana diaSemana, LocalTime horaInicio, LocalTime horaFin) {

    public HorarioSemanal {
        Objects.requireNonNull(diaSemana, "diaSemana es obligatorio");
        Objects.requireNonNull(horaInicio, "horaInicio es obligatoria");
        Objects.requireNonNull(horaFin, "horaFin es obligatoria");

        if (!horaFin.isAfter(horaInicio)) {
            throw new DisponibilidadInvalidaException("horaFin debe ser posterior a horaInicio");
        }
    }
}
