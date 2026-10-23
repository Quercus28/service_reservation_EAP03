package com.service_reservation.EAP03.modulos.recursos.infrastructure.inbound;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AsociarRecursoRequestDTO(

        @NotNull(message = "El id del recurso es obligatorio")
        Integer idRecurso,

        @Min(value = 1, message = "La cantidad debe ser mayor que cero")
        Integer cantidad
) {}
