package com.service_reservation.EAP03.modulos.recursos.domain.ports.in;

public interface EliminarHorarioRecursoUseCase {
    void ejecutar(Integer idRecurso, Integer idHorario);
}
