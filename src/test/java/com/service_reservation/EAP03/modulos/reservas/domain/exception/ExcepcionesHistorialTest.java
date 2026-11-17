package com.service_reservation.EAP03.modulos.reservas.domain.exception;

import com.service_reservation.EAP03.modulos.reservas.domain.exception.ConsultaHistorialInvalidaException.Motivo;
import com.service_reservation.EAP03.modulos.reservas.domain.model.EstadoReserva;
import com.service_reservation.EAP03.modulos.reservas.domain.model.IntervaloReservado;
import com.service_reservation.EAP03.modulos.reservas.domain.model.PaginaHistorial;
import com.service_reservation.EAP03.modulos.reservas.domain.model.PartesReserva;
import com.service_reservation.EAP03.modulos.reservas.domain.model.Reserva;
import com.service_reservation.EAP03.modulos.reservas.domain.model.ReservaHistorial;
import com.service_reservation.EAP03.modulos.reservas.infrastructure.inbound.HistorialReservasResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

class ExcepcionesHistorialTest {

    @Test
    @DisplayName("El historial ajeno tiene un mensaje fijo que no revela datos")
    void otherHistoryHasFixedMessage() {
        HistorialAjenoException ex = new HistorialAjenoException();

        assertEquals("No tiene permiso para consultar el historial de otro cliente.", ex.getMessage());
        assertInstanceOf(ReservaDominioException.class, ex);
    }

    @Test
    @DisplayName("La consulta inválida guarda el motivo y usa un mensaje fijo")
    void invalidQueryKeepsReason() {
        for (Motivo motivo : Motivo.values()) {
            ConsultaHistorialInvalidaException ex = new ConsultaHistorialInvalidaException(motivo);

            assertEquals(motivo, ex.getMotivo());
            assertEquals("La solicitud de consulta no es válida.", ex.getMessage());
        }
    }

    @Test
    @DisplayName("El DTO del historial arma la hora, el mensaje de vacío y el total de páginas")
    void historyDtoFormatsFields() {
        LocalDateTime inicio = LocalDateTime.of(2026, 11, 2, 9, 5);
        Reserva r = Reserva.reconstruir(50, new PartesReserva(3L, 10, 7), new IntervaloReservado(inicio, inicio.plusHours(1)),
                EstadoReserva.COMPLETADO, null, inicio.minusDays(3), null);

        HistorialReservasResponseDTO conDatos = HistorialReservasResponseDTO.desde(
                new PaginaHistorial(List.of(new ReservaHistorial(r, null)), 0, 10, 11));
        HistorialReservasResponseDTO vacio = HistorialReservasResponseDTO.desde(new PaginaHistorial(List.of(), 0, 10, 0));

        assertEquals("09:05", conDatos.contenido().get(0).hora());
        assertEquals(EstadoReserva.COMPLETADO, conDatos.contenido().get(0).estado());
        assertNull(conDatos.contenido().get(0).nombreServicio());
        assertNull(conDatos.mensaje());
        assertEquals(2, conDatos.totalPaginas());
        assertEquals(HistorialReservasResponseDTO.MENSAJE_SIN_CITAS, vacio.mensaje());
        assertEquals(0, new PaginaHistorial(List.of(), 0, 0, 5).totalPaginas());
    }
}