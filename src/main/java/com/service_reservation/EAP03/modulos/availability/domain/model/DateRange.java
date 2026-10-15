package com.service_reservation.EAP03.modulos.availability.domain.model;

import com.service_reservation.EAP03.modulos.availability.domain.exception.DisponibilidadInvalidaException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Value Object inmutable que representa un rango de fechas de consulta.
 */
public record DateRange(LocalDate startDate, LocalDate endDate) {

    public DateRange {
        Objects.requireNonNull(startDate, "startDate es obligatorio");
        Objects.requireNonNull(endDate, "endDate es obligatorio");

        if (endDate.isBefore(startDate)) {
            throw new DisponibilidadInvalidaException("endDate no puede ser anterior a startDate");
        }
    }

    public long totalDias() {
        return ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }
}
