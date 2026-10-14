package com.service_reservation.EAP03.modulos.recursos.application;

import com.service_reservation.EAP03.modulos.recursos.domain.exception.BloqueoNoEncontradoException;
import com.service_reservation.EAP03.modulos.recursos.domain.model.BloqueoRecurso;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.in.EliminarBloqueoRecursoUseCase;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.out.BloqueoRecursoRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EliminarBloqueoRecursoService implements EliminarBloqueoRecursoUseCase {

    private final BloqueoRecursoRepositoryPort bloqueoRecursoRepositoryPort;

    public EliminarBloqueoRecursoService(BloqueoRecursoRepositoryPort bloqueoRecursoRepositoryPort) {
        this.bloqueoRecursoRepositoryPort = bloqueoRecursoRepositoryPort;
    }

    @Override
    @Transactional
    public void ejecutar(Integer idRecurso, Integer idBloqueo) {
        // Un bloqueo de otro recurso se trata igual que uno inexistente (404), para no
        // revelar ids ajenos a quien solo tiene permiso sobre idRecurso.
        BloqueoRecurso bloqueo = bloqueoRecursoRepositoryPort.buscarPorId(idBloqueo)
                .filter(b -> b.getIdRecurso().equals(idRecurso))
                .orElseThrow(() -> new BloqueoNoEncontradoException(idRecurso, idBloqueo));

        bloqueoRecursoRepositoryPort.eliminar(bloqueo.getId());
    }
}
