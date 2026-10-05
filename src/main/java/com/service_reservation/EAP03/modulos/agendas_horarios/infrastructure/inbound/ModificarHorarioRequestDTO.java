package com.service_reservation.EAP03.modulos.agendas_horarios.infrastructure.inbound;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record ModificarHorarioRequestDTO(
        @NotNull(message = "El id del horario es obligatorio")
        Long horarioId,
        @NotNull(message = "El nuevo horario es obligatorio")
        @Valid AgendaHorarioRequestDTO nuevoHorario
) {}
