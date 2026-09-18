package com.service_reservation.EAP03.modulos.recursos.application;

import com.service_reservation.EAP03.modulos.recursos.domain.exception.RecursoDuplicadoException;
import com.service_reservation.EAP03.modulos.recursos.domain.model.CrearRecursoComando;
import com.service_reservation.EAP03.modulos.recursos.domain.model.Recurso;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.in.CrearRecursoUseCase;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.out.RecursoRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CrearRecursoService implements CrearRecursoUseCase {

    private final RecursoRepositoryPort recursoRepositoryPort;

    public CrearRecursoService(RecursoRepositoryPort recursoRepositoryPort) {
        this.recursoRepositoryPort = recursoRepositoryPort;
    }

    @Override
    @Transactional
    public Recurso ejecutar(CrearRecursoComando comando) {
        if (recursoRepositoryPort.existeNombreActivo(comando.idProveedor(), comando.nombre(), null)) {
            throw new RecursoDuplicadoException(comando.nombre());
        }

        Recurso recurso = Recurso.nuevo(
                comando.idProveedor(),
                comando.nombre(),
                comando.precioUnitario(),
                comando.stock()
        );
        return recursoRepositoryPort.guardar(recurso);
    }
}
