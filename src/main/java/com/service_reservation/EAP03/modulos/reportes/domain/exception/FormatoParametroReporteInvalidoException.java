package com.service_reservation.EAP03.modulos.reportes.domain.exception;

/** Una fecha del reporte no tiene el formato YYYY-MM-DD o no es una fecha real (por ejemplo 2026-13-45). */
public class FormatoParametroReporteInvalidoException extends RuntimeException {

    public FormatoParametroReporteInvalidoException(String parametro) {
        super("El parámetro '" + parametro + "' tiene un formato o valor inválido");
    }
}
