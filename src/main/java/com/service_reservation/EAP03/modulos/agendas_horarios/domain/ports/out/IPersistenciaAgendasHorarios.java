package com.service_reservation.EAP03.modulos.agendas_horarios.domain.ports.out;

import com.service_reservation.EAP03.modulos.agendas_horarios.domain.model.Agenda;

public interface IPersistenciaAgendasHorarios {
    Agenda guardar(Agenda agenda);
    Agenda buscarPorId(Long agendaId);
    Agenda buscarPorIdYProveedor(Long agendaId, Long proveedorId);
}
