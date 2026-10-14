package com.service_reservation.EAP03.modulos.recursos.domain.exception;

import java.time.LocalDateTime;

public class RecursoNoOperativoException extends RecursoDominioException {
    public RecursoNoOperativoException(Integer idRecurso, LocalDateTime desde, LocalDateTime hasta) {
        super("El recurso " + idRecurso + " no está operativo entre " + desde + " y " + hasta
                + ": el rango queda fuera de su horario o dentro de un bloqueo.");
    }
}
