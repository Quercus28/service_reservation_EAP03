package com.service_reservation.EAP03.modulos.identidad.infrastructure.inbound;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CrearAdministradorRequestDTO(
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Formato de correo electrónico inválido")
    String email,

    @NotBlank(message = "La contraseña es obligatoria")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[a-z]).{8,}$",
             message = "La contraseña debe tener mínimo 8 caracteres, incluir números y mayúsculas")
    String password
) {}