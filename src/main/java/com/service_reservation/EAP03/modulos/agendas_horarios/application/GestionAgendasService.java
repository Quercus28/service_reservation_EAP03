package com.service_reservation.EAP03.modulos.agendas_horarios.application;

import com.service_reservation.EAP03.modulos.agendas_horarios.domain.model.Agenda;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.ports.in.IGestionAgendas;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.ports.out.IPersistenciaAgendasHorarios;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.ports.out.ProveedorIdentidadPort;
import org.springframework.stereotype.Service;

@Service
public class GestionAgendasService implements IGestionAgendas {
    private final IPersistenciaAgendasHorarios persistencia;
    private final ProveedorIdentidadPort proveedorIdentidad;

    public GestionAgendasService(IPersistenciaAgendasHorarios persistencia, ProveedorIdentidadPort proveedorIdentidad) {
        this.persistencia = persistencia;
        this.proveedorIdentidad = proveedorIdentidad;
    }

    @Override
    public Agenda crearAgenda(Long proveedorId) {
        validarProveedor(proveedorId);
        return persistencia.guardar(new Agenda(proveedorId));
    }

    @Override
    public Agenda consultarAgenda(Long agendaId, Long proveedorId) {
        validarProveedor(proveedorId);
        return persistencia.buscarPorIdYProveedor(agendaId, proveedorId);
    }

    public Long obtenerProveedorId(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("El usuario autenticado es obligatorio");
        }
        return proveedorIdentidad.obtenerProveedorIdPorEmail(email);
    }

    private void validarProveedor(Long proveedorId) {
        if (proveedorId == null) {
            throw new IllegalArgumentException("El proveedor es obligatorio");
        }
    }
}