package com.service_reservation.EAP03.modulos.recursos.application;

import com.service_reservation.EAP03.modulos.recursos.domain.exception.RecursoNoEncontradoException;
import com.service_reservation.EAP03.modulos.recursos.domain.model.Recurso;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.in.ObtenerRecursoPorIdUseCase;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.out.RecursoRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ObtenerRecursoPorIdService implements ObtenerRecursoPorIdUseCase {

    private final RecursoRepositoryPort recursoRepositoryPort;

    public ObtenerRecursoPorIdService(RecursoRepositoryPort recursoRepositoryPort) {
        this.recursoRepositoryPort = recursoRepositoryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public Recurso ejecutar(Integer id) {
        return recursoRepositoryPort.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(id));
    }
}
