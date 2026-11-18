package com.service_reservation.EAP03.modulos.reservas.infrastructure.inbound;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

// El estado llega como texto y se valida aquí, así un valor vacío o que no existe responde
// 400 nombrando el campo "estado" (igual que el resto de validaciones de entrada).
public record CambiarEstadoReservaRequestDTO(
        @NotBlank(message = "El estado es obligatorio")
        @Pattern(regexp = "RESERVADO|CONFIRMADO|EN_PROCESO|COMPLETADO|CANCELADO|NO_ASISTIO",
                message = "El estado no existe. Valores posibles: RESERVADO, CONFIRMADO, EN_PROCESO, COMPLETADO, CANCELADO, NO_ASISTIO")
        String estado
) {}
