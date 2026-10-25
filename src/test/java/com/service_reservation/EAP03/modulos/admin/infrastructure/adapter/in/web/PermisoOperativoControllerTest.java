package com.service_reservation.EAP03.modulos.admin.infrastructure.adapter.in.web;

import com.service_reservation.EAP03.modulos.admin.domain.model.EstadoRevocacion;
import com.service_reservation.EAP03.modulos.admin.domain.model.PermisoOperativo;
import com.service_reservation.EAP03.modulos.admin.domain.model.RestaurarPermisoComando;
import com.service_reservation.EAP03.modulos.admin.domain.model.RevocacionPermiso;
import com.service_reservation.EAP03.modulos.admin.domain.model.RevocarPermisoComando;
import com.service_reservation.EAP03.modulos.admin.domain.model.RolOperativo;
import com.service_reservation.EAP03.modulos.admin.domain.ports.in.GestionarPermisosOperativosUseCase;
import com.service_reservation.EAP03.modulos.identidad.infrastructure.inbound.security.UsuarioAutenticadoPrincipal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PermisoOperativoControllerTest {

    private static final UsuarioAutenticadoPrincipal ADMIN = new UsuarioAutenticadoPrincipal(1L, "admin@test.com", List.of("ADMIN"));

    @Mock private GestionarPermisosOperativosUseCase useCase;

    @InjectMocks private PermisoOperativoController controller;

    @Test
    @DisplayName("Revocar usa el administrador autenticado y devuelve la revocación")
    void revokesWithAuthenticatedAdmin() {
        // Arrange
        RevocacionPermiso revocacion = RevocacionPermiso.conPlazoDeGracia(42L, "Fraude", 1L, LocalDateTime.of(2026, 10, 17, 10, 0));
        when(useCase.revocar(new RevocarPermisoComando(1L, 42L, RolOperativo.PROVEEDOR, "Fraude"))).thenReturn(revocacion);

        // Act
        ResponseEntity<RevocacionPermiso> response = controller.revocar(
                new PermisoOperativoController.RevocarRequest(42L, RolOperativo.PROVEEDOR, "Fraude"), ADMIN);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertSame(revocacion, response.getBody());
    }

    @Test
    @DisplayName("Restaurar usa el administrador autenticado y responde 200 sin cuerpo")
    void restoresWithAuthenticatedAdmin() {
        // Act
        ResponseEntity<Void> response = controller.restaurar(
                new PermisoOperativoController.RestaurarRequest(42L, RolOperativo.CLIENTE, "Apelación"), ADMIN);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertNull(response.getBody());
        verify(useCase).restaurar(new RestaurarPermisoComando(1L, 42L, RolOperativo.CLIENTE, "Apelación"));
    }

    @Test
    @DisplayName("Consulta los permisos operativos del usuario")
    void queriesPermissions() {
        List<PermisoOperativo> permisos = List.of(new PermisoOperativo(RolOperativo.CLIENTE, true, false));
        when(useCase.consultarPermisos(42L)).thenReturn(permisos);

        assertEquals(permisos, controller.consultarPermisos(42L).getBody());
    }

    @Test
    @DisplayName("Lista las revocaciones filtradas por estado")
    void listsRevocations() {
        when(useCase.listarRevocaciones(EstadoRevocacion.COMPLETADA)).thenReturn(List.of());

        assertEquals(List.of(), controller.listarRevocaciones(EstadoRevocacion.COMPLETADA).getBody());
    }
}
