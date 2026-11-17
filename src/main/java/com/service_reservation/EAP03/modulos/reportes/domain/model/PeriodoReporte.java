package com.service_reservation.EAP03.modulos.reportes.domain.model;

import com.service_reservation.EAP03.modulos.reportes.domain.exception.PeriodoReporteInvalidoException;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Value Object que encapsula el rango de fechas de un reporte.
 * Garantiza que startDate no sea posterior a endDate en el momento
 * de construcción, centralizando la regla de negocio en el dominio.
 */
public record PeriodoReporte(LocalDate startDate, LocalDate endDate) {

    public PeriodoReporte {
        Objects.requireNonNull(startDate, "La fecha de inicio es obligatoria");
        Objects.requireNonNull(endDate,   "La fecha de fin es obligatoria");

        if (endDate.isBefore(startDate)) {
            throw new PeriodoReporteInvalidoException(
                    "La fecha de fin no puede ser anterior a la fecha de inicio"
            );
        }
    }
}
