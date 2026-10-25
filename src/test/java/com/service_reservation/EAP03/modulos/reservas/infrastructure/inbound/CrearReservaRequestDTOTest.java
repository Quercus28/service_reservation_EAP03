package com.service_reservation.EAP03.modulos.reservas.infrastructure.inbound;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class CrearReservaRequestDTOTest {

    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 11, 2, 9, 0);

    private static Validator validator;

    @BeforeAll
    static void createValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private static Set<String> fieldsWithErrors(CrearReservaRequestDTO dto) {
        return validator.validate(dto).stream()
                .map(ConstraintViolation::getPropertyPath).map(Object::toString).collect(Collectors.toSet());
    }

    @Test
    @DisplayName("Una reserva con datos válidos no produce violaciones")
    void validRequest() {
        assertTrue(fieldsWithErrors(new CrearReservaRequestDTO(10, INICIO, "Traer toalla", List.of(1, 2))).isEmpty());
        assertTrue(fieldsWithErrors(new CrearReservaRequestDTO(10, INICIO, null, null)).isEmpty());
    }

    @Test
    @DisplayName("Los recursos opcionales no pueden repetirse")
    void optionalResourcesMustBeUnique() {
        assertTrue(new CrearReservaRequestDTO(10, INICIO, null, null).isRecursosOpcionalesSinRepetir());
        assertTrue(new CrearReservaRequestDTO(10, INICIO, null, List.of(1, 2)).isRecursosOpcionalesSinRepetir());
        assertFalse(new CrearReservaRequestDTO(10, INICIO, null, List.of(1, 1)).isRecursosOpcionalesSinRepetir());
        assertEquals(Set.of("recursosOpcionalesSinRepetir"),
                fieldsWithErrors(new CrearReservaRequestDTO(10, INICIO, null, List.of(1, 1))));
    }

    @Test
    @DisplayName("Rechaza servicio y fecha ausentes, notas con HTML y recursos vacíos")
    void rejectsInvalidData() {
        Set<String> campos = fieldsWithErrors(new CrearReservaRequestDTO(null, null, "<script>", Arrays.asList(1, null)));

        assertEquals(Set.of("idServicio", "fechaInicio", "notas", "recursosOpcionales[1].<list element>"), campos);
    }
}
