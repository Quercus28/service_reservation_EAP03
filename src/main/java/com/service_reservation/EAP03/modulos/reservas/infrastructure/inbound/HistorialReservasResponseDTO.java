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
        return desde(pagina, null);
    }

    // estadoFiltrado: el estado por el que filtró el cliente, o null si no filtró.
    public static HistorialReservasResponseDTO desde(PaginaHistorial pagina, EstadoReserva estadoFiltrado) {
        List<Item> items = pagina.contenido().stream().map(h -> {
            Reserva r = h.reserva();
            return new Item(r.getId(), r.getIdServicio(), h.nombreServicio(), r.getFechaInicio(),
                    r.getFechaFin(), r.getFechaInicio().format(HORA), r.getEstado());
        }).toList();
        return new HistorialReservasResponseDTO(items, pagina.page(), pagina.size(), pagina.total(),
                pagina.totalPaginas(), mensajeSiVacia(pagina, estadoFiltrado));
    }

    // Solo hay mensaje cuando la lista sale vacía, y dice por qué:
    //  - hay citas, pero la página pedida está más allá de la última;
    //  - se filtró por un estado y no hay citas en ese estado;
    //  - de verdad no tiene ninguna cita.
    static String mensajeSiVacia(PaginaHistorial pagina, EstadoReserva estadoFiltrado) {
        if (!pagina.contenido().isEmpty()) {
            return null;
        }
        if (pagina.total() > 0) {
            return "La página " + pagina.page() + " no tiene citas. Las páginas van de 0 a "
                    + (pagina.totalPaginas() - 1) + ".";
        }
        if (estadoFiltrado != null) {
            return "No tienes citas en estado " + estadoFiltrado + ".";
        }
        return MENSAJE_SIN_CITAS;
    }
}