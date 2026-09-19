package com.service_reservation.EAP03.modulos.agendas_horarios.infrastructure.inbound;

import com.service_reservation.EAP03.modulos.agendas_horarios.domain.model.Agenda;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.model.Horario;

import java.time.LocalTime;
import java.util.List;

public record AgendaResponseDTO(Long id, Long proveedorId, List<HorarioResponseDTO> horarios) {
    public static AgendaResponseDTO from(Agenda agenda) {
        return new AgendaResponseDTO(
                agenda.getId(),
                agenda.getProveedorId(),
                agenda.getHorarios().stream().map(HorarioResponseDTO::from).toList()
        );
    }

    public record HorarioResponseDTO(String diaSemana, LocalTime horaInicio, LocalTime horaFin) {
        static HorarioResponseDTO from(Horario horario) {
            return new HorarioResponseDTO(horario.diaSemana().name(), horario.horaInicio(), horario.horaFin());
        }
    }
}
