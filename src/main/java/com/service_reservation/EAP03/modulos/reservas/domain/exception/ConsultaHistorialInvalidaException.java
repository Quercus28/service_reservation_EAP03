package com.service_reservation.EAP03.modulos.reservas.domain.exception;

/**
 * La consulta del historial trae parámetros no permitidos o con texto sospechoso.
 * El mensaje al cliente es fijo; el motivo solo sirve para el log (nunca es texto del usuario).
 */
public class ConsultaHistorialInvalidaException extends ReservaDominioException {

    public enum Motivo { PARAMETRO_DESCONOCIDO, PATRON_SOSPECHOSO, VALOR_INVALIDO }

    private final Motivo motivo;

    public ConsultaHistorialInvalidaException(Motivo motivo) {
        super("La solicitud de consulta no es válida.");
        this.motivo = motivo;
    }

    public Motivo getMotivo() { return motivo; }
}