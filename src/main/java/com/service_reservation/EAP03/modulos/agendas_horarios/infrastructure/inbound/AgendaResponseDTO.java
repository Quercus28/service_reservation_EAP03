package com.service_reservation.EAP03.modulos.agendas_horarios.infrastructure.inbound;

import com.service_reservation.EAP03.modulos.agendas_horarios.domain.model.Agenda;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.model.Horario;

import java.time.LocalTime;
import java.util.List;

public record AgendaResponseDTO(Long id, Integer proveedorId, Integer servicioId, boolean activa, List<HorarioResponseDTO> horarios) {
    public static AgendaResponseDTO from(Agenda agenda) {
        return new AgendaResponseDTO(
                agenda.getId(),
                agenda.getProveedorId(),
                agenda.getServicioId(),
                agenda.isActiva(),
                agenda.getHorarios().stream().map(HorarioResponseDTO::from).toList()
        );
    }

    public record HorarioResponseDTO(Long id, String diaSemana, LocalTime horaInicio, LocalTime horaFin) {
        static HorarioResponseDTO from(Horario horario) {
            return new HorarioResponseDTO(horario.id(), horario.diaSemana().name(), horario.horaInicio(), horario.horaFin());
        }
    }
}
