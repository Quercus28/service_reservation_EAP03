package com.service_reservation.EAP03.modulos.reservas.infrastructure.inbound;

import com.service_reservation.EAP03.modulos.reservas.domain.model.EstadoReserva;
import com.service_reservation.EAP03.modulos.reservas.domain.model.Reserva;

import java.time.LocalDateTime;

public record ReservaResponseDTO(
        Integer id,
        Integer idServicio,
        LocalDateTime fechaInicio,
        LocalDateTime fechaFin,
        EstadoReserva estado,
        String notas
) {
    public static ReservaResponseDTO desde(Reserva r) {
        return new ReservaResponseDTO(
                r.getId(), r.getIdServicio(), r.getFechaInicio(),
                r.getFechaFin(), r.getEstado(), r.getNotas()
        );
    }
}
