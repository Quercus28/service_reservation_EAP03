package com.service_reservation.EAP03.modulos.recursos.infrastructure.inbound;

import com.service_reservation.EAP03.modulos.recursos.domain.model.ServicioRecurso;

public record ServicioRecursoResponseDTO(
        Integer id,
        Integer idServicio,
        Integer idRecurso,
        int cantidad
) {
    public static ServicioRecursoResponseDTO desde(ServicioRecurso sr) {
        return new ServicioRecursoResponseDTO(
                sr.getId(), sr.getIdServicio(), sr.getIdRecurso(), sr.getCantidad()
        );
    }
}
