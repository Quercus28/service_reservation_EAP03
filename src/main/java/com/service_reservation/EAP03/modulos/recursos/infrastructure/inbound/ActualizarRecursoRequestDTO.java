package com.service_reservation.EAP03.modulos.recursos.infrastructure.inbound;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ActualizarRecursoRequestDTO(

        @NotBlank(message = "El nombre del recurso es obligatorio")
        @Size(max = 150, message = "El nombre no puede superar los 150 caracteres")
        // Misma regla que el motivo del bloqueo: rechaza etiquetas HTML y comandos.
        @Pattern(regexp = "[A-Za-z\\u00C1\\u00C9\\u00CD\\u00D3\\u00DA\\u00DC\\u00D1\\u00E1\\u00E9\\u00ED\\u00F3\\u00FA\\u00FC\\u00F10-9 .,()\\-]*",
                message = "El nombre tiene caracteres no permitidos")
        String nombre,

        @NotNull(message = "El precio unitario es obligatorio")
        @DecimalMin(value = "0.0", message = "El precio unitario no puede ser negativo")
        BigDecimal precioUnitario,

        @NotNull(message = "El stock es obligatorio")
        @Min(value = 0, message = "El stock no puede ser negativo")
        Integer stock
) {}
