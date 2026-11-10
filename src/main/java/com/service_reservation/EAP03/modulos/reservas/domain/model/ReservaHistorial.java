package com.service_reservation.EAP03.modulos.reservas.domain.model;

/** Una reserva del historial junto con el nombre de su servicio (null si el servicio ya no existe). */
public record ReservaHistorial(Reserva reserva, String nombreServicio) {
}