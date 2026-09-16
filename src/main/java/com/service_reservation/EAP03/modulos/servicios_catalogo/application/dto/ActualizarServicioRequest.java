package com.service_reservation.EAP03.modulos.servicios_catalogo.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ActualizarServicioRequest(
        @NotBlank(message = "El nombre del servicio no puede estar vacío")
        @Size(max = 150, message = "El nombre del servicio no puede superar los 150 caracteres")
        String nombre,

        @NotNull(message = "La duración en minutos es obligatoria")
        @Min(value = 1, message = "La duración debe ser mayor a 0 minutos")
        Integer duracionMinutos
) {
}
