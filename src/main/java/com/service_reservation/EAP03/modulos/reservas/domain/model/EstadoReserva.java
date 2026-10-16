package com.service_reservation.EAP03.modulos.reservas.domain.model;

public enum EstadoReserva {
    RESERVADO,
    EN_PROCESO,
    COMPLETADO,
    CANCELADO,
    NO_ASISTIO;

    // Solo las reservas vigentes (reservada o en curso) ocupan el horario. Una cancelada,
    // completada o en la que el cliente no asistió deja el horario libre.
    public boolean esActiva() {
        return this == RESERVADO || this == EN_PROCESO;
    }
}
