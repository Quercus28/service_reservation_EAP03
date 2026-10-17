package com.service_reservation.EAP03.modulos.reservas.infrastructure.inbound;

import com.service_reservation.EAP03.modulos.reservas.domain.model.EstadoReserva;
import com.service_reservation.EAP03.modulos.reservas.domain.model.Reserva;

import java.time.LocalDateTime;

public record CancelacionReservaResponseDTO(
        Integer id,
        EstadoReserva estado,
        LocalDateTime fechaInicio,
        LocalDateTime canceladaEn,
        String motivoCancelacion
) {
    public static CancelacionReservaResponseDTO desde(Reserva r) {
        return new CancelacionReservaResponseDTO(
                r.getId(), r.getEstado(), r.getFechaInicio(),
                r.getCanceladaEn(), r.getMotivoCancelacion()
        );
    }
}
