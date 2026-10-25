package com.service_reservation.EAP03.modulos.reservas.domain.model;

import com.service_reservation.EAP03.modulos.reservas.domain.exception.CancelacionFueraDeTiempoException;
import com.service_reservation.EAP03.modulos.reservas.domain.exception.DatosReservaInvalidosException;
import com.service_reservation.EAP03.modulos.reservas.domain.exception.FechaEnElPasadoException;
import com.service_reservation.EAP03.modulos.reservas.domain.exception.ReservaNoCancelableException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ReservaTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 10, 24, 12, 0);
    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 11, 2, 9, 0);
    private static final LocalDateTime FIN = INICIO.plusHours(1);

    private static Reserva newReservation() {
        return Reserva.nueva(3L, 10, 7, INICIO, FIN, "  Traer toalla  ", AHORA);
    }

    private static Reserva withState(EstadoReserva estado) {
        return Reserva.reconstruir(8, 3L, 10, 7, INICIO, FIN, estado, null, AHORA, null, null);
    }

    // ---------------------------------------------------------------- Alta

    @Test
    @DisplayName("Una reserva nueva queda reservada, con notas recortadas y la hora de creación")
    void createsNewReservation() {
        Reserva reserva = newReservation();

        assertNull(reserva.getId());
        assertEquals(3L, reserva.getIdCliente());
        assertEquals(10, reserva.getIdServicio());
        assertEquals(7, reserva.getIdProveedor());
        assertEquals(INICIO, reserva.getFechaInicio());
        assertEquals(FIN, reserva.getFechaFin());
        assertEquals(EstadoReserva.RESERVADO, reserva.getEstado());
        assertEquals("Traer toalla", reserva.getNotas());
        assertEquals(AHORA, reserva.getCreatedAt());
        assertNull(reserva.getCanceladaEn());
        assertNull(reserva.getMotivoCancelacion());
        assertTrue(reserva.estaActiva());
    }

    @ParameterizedTest(name = "notas = [{0}]")
    @NullSource
    @ValueSource(strings = {"", "   "})
    @DisplayName("Notas nulas o en blanco se guardan como nulas")
    void blankNotesBecomeNull(String notas) {
        assertNull(Reserva.nueva(3L, 10, 7, INICIO, FIN, notas, AHORA).getNotas());
    }

    @Test
    @DisplayName("Acepta notas de 500 caracteres y rechaza notas más largas")
    void notesLengthLimit() {
        assertEquals(500, Reserva.nueva(3L, 10, 7, INICIO, FIN, "a".repeat(500), AHORA).getNotas().length());

        DatosReservaInvalidosException ex = assertThrows(DatosReservaInvalidosException.class,
                () -> Reserva.nueva(3L, 10, 7, INICIO, FIN, "a".repeat(501), AHORA));
        assertEquals("Las notas no pueden superar los 500 caracteres", ex.getMessage());
    }

    @Test
    @DisplayName("Permite reservar justo en la hora actual y rechaza una fecha pasada")
    void rejectsPastStart() {
        assertEquals(AHORA, Reserva.nueva(3L, 10, 7, AHORA, AHORA.plusHours(1), null, AHORA).getFechaInicio());
        assertThrows(FechaEnElPasadoException.class,
                () -> Reserva.nueva(3L, 10, 7, AHORA.minusMinutes(1), AHORA.plusHours(1), null, AHORA));
    }

    @Test
    @DisplayName("Rechaza fechas nulas, una fecha de fin que no sea posterior o una hora actual nula")
    void rejectsInvalidDates() {
        assertThrows(DatosReservaInvalidosException.class, () -> Reserva.nueva(3L, 10, 7, null, FIN, null, AHORA));
        assertThrows(DatosReservaInvalidosException.class, () -> Reserva.nueva(3L, 10, 7, INICIO, null, null, AHORA));
        DatosReservaInvalidosException ex = assertThrows(DatosReservaInvalidosException.class,
                () -> Reserva.nueva(3L, 10, 7, INICIO, INICIO, null, AHORA));
        assertEquals("fechaFin debe ser posterior a fechaInicio", ex.getMessage());
        assertThrows(NullPointerException.class, () -> Reserva.nueva(3L, 10, 7, INICIO, FIN, null, null));
    }

    @Test
    @DisplayName("Rechaza una reserva sin cliente, servicio, proveedor, fechas o estado")
    void rejectsMissingRequiredData() {
        assertThrows(NullPointerException.class, () -> Reserva.reconstruir(1, null, 10, 7, INICIO, FIN, EstadoReserva.RESERVADO, null, AHORA, null, null));
        assertThrows(NullPointerException.class, () -> Reserva.reconstruir(1, 3L, null, 7, INICIO, FIN, EstadoReserva.RESERVADO, null, AHORA, null, null));
        assertThrows(NullPointerException.class, () -> Reserva.reconstruir(1, 3L, 10, null, INICIO, FIN, EstadoReserva.RESERVADO, null, AHORA, null, null));
        assertThrows(NullPointerException.class, () -> Reserva.reconstruir(1, 3L, 10, 7, null, FIN, EstadoReserva.RESERVADO, null, AHORA, null, null));
        assertThrows(NullPointerException.class, () -> Reserva.reconstruir(1, 3L, 10, 7, INICIO, null, EstadoReserva.RESERVADO, null, AHORA, null, null));
        assertThrows(NullPointerException.class, () -> Reserva.reconstruir(1, 3L, 10, 7, INICIO, FIN, null, null, AHORA, null, null));
    }

    @Test
    @DisplayName("Reconstruye una reserva cancelada en el pasado con todos sus datos")
    void rebuildsPastCancelledReservation() {
        LocalDateTime pasado = LocalDateTime.of(2020, 1, 1, 9, 0);

        Reserva reserva = Reserva.reconstruir(8, 3L, 10, 7, pasado, pasado.plusHours(1), EstadoReserva.CANCELADO,
                "Nota", pasado.minusDays(1), pasado.minusHours(5), "Viaje");

        assertEquals(8, reserva.getId());
        assertEquals("Nota", reserva.getNotas());
        assertEquals(pasado.minusHours(5), reserva.getCanceladaEn());
        assertEquals("Viaje", reserva.getMotivoCancelacion());
        assertFalse(reserva.estaActiva());
    }

    // ---------------------------------------------------------------- Cancelación del cliente

    @Test
    @DisplayName("El cliente cancela con la anticipación mínima exacta y el motivo recortado")
    void clientCancelsAtExactDeadline() {
        // Arrange
        Reserva reserva = withState(EstadoReserva.RESERVADO);
        LocalDateTime limite = INICIO.minusHours(2);

        // Act
        reserva.cancelar(limite, 2, "  Viaje  ");

        // Assert
        assertEquals(EstadoReserva.CANCELADO, reserva.getEstado());
        assertEquals(limite, reserva.getCanceladaEn());
        assertEquals("Viaje", reserva.getMotivoCancelacion());
        assertFalse(reserva.estaActiva());
    }

    @Test
    @DisplayName("El cliente no puede cancelar con menos anticipación de la mínima")
    void clientCannotCancelTooLate() {
        Reserva reserva = withState(EstadoReserva.RESERVADO);

        CancelacionFueraDeTiempoException ex = assertThrows(CancelacionFueraDeTiempoException.class,
                () -> reserva.cancelar(INICIO.minusHours(2).plusMinutes(1), 2, null));

        assertEquals(CancelacionFueraDeTiempoException.faltaPocoTiempo(8, 2).getMessage(), ex.getMessage());
        assertEquals(EstadoReserva.RESERVADO, reserva.getEstado());
    }

    @Test
    @DisplayName("El cliente no puede cancelar una reserva que ya empezó")
    void clientCannotCancelStartedReservation() {
        Reserva reserva = withState(EstadoReserva.RESERVADO);

        assertThrows(CancelacionFueraDeTiempoException.class, () -> reserva.cancelar(INICIO, 2, null));
        assertThrows(CancelacionFueraDeTiempoException.class, () -> reserva.cancelar(FIN, 2, null));
    }

    @Test
    @DisplayName("El cliente no puede cancelar una reserva ya cancelada o que no está reservada")
    void clientCannotCancelInactiveReservation() {
        ReservaNoCancelableException cancelada = assertThrows(ReservaNoCancelableException.class,
                () -> withState(EstadoReserva.CANCELADO).cancelar(AHORA, 2, null));
        ReservaNoCancelableException enProceso = assertThrows(ReservaNoCancelableException.class,
                () -> withState(EstadoReserva.EN_PROCESO).cancelar(AHORA, 2, null));

        assertEquals("La reserva 8 ya estaba cancelada.", cancelada.getMessage());
        assertEquals(ReservaNoCancelableException.noActiva(8, EstadoReserva.EN_PROCESO).getMessage(), enProceso.getMessage());
    }

    @Test
    @DisplayName("Rechaza un motivo de cancelación de más de 255 caracteres")
    void rejectsTooLongCancellationReason() {
        Reserva reserva = withState(EstadoReserva.RESERVADO);
        String motivo = "a".repeat(256);

        DatosReservaInvalidosException ex = assertThrows(DatosReservaInvalidosException.class,
                () -> reserva.cancelar(AHORA, 2, motivo));

        assertEquals("El motivo no puede superar los 255 caracteres", ex.getMessage());
        assertEquals(EstadoReserva.RESERVADO, reserva.getEstado());
    }

    @Test
    @DisplayName("Cancelar exige la hora actual")
    void cancelRequiresCurrentTime() {
        assertThrows(NullPointerException.class, () -> withState(EstadoReserva.RESERVADO).cancelar(null, 2, null));
        assertThrows(NullPointerException.class, () -> withState(EstadoReserva.RESERVADO).cancelarPorAdministrador(null, null));
    }

    // ---------------------------------------------------------------- Cancelación del administrador

    @ParameterizedTest(name = "estado = {0}")
    @EnumSource(value = EstadoReserva.class, names = {"RESERVADO", "EN_PROCESO"})
    @DisplayName("El administrador cancela una reserva activa aunque ya haya empezado")
    void adminCancelsActiveReservation(EstadoReserva estado) {
        // Arrange
        Reserva reserva = withState(estado);

        // Act
        reserva.cancelarPorAdministrador(INICIO.plusMinutes(10), "REVOCACION_ADMIN");

        // Assert
        assertEquals(EstadoReserva.CANCELADO, reserva.getEstado());
        assertEquals("REVOCACION_ADMIN", reserva.getMotivoCancelacion());
    }

    @Test
    @DisplayName("El administrador no puede cancelar una reserva cancelada o terminada")
    void adminCannotCancelInactiveReservation() {
        ReservaNoCancelableException cancelada = assertThrows(ReservaNoCancelableException.class,
                () -> withState(EstadoReserva.CANCELADO).cancelarPorAdministrador(AHORA, null));
        ReservaNoCancelableException completada = assertThrows(ReservaNoCancelableException.class,
                () -> withState(EstadoReserva.COMPLETADO).cancelarPorAdministrador(AHORA, null));

        assertEquals("La reserva 8 ya estaba cancelada.", cancelada.getMessage());
        assertEquals(ReservaNoCancelableException.noActiva(8, EstadoReserva.COMPLETADO).getMessage(), completada.getMessage());
    }

    // ---------------------------------------------------------------- Consultas

    @Test
    @DisplayName("Se solapa cuando los rangos se cruzan y no cuando solo se tocan en el borde")
    void overlaps() {
        Reserva reserva = newReservation();

        assertTrue(reserva.seSolapaCon(INICIO.plusMinutes(30), FIN.plusMinutes(30)));
        assertTrue(reserva.seSolapaCon(INICIO.minusMinutes(30), INICIO.plusMinutes(1)));
        assertFalse(reserva.seSolapaCon(FIN, FIN.plusHours(1)));
        assertFalse(reserva.seSolapaCon(INICIO.minusHours(1), INICIO));
    }

    @Test
    @DisplayName("Reconoce si la reserva pertenece al cliente")
    void belongsToClient() {
        assertTrue(newReservation().esDelCliente(3L));
        assertFalse(newReservation().esDelCliente(4L));
    }
}
