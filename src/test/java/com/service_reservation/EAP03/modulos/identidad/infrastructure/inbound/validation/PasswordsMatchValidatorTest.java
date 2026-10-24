package com.service_reservation.EAP03.modulos.identidad.infrastructure.inbound.validation;

import com.service_reservation.EAP03.modulos.identidad.infrastructure.inbound.RegistroUsuarioRequestDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordsMatchValidatorTest {

    private final PasswordsMatchValidator validator = new PasswordsMatchValidator();

    private static RegistroUsuarioRequestDTO dto(String password, String confirmar) {
        return new RegistroUsuarioRequestDTO("ana@test.com", password, confirmar, "CLIENTE", "Ana", "3001234567", "CC1");
    }

    @Test
    @DisplayName("Es válido cuando la contraseña y su confirmación coinciden")
    void matchingPasswords() {
        assertTrue(validator.isValid(dto("Clave123", "Clave123"), null));
    }

    @Test
    @DisplayName("No es válido cuando la contraseña y su confirmación difieren")
    void differentPasswords() {
        assertFalse(validator.isValid(dto("Clave123", "Clave124"), null));
    }

    @Test
    @DisplayName("No es válido cuando el DTO es nulo")
    void nullDto() {
        assertFalse(validator.isValid(null, null));
    }

    @Test
    @DisplayName("No es válido cuando falta la contraseña o su confirmación")
    void missingPasswordOrConfirmation() {
        assertFalse(validator.isValid(dto(null, "Clave123"), null));
        assertFalse(validator.isValid(dto("Clave123", null), null));
    }
}
