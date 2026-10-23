package com.service_reservation.EAP03.modulos.servicios_catalogo.application.dto;

import com.service_reservation.EAP03.config.RejectUnknownFields;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// HU04 E1: un campo que no existe en el alta (p. ej. capacidadMaxima) se rechaza, no se ignora.
@RejectUnknownFields
public record CrearServicioRequest(
        @NotNull(message = "El id del proveedor es obligatorio")
        Integer idProveedor,

        @NotBlank(message = "El nombre del servicio no puede estar vacío")
        @Size(max = 150, message = "El nombre del servicio no puede superar los 150 caracteres")
        // Misma regla que el nombre del recurso: rechaza etiquetas HTML y comandos.
        @Pattern(regexp = "[A-Za-z\\u00C1\\u00C9\\u00CD\\u00D3\\u00DA\\u00DC\\u00D1\\u00E1\\u00E9\\u00ED\\u00F3\\u00FA\\u00FC\\u00F10-9 .,()'\\-]*",
                message = "El nombre del servicio tiene caracteres no permitidos")
        String nombre,

        @NotNull(message = "La duración en minutos es obligatoria")
        @Min(value = 1, message = "La duración debe ser mayor a 0 minutos")
        Integer duracionMinutos
) {
}
