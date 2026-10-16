package com.service_reservation.EAP03.modulos.reservas.domain.exception;

import java.time.LocalDateTime;

public class FechaEnElPasadoException extends ReservaDominioException {
    public FechaEnElPasadoException(LocalDateTime fechaInicio) {
        super("No se puede reservar en el pasado: la fecha de inicio " + fechaInicio + " ya pasó.");
    }
}
