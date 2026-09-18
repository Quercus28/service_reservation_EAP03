package com.service_reservation.EAP03.modulos.agendas_horarios.infrastructure.outbound.adapter;

import com.service_reservation.EAP03.modulos.agendas_horarios.application.PersistenciaAgendasHorariosNoDisponibleException;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.model.Agenda;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.ports.out.IPersistenciaAgendasHorarios;
import org.springframework.stereotype.Component;

/**
 * Adaptador explícito de limitación académica.
 * No crea ni modifica tablas y no simula una persistencia alternativa.
 */
@Component
public class PersistenciaAgendasHorariosNoDisponibleAdapter implements IPersistenciaAgendasHorarios {
    @Override public Agenda guardar(Agenda agenda) { throw new PersistenciaAgendasHorariosNoDisponibleException(); }
    @Override public Agenda buscarPorId(Long agendaId) { throw new PersistenciaAgendasHorariosNoDisponibleException(); }
    @Override public Agenda buscarPorIdYProveedor(Long agendaId, Long proveedorId) { throw new PersistenciaAgendasHorariosNoDisponibleException(); }
}
