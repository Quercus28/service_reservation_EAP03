package com.service_reservation.EAP03.modulos.reportes.domain.exception;

/**
 * Se lanza cuando el período de reporte tiene fechas incoherentes
 * (endDate anterior a startDate). Excepción de dominio puro.
 */
public class PeriodoReporteInvalidoException extends RuntimeException {

    public PeriodoReporteInvalidoException(String mensaje) {
        super(mensaje);
    }
}
