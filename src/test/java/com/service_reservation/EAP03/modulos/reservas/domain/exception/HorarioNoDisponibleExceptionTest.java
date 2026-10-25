package com.service_reservation.EAP03.modulos.reservas.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HorarioNoDisponibleExceptionTest {

    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 11, 2, 9, 0);
    private static final LocalDateTime FIN = INICIO.plusHours(1);

    @Test
    @DisplayName("Sin agenda indica que el servicio no tiene agenda activa")
    void withoutAgenda() {
        assertEquals("El horario no está disponible: el servicio 10 no tiene una agenda activa.",
                HorarioNoDisponibleException.sinAgenda(10).getMessage());
    }

    @Test
    @DisplayName("Fuera de agenda indica el rango que no cabe en la agenda")
    void outsideAgenda() {
        assertEquals("El horario no está disponible: de " + INICIO + " a " + FIN + " queda fuera de la agenda del servicio 10.",
                HorarioNoDisponibleException.fueraDeAgenda(10, INICIO, FIN).getMessage());
    }

    @Test
    @DisplayName("Ya reservado indica el rango ocupado")
    void alreadyReserved() {
        assertEquals("El horario no está disponible: de " + INICIO + " a " + FIN + " ya está reservado para el servicio 10.",
                HorarioNoDisponibleException.yaReservado(10, INICIO, FIN).getMessage());
    }
}
