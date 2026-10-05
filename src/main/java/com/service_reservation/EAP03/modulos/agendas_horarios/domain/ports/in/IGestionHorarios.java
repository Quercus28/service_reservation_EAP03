package com.service_reservation.EAP03.modulos.agendas_horarios.domain.ports.in;

import com.service_reservation.EAP03.modulos.agendas_horarios.domain.model.Agenda;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.model.Horario;

public interface IGestionHorarios {
    Agenda agregarHorario(Long agendaId, Integer proveedorId, Horario horario);
    Agenda modificarHorario(Long agendaId, Integer proveedorId, Long horarioId, Horario horario);
}
