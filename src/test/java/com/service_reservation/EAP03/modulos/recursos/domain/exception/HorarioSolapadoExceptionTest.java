package com.service_reservation.EAP03.modulos.recursos.domain.exception;

import com.service_reservation.EAP03.modulos.recursos.domain.model.DiaSemana;
import com.service_reservation.EAP03.modulos.recursos.domain.model.HorarioRecurso;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HorarioSolapadoExceptionTest {

    @Test
    @DisplayName("Describe el horario existente con el que se solapa")
    void describesExistingSchedule() {
        // Arrange
        HorarioRecurso existente = HorarioRecurso.reconstruir(2, 1, DiaSemana.LUNES,
                LocalTime.of(8, 0), LocalTime.of(12, 0));

        // Act
        HorarioSolapadoException ex = new HorarioSolapadoException(existente);

        // Assert
        assertEquals("El horario se solapa con otro ya registrado para el recurso 1: LUNES de 08:00 a 12:00.",
                ex.getMessage());
    }
}
