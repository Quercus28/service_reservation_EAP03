package com.service_reservation.EAP03.modulos.recursos.domain.exception;

import com.service_reservation.EAP03.modulos.recursos.domain.model.BloqueoRecurso;
import com.service_reservation.EAP03.modulos.recursos.domain.model.RecursoPrestado;

public class BloqueoEnConflictoException extends RecursoDominioException {

    private BloqueoEnConflictoException(String mensaje) {
        super(mensaje);
    }

    public static BloqueoEnConflictoException conPrestamo(RecursoPrestado prestamo) {
        return new BloqueoEnConflictoException("El bloqueo choca con un préstamo existente del recurso "
                + prestamo.getIdRecurso() + " (préstamo " + prestamo.getId() + ", de "
                + prestamo.getFechaInicio() + " a " + prestamo.getFechaFin() + ").");
    }

    public static BloqueoEnConflictoException conBloqueo(BloqueoRecurso bloqueo) {
        return new BloqueoEnConflictoException("El bloqueo choca con otro bloqueo del recurso "
                + bloqueo.getIdRecurso() + " (bloqueo " + bloqueo.getId() + ", de "
                + bloqueo.getFechaInicio() + " a " + bloqueo.getFechaFin() + ").");
    }
}
