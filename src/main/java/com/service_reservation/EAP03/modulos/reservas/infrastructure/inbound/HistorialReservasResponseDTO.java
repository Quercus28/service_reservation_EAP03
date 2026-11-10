package com.service_reservation.EAP03.modulos.reservas.infrastructure.inbound;

import com.service_reservation.EAP03.modulos.reservas.domain.model.EstadoReserva;
import com.service_reservation.EAP03.modulos.reservas.domain.model.PaginaHistorial;
import com.service_reservation.EAP03.modulos.reservas.domain.model.Reserva;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public record HistorialReservasResponseDTO(
        List<Item> contenido,
        int page,
        int size,
        long totalElementos,
        int totalPaginas,
        String mensaje
) {
    public static final String MENSAJE_SIN_CITAS = "No tienes citas registradas.";
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    public record Item(
            Integer id,
            Integer idServicio,
            String nombreServicio,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin,
            String hora,
            EstadoReserva estado
    ) {
    }

    public static HistorialReservasResponseDTO desde(PaginaHistorial pagina) {
        List<Item> items = pagina.contenido().stream().map(h -> {
            Reserva r = h.reserva();
            return new Item(r.getId(), r.getIdServicio(), h.nombreServicio(), r.getFechaInicio(),
                    r.getFechaFin(), r.getFechaInicio().format(HORA), r.getEstado());
        }).toList();
        return new HistorialReservasResponseDTO(items, pagina.page(), pagina.size(), pagina.total(),
                pagina.totalPaginas(), pagina.total() == 0 ? MENSAJE_SIN_CITAS : null);
    }
}