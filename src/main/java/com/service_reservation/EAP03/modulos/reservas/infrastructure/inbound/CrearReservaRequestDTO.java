package com.service_reservation.EAP03.modulos.reservas.infrastructure.inbound;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record CrearReservaRequestDTO(

        @NotNull(message = "El servicio es obligatorio")
        Integer idServicio,

        @NotNull(message = "La fecha y hora de inicio son obligatorias")
        LocalDateTime fechaInicio,

        // Mismo criterio que el motivo de los bloqueos de recursos: solo letras (incluidas
        // vocales con tilde, ü y ñ), números, espacios y . , - ( ), para rechazar entradas
        // con comandos, etiquetas HTML o saltos de línea.
        @Size(max = 500, message = "Las notas no pueden superar los 500 caracteres")
        @Pattern(regexp = "[A-Za-z\\u00C1\\u00C9\\u00CD\\u00D3\\u00DA\\u00DC\\u00D1\\u00E1\\u00E9\\u00ED\\u00F3\\u00FA\\u00FC\\u00F10-9 .,()\\-]*",
                message = "Las notas solo pueden contener letras, números, espacios y los caracteres . , - ( )")
        String notas
) {}
