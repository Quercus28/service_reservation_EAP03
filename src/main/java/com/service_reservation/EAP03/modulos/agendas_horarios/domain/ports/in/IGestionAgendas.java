package com.service_reservation.EAP03.modulos.agendas_horarios.domain.ports.in;

import com.service_reservation.EAP03.modulos.agendas_horarios.domain.model.Agenda;

public interface IGestionAgendas {
    Agenda crearAgenda(Long proveedorId);
    Agenda consultarAgenda(Long agendaId, Long proveedorId);
}
