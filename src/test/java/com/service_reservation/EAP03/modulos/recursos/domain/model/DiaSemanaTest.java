package com.service_reservation.EAP03.modulos.recursos.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.DayOfWeek;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DiaSemanaTest {

    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource({
            "MONDAY, LUNES",
            "TUESDAY, MARTES",
            "WEDNESDAY, MIERCOLES",
            "THURSDAY, JUEVES",
            "FRIDAY, VIERNES",
            "SATURDAY, SABADO",
            "SUNDAY, DOMINGO"
    })
    @DisplayName("Convierte cada día de la semana de Java a su equivalente en español")
    void mapsEveryDay(DayOfWeek dia, DiaSemana esperado) {
        assertEquals(esperado, DiaSemana.desde(dia));
    }
}
