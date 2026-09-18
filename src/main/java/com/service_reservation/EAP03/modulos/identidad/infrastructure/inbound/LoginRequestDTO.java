package com.service_reservation.EAP03.modulos.identidad.infrastructure.inbound;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Formato de correo electrónico inválido")
    String email,

    @NotBlank(message = "La contraseña es obligatoria")
    String password
) {}
