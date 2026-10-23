package com.service_reservation.EAP03.modulos.reservas.domain.exception;

// El cliente pidió como extra un recurso que el servicio no ofrece como opcional.
public class RecursoOpcionalInvalidoException extends ReservaDominioException {
    public RecursoOpcionalInvalidoException(String mensaje) {
        super(mensaje);
    }
}
