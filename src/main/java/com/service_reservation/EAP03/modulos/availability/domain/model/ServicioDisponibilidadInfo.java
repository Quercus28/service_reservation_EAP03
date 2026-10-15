package com.service_reservation.EAP03.modulos.availability.domain.model;

import java.util.Objects;

/**
 * Value Object inmutable que representa la configuración y duración del servicio necesaria para calcular disponibilidad.
 */
public record ServicioDisponibilidadInfo(Integer id, Integer idProveedor, int duracionMinutos) {

    public ServicioDisponibilidadInfo {
        Objects.requireNonNull(id, "El id del servicio es obligatorio");
        Objects.requireNonNull(idProveedor, "El id del proveedor es obligatorio");
        if (duracionMinutos <= 0) {
            throw new IllegalArgumentException("La duración del servicio debe ser mayor a 0 minutos");
        }
    }
}
