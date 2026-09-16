package com.service_reservation.EAP03.modulos.servicios_catalogo.application.dto;

import com.service_reservation.EAP03.modulos.servicios_catalogo.domain.model.Servicio;

import java.time.OffsetDateTime;

public record ServicioResponse(
        Integer id,
        Integer idProveedor,
        String nombre,
        Integer duracionMinutos,
        OffsetDateTime createdAt
) {
    public static ServicioResponse fromDomain(Servicio servicio) {
        if (servicio == null) {
            return null;
        }
        return new ServicioResponse(
                servicio.getId(),
                servicio.getIdProveedor(),
                servicio.getNombre(),
                servicio.getDuracionMinutos(),
                servicio.getCreatedAt()
        );
    }
}
