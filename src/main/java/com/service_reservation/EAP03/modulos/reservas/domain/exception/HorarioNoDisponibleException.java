package com.service_reservation.EAP03.modulos.reservas.domain.exception;

import java.time.LocalDateTime;

public class HorarioNoDisponibleException extends ReservaDominioException {

    private HorarioNoDisponibleException(String mensaje) {
        super(mensaje);
    }

    public static HorarioNoDisponibleException sinAgenda(Integer idServicio) {
        return new HorarioNoDisponibleException("El horario no está disponible: el servicio "
                + idServicio + " no tiene una agenda activa.");
    }

    public static HorarioNoDisponibleException fueraDeAgenda(Integer idServicio, LocalDateTime inicio, LocalDateTime fin) {
        return new HorarioNoDisponibleException("El horario no está disponible: de " + inicio + " a " + fin
                + " queda fuera de la agenda del servicio " + idServicio + ".");
    }

    public static HorarioNoDisponibleException yaReservado(Integer idServicio, LocalDateTime inicio, LocalDateTime fin) {
        return new HorarioNoDisponibleException("El horario no está disponible: de " + inicio + " a " + fin
                + " ya está reservado para el servicio " + idServicio + ".");
    }
}
