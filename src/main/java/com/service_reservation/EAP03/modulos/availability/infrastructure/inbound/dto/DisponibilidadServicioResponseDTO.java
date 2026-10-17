package com.service_reservation.EAP03.modulos.availability.infrastructure.inbound.dto;

import java.util.List;

public record DisponibilidadServicioResponseDTO(
        Integer servicioId,
        int totalEspaciosDisponibles,
        List<BloqueDisponibilidadDTO> disponibilidad,
        // Solo trae texto cuando no hay ningún espacio libre; si hay espacios va en null.
        String mensaje
) {
    public static final String MENSAJE_SIN_ESPACIOS = "No hay horarios disponibles en ese rango";
}
