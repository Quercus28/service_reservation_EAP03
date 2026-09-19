package com.service_reservation.EAP03.modulos.recursos.infrastructure.inbound;

import com.service_reservation.EAP03.modulos.recursos.domain.model.DisponibilidadRecurso;

import java.time.LocalDateTime;

public record DisponibilidadResponseDTO(
        Integer idRecurso,
        LocalDateTime desde,
        LocalDateTime hasta,
        int stockTotal,
        int comprometido,
        int disponible
) {
    public static DisponibilidadResponseDTO desde(DisponibilidadRecurso d) {
        return new DisponibilidadResponseDTO(
                d.idRecurso(), d.desde(), d.hasta(),
                d.stockTotal(), d.comprometido(), d.disponible()
        );
    }
}
