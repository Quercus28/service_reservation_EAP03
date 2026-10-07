package com.service_reservation.EAP03.modulos.admin.domain.model;

public enum EstadoRevocacion {
    /** Proveedor con reservas activas dentro del plazo de gracia para cancelarlas. */
    PENDIENTE_CANCELACION,
    /** Revocación efectiva y cerrada. */
    COMPLETADA,
    /** El administrador restauró los permisos antes de que la revocación se completara. */
    ANULADA
}
