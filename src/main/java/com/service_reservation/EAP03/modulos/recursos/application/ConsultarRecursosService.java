package com.service_reservation.EAP03.modulos.recursos.application;

import com.service_reservation.EAP03.modulos.recursos.domain.model.Recurso;
import com.service_reservation.EAP03.modulos.recursos.domain.model.ResultadoPaginado;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.in.ConsultarRecursosUseCase;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.out.RecursoRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConsultarRecursosService implements ConsultarRecursosUseCase {

    private final RecursoRepositoryPort recursoRepositoryPort;

    public ConsultarRecursosService(RecursoRepositoryPort recursoRepositoryPort) {
        this.recursoRepositoryPort = recursoRepositoryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public ResultadoPaginado<Recurso> ejecutar(Long idProveedor, int pagina, int tamano) {
        return recursoRepositoryPort.buscarActivosPorProveedor(idProveedor, pagina, tamano);
    }
}
