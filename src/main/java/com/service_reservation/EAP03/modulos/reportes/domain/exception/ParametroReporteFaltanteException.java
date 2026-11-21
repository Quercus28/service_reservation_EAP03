package com.service_reservation.EAP03.modulos.reportes.domain.exception;

/** Falta una fecha obligatoria del reporte (startDate o endDate), o llegó vacía. */
public class ParametroReporteFaltanteException extends RuntimeException {

    public ParametroReporteFaltanteException(String parametro) {
        super("El parámetro '" + parametro + "' es obligatorio");
    }
}
