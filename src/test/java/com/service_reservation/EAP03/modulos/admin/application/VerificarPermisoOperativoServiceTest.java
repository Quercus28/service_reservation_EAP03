package com.service_reservation.EAP03.modulos.admin.application;

import com.service_reservation.EAP03.modulos.admin.domain.model.RevocacionPermiso;
import com.service_reservation.EAP03.modulos.admin.domain.model.RolOperativo;
import com.service_reservation.EAP03.modulos.admin.domain.ports.out.PermisoOperativoPort;
import com.service_reservation.EAP03.modulos.admin.domain.ports.out.RevocacionPermisoRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerificarPermisoOperativoServiceTest {

    @Mock private PermisoOperativoPort permisoOperativoPort;
    @Mock private RevocacionPermisoRepositoryPort revocacionRepository;

    @InjectMocks private VerificarPermisoOperativoService service;

    @Test
    @DisplayName("Tiene permiso según el estado guardado del rol")
    void hasPermissionFromStoredState() {
        when(permisoOperativoPort.estadoPermiso(1L, RolOperativo.CLIENTE)).thenReturn(Optional.of(true));
        when(permisoOperativoPort.estadoPermiso(2L, RolOperativo.CLIENTE)).thenReturn(Optional.of(false));

        assertTrue(service.tienePermisoOperativo(1L, RolOperativo.CLIENTE));
        assertFalse(service.tienePermisoOperativo(2L, RolOperativo.CLIENTE));
    }

    @Test
    @DisplayName("Sin el rol, sin usuario o sin rol no tiene permiso")
    void noPermissionWithoutRoleOrData() {
        when(permisoOperativoPort.estadoPermiso(1L, RolOperativo.PROVEEDOR)).thenReturn(Optional.empty());

        assertFalse(service.tienePermisoOperativo(1L, RolOperativo.PROVEEDOR));
        assertFalse(service.tienePermisoOperativo(null, RolOperativo.PROVEEDOR));
        assertFalse(service.tienePermisoOperativo(1L, null));
    }

    @Test
    @DisplayName("Puede cancelar reservas si tiene permiso activo")
    void canCancelWithActivePermission() {
        when(permisoOperativoPort.estadoPermiso(1L, RolOperativo.PROVEEDOR)).thenReturn(Optional.of(true));

        assertTrue(service.puedeCancelarReservas(1L, RolOperativo.PROVEEDOR));
        verifyNoInteractions(revocacionRepository);
    }

    @Test
    @DisplayName("Puede cancelar reservas durante el plazo de gracia de una revocación")
    void canCancelDuringGracePeriod() {
        when(permisoOperativoPort.estadoPermiso(1L, RolOperativo.PROVEEDOR)).thenReturn(Optional.of(false));
        when(revocacionRepository.buscarPendiente(1L, RolOperativo.PROVEEDOR)).thenReturn(Optional.of(
                RevocacionPermiso.conPlazoDeGracia(1L, "Fraude", 9L, LocalDateTime.of(2026, 10, 17, 10, 0))));

        assertTrue(service.puedeCancelarReservas(1L, RolOperativo.PROVEEDOR));
    }

    @Test
    @DisplayName("No puede cancelar reservas sin permiso ni revocación pendiente")
    void cannotCancelWithoutPermissionOrPendingRevocation() {
        when(permisoOperativoPort.estadoPermiso(1L, RolOperativo.PROVEEDOR)).thenReturn(Optional.of(false));
        when(revocacionRepository.buscarPendiente(1L, RolOperativo.PROVEEDOR)).thenReturn(Optional.empty());

        assertFalse(service.puedeCancelarReservas(1L, RolOperativo.PROVEEDOR));
    }

    @Test
    @DisplayName("Sin usuario o sin rol no puede cancelar reservas")
    void cannotCancelWithoutData() {
        assertFalse(service.puedeCancelarReservas(null, RolOperativo.PROVEEDOR));
        assertFalse(service.puedeCancelarReservas(1L, null));
        verifyNoInteractions(permisoOperativoPort, revocacionRepository);
    }
}
