package com.service_reservation.EAP03.modulos.reservas.domain.exception;

import java.time.LocalTime;

public class HoraFueraDeTurnoException extends ReservaDominioException {
    public HoraFueraDeTurnoException(int duracionMinutos, LocalTime apertura) {
        super("Este servicio se reserva cada " + duracionMinutos + " minutos desde las " + apertura + ".");
    }
}
