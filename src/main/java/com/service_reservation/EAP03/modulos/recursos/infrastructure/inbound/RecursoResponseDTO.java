package com.service_reservation.EAP03.modulos.recursos.infrastructure.inbound;

import com.service_reservation.EAP03.modulos.recursos.domain.model.Recurso;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RecursoResponseDTO(
        Integer id,
        Long idProveedor,
        String nombre,
        BigDecimal precioUnitario,
        Integer stock,
        LocalDateTime createdAt,
        boolean activo
) {
    public static RecursoResponseDTO desde(Recurso recurso) {
        return new RecursoResponseDTO(
                recurso.getId(),
                recurso.getIdProveedor(),
                recurso.getNombre(),
                recurso.getPrecioUnitario(),
                recurso.getStock(),
                recurso.getCreatedAt(),
                recurso.estaActivo()
        );
    }
}
