package com.service_reservation.EAP03.modulos.recursos.infrastructure.inbound;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record RegistrarHorarioRequestDTO(

        @NotNull(message = "El día de la semana es obligatorio")
        @Pattern(regexp = "LUNES|MARTES|MIERCOLES|JUEVES|VIERNES|SABADO|DOMINGO",
                message = "El día de la semana debe ser LUNES, MARTES, MIERCOLES, JUEVES, VIERNES, SABADO o DOMINGO")
        String diaSemana,

        @NotNull(message = "La hora de inicio es obligatoria")
        @Pattern(regexp = "([01]\\d|2[0-3]):[0-5]\\d", message = "La hora de inicio debe tener el formato HH:mm")
        String horaInicio,

        @NotNull(message = "La hora de fin es obligatoria")
        @Pattern(regexp = "([01]\\d|2[0-3]):[0-5]\\d", message = "La hora de fin debe tener el formato HH:mm")
        String horaFin
) {}
