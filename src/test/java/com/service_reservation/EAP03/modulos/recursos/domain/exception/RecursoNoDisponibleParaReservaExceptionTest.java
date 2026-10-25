package com.service_reservation.EAP03.modulos.recursos.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RecursoNoDisponibleParaReservaExceptionTest {

    @Test
    @DisplayName("Sin unidades usa el singular cuando se necesita y queda una")
    void withoutUnitsSingular() {
        assertEquals("No se puede reservar: del recurso 'Camilla' se necesita 1 unidad y en ese horario solo queda 1.",
                RecursoNoDisponibleParaReservaException.sinUnidades("Camilla", 1, 1).getMessage());
    }

    @Test
    @DisplayName("Sin unidades usa el plural cuando se necesitan y quedan varias o ninguna")
    void withoutUnitsPlural() {
        assertEquals("No se puede reservar: del recurso 'Camilla' se necesitan 3 unidades y en ese horario solo quedan 0.",
                RecursoNoDisponibleParaReservaException.sinUnidades("Camilla", 3, 0).getMessage());
    }

    @Test
    @DisplayName("No operativo explica que está fuera de horario o bloqueado")
    void notOperational() {
        assertEquals("No se puede reservar: el recurso 'Camilla' no se puede usar en ese horario"
                        + " (está fuera de su horario o bloqueado).",
                RecursoNoDisponibleParaReservaException.noOperativo("Camilla").getMessage());
    }
}
