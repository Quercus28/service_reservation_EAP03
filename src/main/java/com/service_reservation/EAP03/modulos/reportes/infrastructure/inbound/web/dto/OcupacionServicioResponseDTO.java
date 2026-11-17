package com.service_reservation.EAP03.modulos.reportes.infrastructure.inbound.web.dto;

import com.service_reservation.EAP03.modulos.reportes.domain.model.OcupacionServicio;

/**
 * DTO de salida con las métricas de ocupación para un servicio individual.
 */
public record OcupacionServicioResponseDTO(
        Integer idServicio,
        String nombreServicio,
        int totalCitas,
        double horasOcupadas,
        double horasDisponibles,
        double porcentajeOcupacion
) {
    public static OcupacionServicioResponseDTO from(OcupacionServicio servicio) {
        return new OcupacionServicioResponseDTO(
                servicio.idServicio(),
                servicio.nombreServicio(),
                servicio.totalCitas(),
                servicio.horasOcupadas(),
                servicio.horasDisponibles(),
                servicio.porcentajeOcupacion()
        );
    }
}
