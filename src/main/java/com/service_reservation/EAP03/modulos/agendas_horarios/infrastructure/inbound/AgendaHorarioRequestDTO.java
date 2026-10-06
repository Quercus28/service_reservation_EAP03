package com.service_reservation.EAP03.modulos.agendas_horarios.infrastructure.inbound;

import com.service_reservation.EAP03.modulos.agendas_horarios.domain.model.DiaSemana;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalTime;

public record AgendaHorarioRequestDTO(
        @NotNull(message = "El día de la semana es obligatorio")
        DiaSemana diaSemana,

        @NotBlank(message = "La hora de inicio es obligatoria")
        @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "La hora de inicio debe tener formato HH:mm")
        String horaInicio,

        @NotBlank(message = "La hora de fin es obligatoria")
        @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "La hora de fin debe tener formato HH:mm")
        String horaFin
) {
    public LocalTime horaInicioLocal() { return LocalTime.parse(horaInicio); }
    public LocalTime horaFinLocal() { return LocalTime.parse(horaFin); }
}
