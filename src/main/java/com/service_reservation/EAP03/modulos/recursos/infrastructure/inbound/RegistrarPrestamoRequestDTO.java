package com.service_reservation.EAP03.modulos.recursos.infrastructure.inbound;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RegistrarPrestamoRequestDTO(

        @NotNull(message = "El id del servicio prestado es obligatorio")
        Integer idServicioPrestado,

        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad debe ser mayor que cero")
        Integer cantidad,

        @NotNull(message = "La fecha de inicio es obligatoria")
        LocalDateTime fechaInicio,

        @NotNull(message = "La fecha de fin es obligatoria")
        LocalDateTime fechaFin,

        @NotNull(message = "El precio total es obligatorio")
        @DecimalMin(value = "0.0", message = "El precio total no puede ser negativo")
        BigDecimal precioTotal
) {}
