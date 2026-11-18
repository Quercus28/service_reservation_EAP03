package com.service_reservation.EAP03.modulos.reportes.infrastructure.inbound.web.dto;

import com.service_reservation.EAP03.modulos.reportes.domain.model.ReporteOcupacion;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO de salida para la respuesta del endpoint de reporte de ocupación.
 * Contiene cifras consolidadas de ocupación y cancelaciones, así como el detalle por servicio.
 */
public record ReporteOcupacionResponseDTO(
        Integer proveedorId,
        LocalDate startDate,
        LocalDate endDate,
        int totalCitasGlobal,
        double horasOcupadasGlobal,
        double horasDisponiblesGlobal,
        double porcentajeOcupacionGlobal,
        int totalCanceladasGlobal,
        double porcentajeCancelacionGlobal,
        List<OcupacionServicioResponseDTO> servicios
) {
    public static ReporteOcupacionResponseDTO from(ReporteOcupacion reporte) {
        List<OcupacionServicioResponseDTO> detalle = reporte.getDetalleServicios().stream()
                .map(OcupacionServicioResponseDTO::from)
                .toList();

        return new ReporteOcupacionResponseDTO(
                reporte.getIdProveedor(),
                reporte.getPeriodo().startDate(),
                reporte.getPeriodo().endDate(),
                reporte.getTotalCitasGlobal(),
                reporte.getHorasOcupadasGlobal(),
                reporte.getHorasDisponiblesGlobal(),
                reporte.getPorcentajeOcupacionGlobal(),
                reporte.getTotalCanceladasGlobal(),
                reporte.getPorcentajeCancelacionGlobal(),
                detalle
        );
    }
}
