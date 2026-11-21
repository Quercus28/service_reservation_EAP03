package com.service_reservation.EAP03.modulos.reportes.domain.exception;

/**
 * La consulta del reporte trae un parámetro que no está en la lista blanca o uno repetido.
 * El mensaje al cliente es fijo; el motivo solo sirve para el log (nunca es texto del usuario).
 */
public class ConsultaReporteInvalidaException extends RuntimeException {

    public enum Motivo { PARAMETRO_DESCONOCIDO, PARAMETRO_REPETIDO }

    private final Motivo motivo;

    public ConsultaReporteInvalidaException(Motivo motivo) {
        super("La solicitud de consulta no es válida.");
        this.motivo = motivo;
    }

    public Motivo getMotivo() { return motivo; }
}
