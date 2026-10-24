package com.service_reservation.EAP03.modulos.servicios_catalogo.application.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServicioRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void createValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private static boolean hasNameViolation(Set<? extends ConstraintViolation<?>> violations) {
        return violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("nombre"));
    }

    @ParameterizedTest(name = "nombre = {0}")
    @ValueSource(strings = {"Limpieza facial (30 min)", "Corte de cabello - niño", "Masaje D'Angelo, 2.0"})
    @DisplayName("Acepta nombres legítimos al crear y al actualizar un servicio")
    void acceptsLegitimateNames(String nombre) {
        // Arrange
        CrearServicioRequest crear = new CrearServicioRequest(1, nombre, 30);
        ActualizarServicioRequest actualizar = new ActualizarServicioRequest(nombre, 30);

        // Act
        boolean createViolation = hasNameViolation(validator.validate(crear));
        boolean updateViolation = hasNameViolation(validator.validate(actualizar));

        // Assert
        assertFalse(createViolation);
        assertFalse(updateViolation);
    }

    @ParameterizedTest(name = "nombre = {0}")
    @ValueSource(strings = {"<script>alert(1)</script>", "x'; DROP TABLE servicio;--", "<img src=x onerror=alert(1)>"})
    @DisplayName("Rechaza HTML/JS y comandos en el nombre al crear y al actualizar un servicio")
    void rejectsMaliciousNames(String nombre) {
        // Arrange
        CrearServicioRequest crear = new CrearServicioRequest(1, nombre, 30);
        ActualizarServicioRequest actualizar = new ActualizarServicioRequest(nombre, 30);

        // Act
        boolean createViolation = hasNameViolation(validator.validate(crear));
        boolean updateViolation = hasNameViolation(validator.validate(actualizar));

        // Assert
        assertTrue(createViolation);
        assertTrue(updateViolation);
    }
}
