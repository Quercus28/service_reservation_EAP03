package com.service_reservation.EAP03.modulos.agendas_horarios.application;

import com.service_reservation.EAP03.modulos.agendas_horarios.domain.model.Agenda;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.model.Horario;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.ports.in.IDisponibilidad;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.ports.in.IGestionHorarios;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.ports.out.IPersistenciaAgendasHorarios;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GestionHorariosService implements IGestionHorarios, IDisponibilidad {
    private final IPersistenciaAgendasHorarios persistencia;

    public GestionHorariosService(IPersistenciaAgendasHorarios persistencia) {
        this.persistencia = persistencia;
    }

    @Override
    public Agenda agregarHorario(Long agendaId, Long proveedorId, Horario horario) {
        Agenda agenda = obtenerAgendaPropia(agendaId, proveedorId);
        agenda.agregarHorario(horario);
        return persistencia.guardar(agenda);
    }

    @Override
    public Agenda modificarHorario(Long agendaId, Long proveedorId, Horario horarioAnterior, Horario horarioNuevo) {
        Agenda agenda = obtenerAgendaPropia(agendaId, proveedorId);
        agenda.reemplazarHorario(horarioAnterior, horarioNuevo);
        return persistencia.guardar(agenda);
    }

    @Override
    public List<Horario> consultarHorariosDisponibles(Long proveedorId) {
        return obtenerAgendaDeProveedor(proveedorId).getHorarios();
    }

    private Agenda obtenerAgendaPropia(Long agendaId, Long proveedorId) {
        if (agendaId == null || proveedorId == null) {
            throw new IllegalArgumentException("La agenda y el proveedor son obligatorios");
        }
        return persistencia.buscarPorIdYProveedor(agendaId, proveedorId);
    }

    private Agenda obtenerAgendaDeProveedor(Long proveedorId) {
        if (proveedorId == null) {
            throw new IllegalArgumentException("El proveedor es obligatorio");
        }
        // El contrato de persistencia actual solo permite consultar una agenda por ID.
        // La consulta por proveedor queda bloqueada hasta disponer de la estructura de BD.
        throw new PersistenciaAgendasHorariosNoDisponibleException();
    }
}
