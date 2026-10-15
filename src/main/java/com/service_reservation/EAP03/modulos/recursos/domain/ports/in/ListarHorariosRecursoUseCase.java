package com.service_reservation.EAP03.modulos.recursos.domain.ports.in;

import com.service_reservation.EAP03.modulos.recursos.domain.model.HorarioRecurso;

import java.util.List;

public interface ListarHorariosRecursoUseCase {
    List<HorarioRecurso> ejecutar(Integer idRecurso);
}
