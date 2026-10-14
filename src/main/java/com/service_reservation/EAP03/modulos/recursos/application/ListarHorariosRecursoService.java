package com.service_reservation.EAP03.modulos.recursos.application;

import com.service_reservation.EAP03.modulos.recursos.domain.exception.RecursoNoEncontradoException;
import com.service_reservation.EAP03.modulos.recursos.domain.model.HorarioRecurso;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.in.ListarHorariosRecursoUseCase;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.out.HorarioRecursoRepositoryPort;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.out.RecursoRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListarHorariosRecursoService implements ListarHorariosRecursoUseCase {

    private final RecursoRepositoryPort recursoRepositoryPort;
    private final HorarioRecursoRepositoryPort horarioRecursoRepositoryPort;

    public ListarHorariosRecursoService(RecursoRepositoryPort recursoRepositoryPort,
                                        HorarioRecursoRepositoryPort horarioRecursoRepositoryPort) {
        this.recursoRepositoryPort = recursoRepositoryPort;
        this.horarioRecursoRepositoryPort = horarioRecursoRepositoryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public List<HorarioRecurso> ejecutar(Integer idRecurso) {
        if (!recursoRepositoryPort.existePorId(idRecurso)) {
            throw new RecursoNoEncontradoException(idRecurso);
        }
        return horarioRecursoRepositoryPort.buscarPorRecurso(idRecurso);
    }
}
