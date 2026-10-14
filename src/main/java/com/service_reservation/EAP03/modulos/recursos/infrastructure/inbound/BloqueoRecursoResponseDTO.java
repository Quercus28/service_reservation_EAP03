package com.service_reservation.EAP03.modulos.recursos.infrastructure.inbound;

import com.service_reservation.EAP03.modulos.recursos.domain.model.BloqueoRecurso;

import java.time.LocalDateTime;

public record BloqueoRecursoResponseDTO(
        Integer id,
        Integer idRecurso,
        LocalDateTime fechaInicio,
        LocalDateTime fechaFin,
        String motivo,
        LocalDateTime createdAt
) {
    public static BloqueoRecursoResponseDTO desde(BloqueoRecurso b) {
        return new BloqueoRecursoResponseDTO(
                b.getId(), b.getIdRecurso(), b.getFechaInicio(),
                b.getFechaFin(), b.getMotivo(), b.getCreatedAt()
        );
    }
}
