package com.service_reservation.EAP03.modulos.reportes.domain.model;

import com.service_reservation.EAP03.modulos.reportes.domain.exception.PeriodoReporteInvalidoException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Value Object que encapsula el rango de fechas de un reporte.
 * Garantiza que startDate no sea posterior a endDate y que el período no pase de
 * {@value #MAX_DIAS} días (contando ambos extremos), centralizando la regla en el dominio.
 */
public record PeriodoReporte(LocalDate startDate, LocalDate endDate) {

    /** Días máximos del período, contando el día de inicio y el de fin. Un año bisiesto completo cabe. */
    public static final int MAX_DIAS = 366;

    public PeriodoReporte {
        Objects.requireNonNull(startDate, "La fecha de inicio es obligatoria");
        Objects.requireNonNull(endDate,   "La fecha de fin es obligatoria");

        if (endDate.isBefore(startDate)) {
            throw new PeriodoReporteInvalidoException(
                    "La fecha de fin no puede ser anterior a la fecha de inicio"
            );
        }
        if (ChronoUnit.DAYS.between(startDate, endDate) + 1 > MAX_DIAS) {
            throw new PeriodoReporteInvalidoException(
                    "El período no puede ser mayor a " + MAX_DIAS + " días"
            );
        }
    }
}
