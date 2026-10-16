package com.service_reservation.EAP03.modulos.reservas.domain.model;

public enum EstadoReserva {
    RESERVADO,
    EN_PROCESO,
    COMPLETADO,
    CANCELADO,
    NO_ASISTIO;

    // Solo una reserva activa ocupa el horario del servicio.
    public boolean esActiva() {
        return this == RESERVADO || this == EN_PROCESO;
    }
}
