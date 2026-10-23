package com.service_reservation.EAP03.modulos.recursos.application;

import com.service_reservation.EAP03.modulos.recursos.domain.exception.RecursoDeOtroProveedorException;
import com.service_reservation.EAP03.modulos.recursos.domain.exception.RecursoDesactivadoException;
import com.service_reservation.EAP03.modulos.recursos.domain.exception.RecursoNoEncontradoException;
import com.service_reservation.EAP03.modulos.recursos.domain.exception.RecursoYaAsociadoException;
import com.service_reservation.EAP03.modulos.recursos.domain.exception.ServicioNoExisteException;
import com.service_reservation.EAP03.modulos.recursos.domain.model.AsociarRecursoComando;
import com.service_reservation.EAP03.modulos.recursos.domain.model.Recurso;
import com.service_reservation.EAP03.modulos.recursos.domain.model.ServicioRecurso;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.in.AsociarRecursoAServicioUseCase;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.out.RecursoRepositoryPort;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.out.ServicioRecursoRepositoryPort;
import com.service_reservation.EAP03.modulos.servicios_catalogo.application.dto.ServicioResponse;
import com.service_reservation.EAP03.modulos.servicios_catalogo.application.usecase.ObtenerServicioPorIdUseCase;
import com.service_reservation.EAP03.modulos.servicios_catalogo.domain.exception.ServicioNoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AsociarRecursoAServicioService implements AsociarRecursoAServicioUseCase {

    private final ObtenerServicioPorIdUseCase obtenerServicioPorIdUseCase;
    private final RecursoRepositoryPort recursoRepositoryPort;
    private final ServicioRecursoRepositoryPort servicioRecursoRepositoryPort;

    public AsociarRecursoAServicioService(ObtenerServicioPorIdUseCase obtenerServicioPorIdUseCase,
                                          RecursoRepositoryPort recursoRepositoryPort,
                                          ServicioRecursoRepositoryPort servicioRecursoRepositoryPort) {
        this.obtenerServicioPorIdUseCase = obtenerServicioPorIdUseCase;
        this.recursoRepositoryPort = recursoRepositoryPort;
        this.servicioRecursoRepositoryPort = servicioRecursoRepositoryPort;
    }

    @Override
    @Transactional
    public ServicioRecurso ejecutar(AsociarRecursoComando comando) {
        // El servicio es del módulo de catálogo: se le pregunta a él, no a sus tablas.
        ServicioResponse servicio;
        try {
            servicio = obtenerServicioPorIdUseCase.ejecutar(comando.idServicio());
        } catch (ServicioNoEncontradoException e) {
            throw new ServicioNoExisteException(comando.idServicio());
        }

        Recurso recurso = recursoRepositoryPort.buscarPorId(comando.idRecurso())
                .orElseThrow(() -> new RecursoNoEncontradoException(comando.idRecurso()));

        if (!recurso.estaActivo()) {
            throw new RecursoDesactivadoException(recurso.getId());
        }

        // Un proveedor solo puede usar sus propios recursos en sus propios servicios.
        if (!recurso.getIdProveedor().equals(servicio.idProveedor())) {
            throw new RecursoDeOtroProveedorException(recurso.getId(), servicio.id());
        }

        if (servicioRecursoRepositoryPort.existePorServicioYRecurso(comando.idServicio(), comando.idRecurso())) {
            throw new RecursoYaAsociadoException(comando.idRecurso(), comando.idServicio());
        }

        return servicioRecursoRepositoryPort.guardar(ServicioRecurso.nuevo(
                comando.idServicio(),
                comando.idRecurso(),
                comando.cantidad()
        ));
    }
}
