package com.service_reservation.EAP03.modulos.reservas.domain.exception;

// Un recurso que la reserva necesita no se pudo apartar (desactivado, sin unidades libres,
// fuera de su horario o bloqueado). El mensaje viene del módulo de recursos y dice cuál.
public class RecursoNoDisponibleException extends ReservaDominioException {
    public RecursoNoDisponibleException(String mensaje) {
        super(mensaje);
    }
}
