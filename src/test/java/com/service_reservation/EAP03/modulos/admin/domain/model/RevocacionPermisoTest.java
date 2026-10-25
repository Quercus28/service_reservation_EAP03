package com.service_reservation.EAP03.modulos.admin.domain.model;

import com.service_reservation.EAP03.modulos.admin.domain.exception.DatosAdminInvalidosException;
import com.service_reservation.EAP03.modulos.admin.domain.exception.OperacionAdminInvalidaException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class RevocacionPermisoTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 10, 17, 10, 0);

    private static RevocacionPermiso pending() {
        return RevocacionPermiso.conPlazoDeGracia(42L, "Incumplimiento reiterado", 1L, AHORA);
    }

    @Test
    @DisplayName("Una revocación inmediata queda completada en el mismo instante")
    void immediateRevocationIsCompleted() {
        // Act
        RevocacionPermiso revocacion = RevocacionPermiso.inmediata(42L, RolOperativo.CLIENTE, "  Fraude  ", 1L, AHORA, 3);

        // Assert
        assertNull(revocacion.getId());
        assertEquals(42L, revocacion.getIdUsuario());
        assertEquals(RolOperativo.CLIENTE, revocacion.getRol());
        assertEquals(EstadoRevocacion.COMPLETADA, revocacion.getEstado());
        assertEquals("Fraude", revocacion.getMotivo());
        assertEquals(1L, revocacion.getIdAdmin());
        assertEquals(AHORA, revocacion.getFechaSolicitud());
        assertNull(revocacion.getFechaLimite());
        assertEquals(AHORA, revocacion.getFechaCierre());
        assertEquals(3, revocacion.getReservasCanceladas());
        assertFalse(revocacion.estaPendiente());
    }

    @Test
    @DisplayName("Una revocación con plazo de gracia queda pendiente cinco días")
    void graceRevocationIsPendingFiveDays() {
        // Act
        RevocacionPermiso revocacion = pending();

        // Assert
        assertEquals(RolOperativo.PROVEEDOR, revocacion.getRol());
        assertEquals(EstadoRevocacion.PENDIENTE_CANCELACION, revocacion.getEstado());
        assertEquals(AHORA.plusDays(5), revocacion.getFechaLimite());
        assertNull(revocacion.getFechaCierre());
        assertEquals(0, revocacion.getReservasCanceladas());
        assertTrue(revocacion.estaPendiente());
    }

    @Test
    @DisplayName("Rechaza una revocación sin usuario, rol o administrador")
    void rejectsMissingRequiredData() {
        assertThrows(DatosAdminInvalidosException.class, () -> new RevocacionPermiso(null, null, RolOperativo.CLIENTE,
                EstadoRevocacion.COMPLETADA, "m", 1L, AHORA, null, AHORA, 0));
        assertThrows(DatosAdminInvalidosException.class, () -> new RevocacionPermiso(null, 42L, null,
                EstadoRevocacion.COMPLETADA, "m", 1L, AHORA, null, AHORA, 0));
        assertThrows(DatosAdminInvalidosException.class, () -> new RevocacionPermiso(null, 42L, RolOperativo.CLIENTE,
                EstadoRevocacion.COMPLETADA, "m", null, AHORA, null, AHORA, 0));
    }

    @Test
    @DisplayName("Rechaza un motivo inválido al crear la revocación")
    void rejectsInvalidReason() {
        assertThrows(DatosAdminInvalidosException.class,
                () -> RevocacionPermiso.conPlazoDeGracia(42L, "<script>", 1L, AHORA));
    }

    @Test
    @DisplayName("El plazo vence justo en la fecha límite y no antes")
    void deadlineExpiresAtLimit() {
        RevocacionPermiso revocacion = pending();

        assertFalse(revocacion.plazoVencido(AHORA.plusDays(5).minusSeconds(1)));
        assertTrue(revocacion.plazoVencido(AHORA.plusDays(5)));
        assertTrue(revocacion.plazoVencido(AHORA.plusDays(6)));
    }

    @Test
    @DisplayName("Sin fecha límite el plazo nunca vence")
    void noDeadlineNeverExpires() {
        RevocacionPermiso revocacion = RevocacionPermiso.inmediata(42L, RolOperativo.CLIENTE, "Fraude", 1L, AHORA, 0);

        assertFalse(revocacion.plazoVencido(AHORA.plusYears(1)));
    }

    @Test
    @DisplayName("Completar cierra la revocación pendiente con las reservas canceladas")
    void completesPendingRevocation() {
        // Arrange
        RevocacionPermiso revocacion = pending();
        LocalDateTime cierre = AHORA.plusDays(5);

        // Act
        revocacion.completar(cierre, 4);

        // Assert
        assertEquals(EstadoRevocacion.COMPLETADA, revocacion.getEstado());
        assertEquals(cierre, revocacion.getFechaCierre());
        assertEquals(4, revocacion.getReservasCanceladas());
    }

    @Test
    @DisplayName("Anular cierra la revocación pendiente como anulada")
    void cancelsPendingRevocation() {
        // Arrange
        RevocacionPermiso revocacion = pending();

        // Act
        revocacion.anular(AHORA.plusDays(1));

        // Assert
        assertEquals(EstadoRevocacion.ANULADA, revocacion.getEstado());
        assertEquals(AHORA.plusDays(1), revocacion.getFechaCierre());
    }

    @Test
    @DisplayName("No permite completar ni anular una revocación ya cerrada")
    void rejectsClosingAClosedRevocation() {
        // Arrange
        RevocacionPermiso cerrada = new RevocacionPermiso(9L, 42L, RolOperativo.PROVEEDOR, EstadoRevocacion.ANULADA,
                "Fraude", 1L, AHORA, AHORA.plusDays(5), AHORA.plusDays(1), 0);

        // Act
        OperacionAdminInvalidaException ex = assertThrows(OperacionAdminInvalidaException.class,
                () -> cerrada.completar(AHORA, 0));

        // Assert
        assertEquals("La revocación 9 ya está cerrada con estado ANULADA", ex.getMessage());
        assertThrows(OperacionAdminInvalidaException.class, () -> cerrada.anular(AHORA));
        assertEquals(9L, cerrada.getId());
    }
}
