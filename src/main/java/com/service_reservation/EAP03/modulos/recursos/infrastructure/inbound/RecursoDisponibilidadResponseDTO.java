package com.service_reservation.EAP03.modulos.recursos.infrastructure.inbound;

import com.service_reservation.EAP03.modulos.recursos.domain.model.RecursoConHorarios;

import java.util.List;

public record RecursoDisponibilidadResponseDTO(
        Integer idRecurso,
        String nombre,
        Integer stock,
        List<HorarioRecursoResponseDTO> horarios
) {
    public static RecursoDisponibilidadResponseDTO desde(RecursoConHorarios r) {
        return new RecursoDisponibilidadResponseDTO(
                r.recurso().getId(),
                r.recurso().getNombre(),
                r.recurso().getStock(),
                r.horarios().stream().map(HorarioRecursoResponseDTO::desde).toList()
        );
    }
}
