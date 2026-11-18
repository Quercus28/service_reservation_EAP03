package com.service_reservation.EAP03.modulos.reservas.infrastructure.inbound;

import com.service_reservation.EAP03.modulos.reservas.domain.model.EstadoReserva;
import com.service_reservation.EAP03.modulos.reservas.domain.model.IntervaloReservado;
import com.service_reservation.EAP03.modulos.reservas.domain.model.PaginaHistorial;
import com.service_reservation.EAP03.modulos.reservas.domain.model.PartesReserva;
import com.service_reservation.EAP03.modulos.reservas.domain.model.Reserva;
import com.service_reservation.EAP03.modulos.reservas.domain.model.ReservaHistorial;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class HistorialReservasResponseDTOTest {

    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 11, 2, 9, 0);

    private static ReservaHistorial cita() {
        return new ReservaHistorial(Reserva.reconstruir(1, new PartesReserva(3L, 10, 7),
                new IntervaloReservado(INICIO, INICIO.plusHours(1)), EstadoReserva.COMPLETADO, null, INICIO, null), "Corte");
    }

    @Test
    @DisplayName("Sin ninguna cita y sin filtro: 'No tienes citas registradas.'")
    void noAppointmentsAtAll() {
        HistorialReservasResponseDTO dto = HistorialReservasResponseDTO.desde(new PaginaHistorial(List.of(), 0, 10, 0), null);

        assertEquals("No tienes citas registradas.", dto.mensaje());
    }

    @Test
    @DisplayName("Filtrando por un estado sin citas: dice que no tiene citas en ese estado")
    void noAppointmentsInFilteredState() {
        HistorialReservasResponseDTO dto = HistorialReservasResponseDTO.desde(
                new PaginaHistorial(List.of(), 0, 10, 0), EstadoReserva.CANCELADO);

        assertEquals("No tienes citas en estado CANCELADO.", dto.mensaje());
    }

    @Test
    @DisplayName("Pidiendo una página que no existe: dice que esa página no tiene citas y hasta cuál hay")
    void pageBeyondLast() {
        HistorialReservasResponseDTO dto = HistorialReservasResponseDTO.desde(
                new PaginaHistorial(List.of(), 5, 10, 25), null);

        assertEquals("La página 5 no tiene citas. Las páginas van de 0 a 2.", dto.mensaje());
    }

    @Test
    @DisplayName("Con filtro y una página que no existe, gana el mensaje de la página")
    void filteredPageBeyondLast() {
        HistorialReservasResponseDTO dto = HistorialReservasResponseDTO.desde(
                new PaginaHistorial(List.of(), 3, 10, 4), EstadoReserva.COMPLETADO);

        assertEquals("La página 3 no tiene citas. Las páginas van de 0 a 0.", dto.mensaje());
    }

    @Test
    @DisplayName("Si la página trae citas no hay mensaje, con o sin filtro")
    void withAppointmentsNoMessage() {
        assertNull(HistorialReservasResponseDTO.desde(new PaginaHistorial(List.of(cita()), 0, 10, 1), null).mensaje());
        assertNull(HistorialReservasResponseDTO.desde(
                new PaginaHistorial(List.of(cita()), 0, 10, 1), EstadoReserva.COMPLETADO).mensaje());
    }

    @Test
    @DisplayName("La versión sin filtro se comporta como si no se hubiera filtrado")
    void withoutFilterOverload() {
        assertEquals("No tienes citas registradas.",
                HistorialReservasResponseDTO.desde(new PaginaHistorial(List.of(), 0, 10, 0)).mensaje());
    }
}
