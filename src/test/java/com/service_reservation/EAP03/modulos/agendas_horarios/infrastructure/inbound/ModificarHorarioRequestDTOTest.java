package com.service_reservation.EAP03.modulos.agendas_horarios.infrastructure.inbound;

import com.service_reservation.EAP03.modulos.agendas_horarios.domain.model.DiaSemana;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModificarHorarioRequestDTOTest {

    private static Validator validator;

    @BeforeAll
    static void createValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    @DisplayName("Una modificación con id y horario válidos no produce violaciones")
    void validRequest() {
        ModificarHorarioRequestDTO dto = new ModificarHorarioRequestDTO(1L,
                new AgendaHorarioRequestDTO(DiaSemana.LUNES, "08:00", "12:00"));

        assertTrue(validator.validate(dto).isEmpty());
    }

    @Test
    @DisplayName("Rechaza la falta de id y valida también el horario anidado")
    void validatesIdAndNestedSchedule() {
        // Arrange
        ModificarHorarioRequestDTO dto = new ModificarHorarioRequestDTO(null,
                new AgendaHorarioRequestDTO(DiaSemana.LUNES, "8:00", "25:00"));

        // Act
        Set<String> campos = validator.validate(dto).stream()
                .map(ConstraintViolation::getPropertyPath).map(Object::toString).collect(Collectors.toSet());

        // Assert
        assertEquals(Set.of("horarioId", "nuevoHorario.horaInicio", "nuevoHorario.horaFin"), campos);
    }

    @Test
    @DisplayName("Rechaza la falta del nuevo horario")
    void rejectsMissingSchedule() {
        Set<String> campos = validator.validate(new ModificarHorarioRequestDTO(1L, null)).stream()
                .map(ConstraintViolation::getPropertyPath).map(Object::toString).collect(Collectors.toSet());

        assertEquals(Set.of("nuevoHorario"), campos);
    }
}
