package com.service_reservation.EAP03.modulos.agendas_horarios.domain.ports.out;

import com.service_reservation.EAP03.modulos.agendas_horarios.domain.model.Agenda;

import java.util.Optional;

public interface IPersistenciaAgendasHorarios {
    Agenda guardar(Agenda agenda);
    Optional<Agenda> buscarPorIdYProveedor(Long agendaId, Integer proveedorId);
    Optional<Agenda> buscarAgendaActivaPorServicio(Integer servicioId);
    boolean existeAgendaActivaPorServicio(Integer servicioId);
}
