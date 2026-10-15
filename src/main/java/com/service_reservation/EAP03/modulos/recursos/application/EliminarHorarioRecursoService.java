package com.service_reservation.EAP03.modulos.recursos.application;

import com.service_reservation.EAP03.modulos.recursos.domain.exception.HorarioNoEncontradoException;
import com.service_reservation.EAP03.modulos.recursos.domain.model.HorarioRecurso;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.in.EliminarHorarioRecursoUseCase;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.out.HorarioRecursoRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EliminarHorarioRecursoService implements EliminarHorarioRecursoUseCase {

    private final HorarioRecursoRepositoryPort horarioRecursoRepositoryPort;

    public EliminarHorarioRecursoService(HorarioRecursoRepositoryPort horarioRecursoRepositoryPort) {
        this.horarioRecursoRepositoryPort = horarioRecursoRepositoryPort;
    }

    @Override
    @Transactional
    public void ejecutar(Integer idRecurso, Integer idHorario) {
        // Un horario de otro recurso se trata igual que uno inexistente (404), para no
        // revelar ids ajenos a quien solo tiene permiso sobre idRecurso.
        HorarioRecurso horario = horarioRecursoRepositoryPort.buscarPorId(idHorario)
                .filter(h -> h.getIdRecurso().equals(idRecurso))
                .orElseThrow(() -> new HorarioNoEncontradoException(idRecurso, idHorario));

        horarioRecursoRepositoryPort.eliminar(horario.getId());
    }
}
