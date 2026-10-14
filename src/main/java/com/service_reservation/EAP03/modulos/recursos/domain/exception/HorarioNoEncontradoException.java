package com.service_reservation.EAP03.modulos.recursos.domain.exception;

public class HorarioNoEncontradoException extends RecursoDominioException {
    public HorarioNoEncontradoException(Integer idRecurso, Integer idHorario) {
        super("No se encontró el horario con ID " + idHorario + " para el recurso " + idRecurso);
    }
}
