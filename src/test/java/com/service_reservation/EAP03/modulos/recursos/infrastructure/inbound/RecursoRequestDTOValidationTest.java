package com.service_reservation.EAP03.modulos.recursos.infrastructure.inbound;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecursoRequestDTOValidationTest {

    private static Validator validator;

    @BeforeAll
    static void createValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private static boolean hasNameViolation(Set<? extends ConstraintViolation<?>> violations) {
        return violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("nombre"));
    }

    @ParameterizedTest(name = "nombre = {0}")
    @ValueSource(strings = {"Kit O'Neill", "Silla ergonómica (niño) - Ñandú, 2.0", "Pingüino de peluche"})
    @DisplayName("Acepta nombres legítimos al crear y al actualizar un recurso")
    void acceptsLegitimateNames(String nombre) {
        // Arrange
        CrearRecursoRequestDTO crear = new CrearRecursoRequestDTO(nombre, new BigDecimal("50000"), 3);
        ActualizarRecursoRequestDTO actualizar = new ActualizarRecursoRequestDTO(nombre, new BigDecimal("50000"), 3);

        // Act
        boolean createViolation = hasNameViolation(validator.validate(crear));
        boolean updateViolation = hasNameViolation(validator.validate(actualizar));

        // Assert
        assertFalse(createViolation);
        assertFalse(updateViolation);
    }

    @ParameterizedTest(name = "nombre = {0}")
    @ValueSource(strings = {"<img src=x onerror=alert(1)>", "<script>alert(1)</script>", "x'; DROP TABLE recurso;--"})
    @DisplayName("Rechaza HTML/JS y comandos en el nombre al crear y al actualizar un recurso (BUG-03)")
    void rejectsMaliciousNames(String nombre) {
        // Arrange
        CrearRecursoRequestDTO crear = new CrearRecursoRequestDTO(nombre, new BigDecimal("50000"), 3);
        ActualizarRecursoRequestDTO actualizar = new ActualizarRecursoRequestDTO(nombre, new BigDecimal("50000"), 3);

        // Act
        boolean createViolation = hasNameViolation(validator.validate(crear));
        boolean updateViolation = hasNameViolation(validator.validate(actualizar));

        // Assert
        assertTrue(createViolation);
        assertTrue(updateViolation);
    }
}
