package com.service_reservation.EAP03.modulos.reservas.domain.model;

import java.time.Duration;
import java.time.LocalTime;

// Los turnos de un servicio empiezan a la hora de apertura del horario y se repiten cada
// "duración del servicio". Por ejemplo, con 60 minutos desde las 08:00: 08:00, 09:00,
// 10:00... Es la misma regla con la que la consulta de disponibilidad (módulo availability)
// muestra los horarios libres, así que solo se puede reservar lo que esa consulta muestra.
public final class ReglaDeTurnos {

    private ReglaDeTurnos() {
    }

    // ¿La hora de inicio cae justo en un turno? Es decir, ¿desde la apertura hasta el inicio
    // pasó un número exacto de turnos? 08:00 y 09:00 sí; 08:30 no.
    public static boolean caeEnTurno(LocalTime apertura, LocalTime inicio, int duracionMinutos) {
        if (duracionMinutos <= 0 || inicio.isBefore(apertura)) {
            return false;
        }
        Duration desdeApertura = Duration.between(apertura, inicio);
        return desdeApertura.getNano() == 0
                && desdeApertura.getSeconds() % (duracionMinutos * 60L) == 0;
    }
}
