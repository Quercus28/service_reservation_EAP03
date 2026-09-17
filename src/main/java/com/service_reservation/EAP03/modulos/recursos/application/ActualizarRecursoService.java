package com.service_reservation.EAP03.modulos.recursos.application;

import com.service_reservation.EAP03.modulos.recursos.domain.exception.DatosInvalidosException;
import com.service_reservation.EAP03.modulos.recursos.domain.exception.RecursoNoEncontradoException;
import com.service_reservation.EAP03.modulos.recursos.domain.model.ActualizarRecursoComando;
import com.service_reservation.EAP03.modulos.recursos.domain.model.Recurso;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.in.ActualizarRecursoUseCase;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.out.RecursoRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ActualizarRecursoService implements ActualizarRecursoUseCase {

    private final RecursoRepositoryPort recursoRepositoryPort;

    public ActualizarRecursoService(RecursoRepositoryPort recursoRepositoryPort) {
        this.recursoRepositoryPort = recursoRepositoryPort;
    }

    @Override
    @Transactional
    public Recurso ejecutar(ActualizarRecursoComando comando) {
        Recurso recurso = recursoRepositoryPort.buscarPorId(comando.id())
                .orElseThrow(() -> new RecursoNoEncontradoException(comando.id()));

        if (!recurso.estaActivo()) {
            throw new DatosInvalidosException("No se puede actualizar un recurso desactivado");
        }

        recurso.actualizarDatos(comando.nombre(), comando.precioUnitario(), comando.stock());

        return recursoRepositoryPort.guardar(recurso);
    }
}
