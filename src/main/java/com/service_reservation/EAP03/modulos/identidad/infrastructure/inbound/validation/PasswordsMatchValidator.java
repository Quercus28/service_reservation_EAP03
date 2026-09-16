package com.service_reservation.EAP03.modulos.identidad.infrastructure.inbound.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import com.service_reservation.EAP03.modulos.identidad.infrastructure.inbound.RegistroUsuarioRequestDTO;

public class PasswordsMatchValidator implements ConstraintValidator<PasswordsMatch, RegistroUsuarioRequestDTO> {
    @Override
    public boolean isValid(RegistroUsuarioRequestDTO dto, ConstraintValidatorContext context) {
        if (dto == null || dto.password() == null || dto.confirmarPassword() == null) {
            return false;
        }
        return dto.password().equals(dto.confirmarPassword());
    }
}