package com.service_reservation.EAP03.modulos.reportes.infrastructure.inbound.web.dto;

import com.service_reservation.EAP03.modulos.reportes.domain.model.OcupacionServicio;

/**
 * DTO de salida con las métricas de ocupación y cancelación para un servicio individual.
 */
public record OcupacionServicioResponseDTO(
        Integer idServicio,
        String nombreServicio,
        int totalCitas,
        double horasOcupadas,
        double horasDisponibles,
        double porcentajeOcupacion,
        int citasCanceladas,
        double porcentajeCancelacion
) {
    public static OcupacionServicioResponseDTO from(OcupacionServicio servicio) {
        return new OcupacionServicioResponseDTO(
                servicio.getIdServicio(),
                servicio.getNombreServicio(),
                servicio.getTotalCitas(),
                servicio.getHorasOcupadas(),
                servicio.getHorasDisponibles(),
                servicio.getPorcentajeOcupacion(),
                servicio.getCitasCanceladas(),
                servicio.getPorcentajeCancelacion()
        );
    }
}
