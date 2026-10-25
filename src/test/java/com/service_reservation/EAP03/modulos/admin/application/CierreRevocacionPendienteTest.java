package com.service_reservation.EAP03.modulos.admin.application;

import com.service_reservation.EAP03.modulos.admin.domain.model.AccionAdmin;
import com.service_reservation.EAP03.modulos.admin.domain.model.EstadoRevocacion;
import com.service_reservation.EAP03.modulos.admin.domain.model.RegistroAuditoria;
import com.service_reservation.EAP03.modulos.admin.domain.model.RevocacionPermiso;
import com.service_reservation.EAP03.modulos.admin.domain.model.RolOperativo;
import com.service_reservation.EAP03.modulos.admin.domain.model.SolicitudRevocacion;
import com.service_reservation.EAP03.modulos.admin.domain.ports.out.AuditoriaPort;
import com.service_reservation.EAP03.modulos.admin.domain.ports.out.ReservasPort;
import com.service_reservation.EAP03.modulos.admin.domain.ports.out.RevocacionPermisoRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CierreRevocacionPendienteTest {

    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 10, 10, 9, 0);

    @Mock private RevocacionPermisoRepositoryPort revocacionRepository;
    @Mock private ReservasPort reservasPort;
    @Mock private AuditoriaPort auditoriaPort;

    private CierreRevocacionPendiente cierre;
    private RevocacionPermiso revocacion;

    @BeforeEach
    void setUp() {
        cierre = new CierreRevocacionPendiente(revocacionRepository, reservasPort, auditoriaPort);
        revocacion = new RevocacionPermiso(7L, new SolicitudRevocacion(42L, RolOperativo.PROVEEDOR, "Fraude", 1L, INICIO), EstadoRevocacion.PENDIENTE_CANCELACION,
                INICIO.plusDays(5), null, 0);
    }

    private RegistroAuditoria auditEntry() {
        ArgumentCaptor<RegistroAuditoria> captor = ArgumentCaptor.forClass(RegistroAuditoria.class);
        verify(auditoriaPort).registrar(captor.capture());
        return captor.getValue();
    }

    @Test
    @DisplayName("Completa la revocación cuando el proveedor ya no tiene reservas activas")
    void completesWhenNoActiveReservations() {
        // Arrange
        LocalDateTime ahora = INICIO.plusDays(2);
        when(reservasPort.contarReservasActivasDeProveedor(42L)).thenReturn(0);

        // Act
        boolean completada = cierre.procesar(revocacion, ahora);

        // Assert
        assertTrue(completada);
        assertEquals(EstadoRevocacion.COMPLETADA, revocacion.getEstado());
        assertEquals(0, revocacion.getReservasCanceladas());
        verify(revocacionRepository).guardar(revocacion);
        RegistroAuditoria auditoria = auditEntry();
        assertEquals(AccionAdmin.REVOCACION_COMPLETADA, auditoria.accion());
        assertNull(auditoria.idActor());
        assertEquals("revocacion=7, el proveedor no tiene reservas activas", auditoria.detalle());
        verify(reservasPort, never()).cancelarReservasActivasDeProveedor(any(), any());
    }

    @Test
    @DisplayName("Con el plazo vencido cancela las reservas activas y fuerza la revocación")
    void forcesRevocationWhenDeadlineExpired() {
        // Arrange
        LocalDateTime ahora = INICIO.plusDays(5);
        when(reservasPort.contarReservasActivasDeProveedor(42L)).thenReturn(2);
        when(reservasPort.cancelarReservasActivasDeProveedor(42L, RevocacionPermiso.MOTIVO_CANCELACION_RESERVAS)).thenReturn(2);

        // Act
        boolean completada = cierre.procesar(revocacion, ahora);

        // Assert
        assertTrue(completada);
        assertEquals(2, revocacion.getReservasCanceladas());
        verify(revocacionRepository).guardar(revocacion);
        RegistroAuditoria auditoria = auditEntry();
        assertEquals(AccionAdmin.REVOCACION_FORZADA, auditoria.accion());
        assertEquals("revocacion=7, plazo vencido el " + INICIO.plusDays(5) + ", reservasCanceladasForzosamente=2",
                auditoria.detalle());
    }

    @Test
    @DisplayName("Dentro del plazo con reservas activas no hace nada")
    void doesNothingWithinDeadline() {
        when(reservasPort.contarReservasActivasDeProveedor(42L)).thenReturn(2);

        assertFalse(cierre.procesar(revocacion, INICIO.plusDays(1)));
        assertTrue(revocacion.estaPendiente());
        verifyNoInteractions(revocacionRepository, auditoriaPort);
    }
}
