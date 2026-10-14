package com.service_reservation.EAP03.modulos.identidad.infrastructure.inbound;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record Verificar2faLoginRequestDTO(
    @NotBlank(message = "El token temporal es obligatorio")
    String tokenTemporal,

    @NotBlank(message = "El código 2FA es obligatorio")
    @Pattern(regexp = "^[0-9]{6}$", message = "El código debe ser de 6 dígitos numéricos")
    String codigo
) {}
