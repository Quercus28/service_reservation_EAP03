package com.service_reservation.EAP03.modulos.recursos.application;

import com.service_reservation.EAP03.modulos.recursos.domain.exception.AsociacionNoEncontradaException;
import com.service_reservation.EAP03.modulos.recursos.domain.model.ServicioRecurso;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.in.QuitarRecursoDeServicioUseCase;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.out.ServicioRecursoRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QuitarRecursoDeServicioService implements QuitarRecursoDeServicioUseCase {

    private final ServicioRecursoRepositoryPort servicioRecursoRepositoryPort;

    public QuitarRecursoDeServicioService(ServicioRecursoRepositoryPort servicioRecursoRepositoryPort) {
        this.servicioRecursoRepositoryPort = servicioRecursoRepositoryPort;
    }

    @Override
    @Transactional
    public void ejecutar(Integer idServicio, Integer idRecurso) {
        ServicioRecurso asociacion = servicioRecursoRepositoryPort.buscarPorServicioYRecurso(idServicio, idRecurso)
                .orElseThrow(() -> new AsociacionNoEncontradaException(idServicio, idRecurso));

        servicioRecursoRepositoryPort.eliminar(asociacion.getId());
    }
}
