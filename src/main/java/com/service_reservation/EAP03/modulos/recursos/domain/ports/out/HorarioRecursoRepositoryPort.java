package com.service_reservation.EAP03.modulos.recursos.domain.ports.out;

import com.service_reservation.EAP03.modulos.recursos.domain.model.HorarioRecurso;

import java.util.List;
import java.util.Optional;

public interface HorarioRecursoRepositoryPort {
    HorarioRecurso guardar(HorarioRecurso horario);
    Optional<HorarioRecurso> buscarPorId(Integer id);
    List<HorarioRecurso> buscarPorRecurso(Integer idRecurso);
    List<HorarioRecurso> buscarPorRecursos(List<Integer> idsRecurso);
    void eliminar(Integer id);
}
