package com.service_reservation.EAP03.modulos.agendas_horarios.application;

import com.service_reservation.EAP03.modulos.agendas_horarios.domain.exception.AgendaNoEncontradaException;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.exception.DatosAgendaInvalidosException;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.model.Agenda;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.model.Horario;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.ports.in.IDisponibilidad;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.ports.in.IGestionHorarios;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.ports.out.IPersistenciaAgendasHorarios;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GestionHorariosService implements IGestionHorarios, IDisponibilidad {
    private static final Logger log = LoggerFactory.getLogger(GestionHorariosService.class);
    private final IPersistenciaAgendasHorarios persistencia;

    public GestionHorariosService(IPersistenciaAgendasHorarios persistencia) {
        this.persistencia = persistencia;
    }

    @Override
    @Transactional
    public Agenda agregarHorario(Long agendaId, Integer proveedorId, Horario horario) {
        Agenda agenda = obtenerAgendaPropia(agendaId, proveedorId);
        agenda.agregarHorario(horario);
        return persistencia.guardar(agenda);
    }

    @Override
    @Transactional
    public Agenda modificarHorario(Long agendaId, Integer proveedorId, Long horarioId, Horario horario) {
        Agenda agenda = obtenerAgendaPropia(agendaId, proveedorId);
        agenda.reemplazarHorario(horarioId, horario);
        return persistencia.guardar(agenda);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Horario> consultarHorariosDisponibles(Integer proveedorId, Integer servicioId) {
        if (proveedorId == null || servicioId == null) {
            throw new DatosAgendaInvalidosException("El proveedor y el servicio son obligatorios.");
        }
        return persistencia.buscarAgendaActivaPorServicio(servicioId)
                .filter(a -> proveedorId.equals(a.getProveedorId()))
                .orElseThrow(() -> new AgendaNoEncontradaException(servicioId.longValue()))
                .getHorarios();
    }

    private Agenda obtenerAgendaPropia(Long agendaId, Integer proveedorId) {
        if (agendaId == null || proveedorId == null) {
            throw new DatosAgendaInvalidosException("La agenda y el proveedor son obligatorios");
        }
        return persistencia.buscarPorIdYProveedor(agendaId, proveedorId)
        .orElseGet(() -> {
            log.warn("Intento de acceso a una agenda no perteneciente al proveedor autenticado. agendaId={}", agendaId);
            throw new AgendaNoEncontradaException(agendaId);
        });
    }
}
