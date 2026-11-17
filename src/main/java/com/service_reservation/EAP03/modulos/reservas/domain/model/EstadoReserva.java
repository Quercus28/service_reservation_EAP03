package com.service_reservation.EAP03.modulos.reservas.domain.model;

// Nombres de la HU20: RESERVADO es la "Pendiente", CONFIRMADO la "Confirmada" y COMPLETADO la
// "Finalizada". Los nombres no se cambian para no romper los datos que ya están guardados.
public enum EstadoReserva {
    RESERVADO,
    CONFIRMADO,
    EN_PROCESO,
    COMPLETADO,
    CANCELADO,
    NO_ASISTIO;

    // Solo las reservas vigentes (reservada, confirmada o en curso) ocupan el horario. Una
    // cancelada, completada o en la que el cliente no asistió deja el horario libre.
    public boolean esActiva() {
        return this == RESERVADO || this == CONFIRMADO || this == EN_PROCESO;
    }

    // Cambios que puede hacer el proveedor: confirmar una pendiente y finalizar una confirmada.
    public boolean puedePasarA(EstadoReserva nuevo) {
        return (this == RESERVADO && nuevo == CONFIRMADO)
                || (this == CONFIRMADO && nuevo == COMPLETADO);
    }
}
