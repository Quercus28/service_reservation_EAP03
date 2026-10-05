package com.service_reservation.EAP03.modulos.agendas_horarios.application;

import com.service_reservation.EAP03.modulos.agendas_horarios.domain.exception.AgendaDuplicadaException;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.exception.AgendaNoEncontradaException;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.exception.DatosAgendaInvalidosException;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.exception.ServicioNoPerteneceAlProveedorException;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.model.Agenda;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.ports.in.IGestionAgendas;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.ports.out.IPersistenciaAgendasHorarios;
import com.service_reservation.EAP03.modulos.servicios_catalogo.application.dto.ServicioResponse;
import com.service_reservation.EAP03.modulos.servicios_catalogo.application.usecase.ObtenerServicioPorIdUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GestionAgendasService implements IGestionAgendas {
    private static final Logger log = LoggerFactory.getLogger(GestionAgendasService.class);
    private final IPersistenciaAgendasHorarios persistencia;
    private final ObtenerServicioPorIdUseCase obtenerServicioPorId;

    public GestionAgendasService(IPersistenciaAgendasHorarios persistencia, ObtenerServicioPorIdUseCase obtenerServicioPorId) {
        this.persistencia = persistencia;
        this.obtenerServicioPorId = obtenerServicioPorId;
    }

    @Override
    @Transactional
    public Agenda crearAgenda(Integer proveedorId, Integer servicioId) {
        validarIds(proveedorId, servicioId);

        ServicioResponse servicio = obtenerServicioPorId.ejecutar(servicioId);
        if (servicio.idProveedor() == null || !proveedorId.equals(servicio.idProveedor())) {
            throw new ServicioNoPerteneceAlProveedorException(servicioId);
        }

        if (persistencia.existeAgendaActivaPorServicio(servicioId)) {
            throw new AgendaDuplicadaException(servicioId);
        }

        return persistencia.guardar(new Agenda(proveedorId, servicioId));
    }

    @Override
    @Transactional(readOnly = true)
    public Agenda consultarAgenda(Long agendaId, Integer proveedorId) {
        if (agendaId == null || proveedorId == null) {
            throw new DatosAgendaInvalidosException("La agenda y el proveedor son obligatorios.");
        }

        return persistencia.buscarPorIdYProveedor(agendaId, proveedorId)
                .orElseGet(() -> {
                    log.warn("Intento de acceso a una agenda no perteneciente al proveedor autenticado. agendaId={}", agendaId);
                    throw new AgendaNoEncontradaException(agendaId);
                });
    }

    private void validarIds(Integer proveedorId, Integer servicioId) {
        if (proveedorId == null) throw new DatosAgendaInvalidosException("El proveedor es obligatorio.");
        if (servicioId == null) throw new DatosAgendaInvalidosException("El servicio es obligatorio.");
    }
}