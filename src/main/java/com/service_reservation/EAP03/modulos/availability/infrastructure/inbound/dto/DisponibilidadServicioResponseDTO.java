package com.service_reservation.EAP03.modulos.availability.infrastructure.inbound.dto;

import java.util.List;

public record DisponibilidadServicioResponseDTO(
        Integer servicioId,
        int totalEspaciosDisponibles,
        List<BloqueDisponibilidadDTO> disponibilidad
) {
}
