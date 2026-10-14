package com.service_reservation.EAP03.modulos.availability.infrastructure.inbound.dto;

import com.service_reservation.EAP03.modulos.availability.domain.model.TimeSlot;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record BloqueDisponibilidadDTO(
        LocalDate fecha,
        List<FranjaHorariaDTO> franjas
) {
    public record FranjaHorariaDTO(
            LocalTime inicio,
            LocalTime fin
    ) {
        public static FranjaHorariaDTO from(TimeSlot slot) {
            return new FranjaHorariaDTO(slot.start().toLocalTime(), slot.end().toLocalTime());
        }
    }
}
