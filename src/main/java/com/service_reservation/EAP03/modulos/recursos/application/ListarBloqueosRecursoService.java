package com.service_reservation.EAP03.modulos.recursos.application;

import com.service_reservation.EAP03.modulos.recursos.domain.exception.DatosInvalidosException;
import com.service_reservation.EAP03.modulos.recursos.domain.exception.RecursoNoEncontradoException;
import com.service_reservation.EAP03.modulos.recursos.domain.model.BloqueoRecurso;
import com.service_reservation.EAP03.modulos.recursos.domain.model.ResultadoPaginado;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.in.ListarBloqueosRecursoUseCase;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.out.BloqueoRecursoRepositoryPort;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.out.RecursoRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListarBloqueosRecursoService implements ListarBloqueosRecursoUseCase {

    private final RecursoRepositoryPort recursoRepositoryPort;
    private final BloqueoRecursoRepositoryPort bloqueoRecursoRepositoryPort;

    public ListarBloqueosRecursoService(RecursoRepositoryPort recursoRepositoryPort,
                                        BloqueoRecursoRepositoryPort bloqueoRecursoRepositoryPort) {
        this.recursoRepositoryPort = recursoRepositoryPort;
        this.bloqueoRecursoRepositoryPort = bloqueoRecursoRepositoryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public ResultadoPaginado<BloqueoRecurso> ejecutar(Integer idRecurso, int pagina, int tamano) {
        if (pagina < 0 || tamano < 1) {
            throw new DatosInvalidosException("pagina debe ser >= 0 y tamano debe ser >= 1");
        }
        if (!recursoRepositoryPort.existePorId(idRecurso)) {
            throw new RecursoNoEncontradoException(idRecurso);
        }
        return bloqueoRecursoRepositoryPort.buscarPorRecurso(idRecurso, pagina, tamano);
    }
}
