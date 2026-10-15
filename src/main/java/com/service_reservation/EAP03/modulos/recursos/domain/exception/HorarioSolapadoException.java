package com.service_reservation.EAP03.modulos.recursos.domain.exception;

import com.service_reservation.EAP03.modulos.recursos.domain.model.HorarioRecurso;

public class HorarioSolapadoException extends RecursoDominioException {
    public HorarioSolapadoException(HorarioRecurso existente) {
        super("El horario se solapa con otro ya registrado para el recurso " + existente.getIdRecurso()
                + ": " + existente.getDiaSemana() + " de " + existente.getHoraInicio()
                + " a " + existente.getHoraFin() + ".");
    }
}
