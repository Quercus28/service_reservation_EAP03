package com.service_reservation.EAP03.modulos.recursos.infrastructure.inbound;

import com.service_reservation.EAP03.modulos.recursos.domain.model.RecursoPrestado;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RecursoPrestadoResponseDTO(
        Integer id,
        Integer idRecurso,
        Integer idServicioPrestado,
        Integer cantidad,
        LocalDateTime fechaInicio,
        LocalDateTime fechaFin,
        BigDecimal precioTotal
) {
    public static RecursoPrestadoResponseDTO desde(RecursoPrestado p) {
        return new RecursoPrestadoResponseDTO(
                p.getId(), p.getIdRecurso(), p.getIdServicioPrestado(),
                p.getCantidad(), p.getFechaInicio(), p.getFechaFin(), p.getPrecioTotal()
        );
    }
}
