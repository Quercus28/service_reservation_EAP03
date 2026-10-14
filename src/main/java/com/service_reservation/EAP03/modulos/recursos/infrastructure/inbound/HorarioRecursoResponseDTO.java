package com.service_reservation.EAP03.modulos.recursos.infrastructure.inbound;

import com.service_reservation.EAP03.modulos.recursos.domain.model.DiaSemana;
import com.service_reservation.EAP03.modulos.recursos.domain.model.HorarioRecurso;

import java.time.format.DateTimeFormatter;

public record HorarioRecursoResponseDTO(
        Integer id,
        Integer idRecurso,
        DiaSemana diaSemana,
        String horaInicio,
        String horaFin
) {
    private static final DateTimeFormatter HH_MM = DateTimeFormatter.ofPattern("HH:mm");

    public static HorarioRecursoResponseDTO desde(HorarioRecurso h) {
        return new HorarioRecursoResponseDTO(
                h.getId(), h.getIdRecurso(), h.getDiaSemana(),
                h.getHoraInicio().format(HH_MM), h.getHoraFin().format(HH_MM)
        );
    }
}
