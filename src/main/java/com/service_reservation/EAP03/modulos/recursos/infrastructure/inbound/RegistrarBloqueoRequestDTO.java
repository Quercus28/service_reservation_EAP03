package com.service_reservation.EAP03.modulos.recursos.infrastructure.inbound;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record RegistrarBloqueoRequestDTO(

        @NotNull(message = "La fecha de inicio es obligatoria")
        LocalDateTime fechaInicio,

        @NotNull(message = "La fecha de fin es obligatoria")
        LocalDateTime fechaFin,

        // Solo letras (incluidas vocales con tilde, ü y ñ), números, espacios y . , - ( )
        // para rechazar entradas con comandos, etiquetas HTML o saltos de línea.
        @Size(max = 255, message = "El motivo no puede superar los 255 caracteres")
        @Pattern(regexp = "[A-Za-z\\u00C1\\u00C9\\u00CD\\u00D3\\u00DA\\u00DC\\u00D1\\u00E1\\u00E9\\u00ED\\u00F3\\u00FA\\u00FC\\u00F10-9 .,()\\-]*",
                message = "El motivo solo puede contener letras, números, espacios y los caracteres . , - ( )")
        String motivo
) {}
