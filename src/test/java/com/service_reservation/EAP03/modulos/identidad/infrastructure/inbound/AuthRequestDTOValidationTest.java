package com.service_reservation.EAP03.modulos.identidad.infrastructure.inbound;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class AuthRequestDTOValidationTest {

    private static Validator validator;

    @BeforeAll
    static void createValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private static Set<String> fieldsWithErrors(Set<? extends ConstraintViolation<?>> violaciones) {
        return violaciones.stream().map(v -> v.getPropertyPath().toString()).collect(Collectors.toSet());
    }

    @Test
    @DisplayName("Un login con correo y contraseña válidos no produce violaciones")
    void validLogin() {
        assertTrue(validator.validate(new LoginRequestDTO("ana@test.com", "Clave123")).isEmpty());
    }

    @Test
    @DisplayName("Rechaza un login con correo inválido y contraseña vacía")
    void invalidLogin() {
        // Act
        Set<String> campos = fieldsWithErrors(validator.validate(new LoginRequestDTO("no-es-correo", "")));

        // Assert
        assertEquals(Set.of("email", "password"), campos);
    }

    @Test
    @DisplayName("Acepta un código de activación de 6 dígitos")
    void validActivationCode() {
        assertTrue(validator.validate(new Activar2faRequestDTO("123456")).isEmpty());
    }

    @ParameterizedTest(name = "codigo = [{0}]")
    @ValueSource(strings = {"", "12345", "1234567", "12a456"})
    @DisplayName("Rechaza códigos de activación que no tengan 6 dígitos")
    void invalidActivationCode(String codigo) {
        assertFalse(validator.validate(new Activar2faRequestDTO(codigo)).isEmpty());
    }

    @Test
    @DisplayName("Acepta una verificación de login con token temporal y código de 6 dígitos")
    void validVerification() {
        assertTrue(validator.validate(new Verificar2faLoginRequestDTO("temporal", "123456")).isEmpty());
    }

    @Test
    @DisplayName("Rechaza una verificación sin token temporal y con código inválido")
    void invalidVerification() {
        // Act
        Set<String> campos = fieldsWithErrors(validator.validate(new Verificar2faLoginRequestDTO(" ", "abc")));

        // Assert
        assertEquals(Set.of("tokenTemporal", "codigo"), campos);
    }

    @Test
    @DisplayName("LoginResponseDTO de tres campos no requiere 2FA ni lleva token temporal")
    void threeFieldLoginResponse() {
        // Act
        LoginResponseDTO dto = new LoginResponseDTO("jwt", "Bearer", 3600);

        // Assert
        assertEquals("jwt", dto.token());
        assertEquals("Bearer", dto.tipo());
        assertEquals(3600, dto.expiracionSegundos());
        assertFalse(dto.requiere2fa());
        assertNull(dto.tokenTemporal());
    }
}
