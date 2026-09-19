package com.service_reservation.EAP03.modulos.recursos.application;

import com.service_reservation.EAP03.modulos.recursos.domain.exception.RecursoNoEncontradoException;
import com.service_reservation.EAP03.modulos.recursos.domain.model.Recurso;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.in.DesactivarRecursoUseCase;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.out.RecursoRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DesactivarRecursoService implements DesactivarRecursoUseCase {

    private final RecursoRepositoryPort recursoRepositoryPort;

    public DesactivarRecursoService(RecursoRepositoryPort recursoRepositoryPort) {
        this.recursoRepositoryPort = recursoRepositoryPort;
    }

    @Override
    @Transactional
    public void ejecutar(Integer id) {
        Recurso recurso = recursoRepositoryPort.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(id));

        recurso.desactivar();

        recursoRepositoryPort.guardar(recurso);
    }
}
