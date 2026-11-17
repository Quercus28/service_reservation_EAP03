package com.service_reservation.EAP03.modulos.reportes.domain.model;

import java.util.Objects;

/**
 * Value Object que representa las métricas de ocupación de un único servicio
 * dentro del período consultado.
 * Dominio puro: sin anotaciones de Spring ni JPA.
 */
public record OcupacionServicio(
        Integer idServicio,
        String nombreServicio,
        int totalCitas,
        double horasOcupadas,
        double horasDisponibles,
        double porcentajeOcupacion
) {
    public OcupacionServicio {
        Objects.requireNonNull(idServicio,       "El id del servicio es obligatorio");
        Objects.requireNonNull(nombreServicio,   "El nombre del servicio es obligatorio");
    }
}
