package com.service_reservation.EAP03.modulos.recursos.application;

import com.service_reservation.EAP03.modulos.recursos.domain.exception.ServicioNoExisteException;
import com.service_reservation.EAP03.modulos.recursos.domain.model.ServicioRecurso;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.in.ListarRecursosDeServicioUseCase;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.out.ServicioRecursoRepositoryPort;
import com.service_reservation.EAP03.modulos.servicios_catalogo.application.usecase.ObtenerServicioPorIdUseCase;
import com.service_reservation.EAP03.modulos.servicios_catalogo.domain.exception.ServicioNoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListarRecursosDeServicioService implements ListarRecursosDeServicioUseCase {

    private final ObtenerServicioPorIdUseCase obtenerServicioPorIdUseCase;
    private final ServicioRecursoRepositoryPort servicioRecursoRepositoryPort;

    public ListarRecursosDeServicioService(ObtenerServicioPorIdUseCase obtenerServicioPorIdUseCase,
                                           ServicioRecursoRepositoryPort servicioRecursoRepositoryPort) {
        this.obtenerServicioPorIdUseCase = obtenerServicioPorIdUseCase;
        this.servicioRecursoRepositoryPort = servicioRecursoRepositoryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServicioRecurso> ejecutar(Integer idServicio) {
        try {
            obtenerServicioPorIdUseCase.ejecutar(idServicio);
        } catch (ServicioNoEncontradoException e) {
            throw new ServicioNoExisteException(idServicio);
        }
        return servicioRecursoRepositoryPort.buscarPorServicio(idServicio);
    }
}
