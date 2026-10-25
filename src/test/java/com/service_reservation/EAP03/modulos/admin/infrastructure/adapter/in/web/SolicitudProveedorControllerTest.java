package com.service_reservation.EAP03.modulos.admin.infrastructure.adapter.in.web;

import com.service_reservation.EAP03.modulos.admin.domain.model.ActualizarProveedorPerfilComando;
import com.service_reservation.EAP03.modulos.admin.domain.model.EstadoSolicitud;
import com.service_reservation.EAP03.modulos.admin.domain.model.ProveedorPerfilDTO;
import com.service_reservation.EAP03.modulos.admin.domain.model.SolicitudAprobacionProveedor;
import com.service_reservation.EAP03.modulos.admin.domain.ports.in.GestionarSolicitudesProveedorUseCase;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SolicitudProveedorControllerTest {

    private static final UsuarioAutenticadoPrincipal PROVEEDOR = new UsuarioAutenticadoPrincipal(42L, "prov@test.com", List.of("PROVEEDOR"));
    private static final UsuarioAutenticadoPrincipal ADMIN = new UsuarioAutenticadoPrincipal(1L, "admin@test.com", List.of("ADMIN"));
    private static final SolicitudAprobacionProveedor SOLICITUD =
            SolicitudAprobacionProveedor.nueva(42L, LocalDateTime.of(2026, 10, 17, 10, 0));
    private static final ProveedorPerfilDTO PERFIL = new ProveedorPerfilDTO(42L, "Empresa SAS", "900123-1", "3001234567");

    @Mock private GestionarSolicitudesProveedorUseCase useCase;

    @InjectMocks private SolicitudProveedorController controller;

    @Test
    @DisplayName("El proveedor autenticado solicita su aprobación")
    void providerRequestsApproval() {
        when(useCase.registrarSolicitud(42L)).thenReturn(SOLICITUD);

        ResponseEntity<SolicitudAprobacionProveedor> response = controller.solicitarAprobacion(PROVEEDOR);

        assertEquals(200, response.getStatusCode().value());
        assertSame(SOLICITUD, response.getBody());
    }

    @Test
    @DisplayName("El proveedor autenticado reenvía su solicitud")
    void providerResubmitsRequest() {
        when(useCase.reenviarSolicitud(42L)).thenReturn(SOLICITUD);

        assertSame(SOLICITUD, controller.reenviarSolicitud(PROVEEDOR).getBody());
    }

    @Test
    @DisplayName("El proveedor autenticado consulta su última solicitud")
    void providerQueriesOwnRequest() {
        when(useCase.consultarUltimaSolicitud(42L)).thenReturn(SOLICITUD);

        assertSame(SOLICITUD, controller.consultarMiSolicitud(PROVEEDOR).getBody());
    }

    @Test
    @DisplayName("El administrador lista las solicitudes por estado")
    void adminListsRequests() {
        when(useCase.listar(EstadoSolicitud.PENDIENTE)).thenReturn(List.of(SOLICITUD));

        assertEquals(List.of(SOLICITUD), controller.listar(EstadoSolicitud.PENDIENTE).getBody());
    }

    @Test
    @DisplayName("El administrador autenticado aprueba una solicitud")
    void adminApprovesRequest() {
        when(useCase.aprobar(1L, 50L)).thenReturn(SOLICITUD);

        assertSame(SOLICITUD, controller.aprobar(50L, ADMIN).getBody());
    }

    @Test
    @DisplayName("El administrador autenticado rechaza una solicitud con motivo")
    void adminRejectsRequest() {
        when(useCase.rechazar(1L, 50L, "Documentos incompletos")).thenReturn(SOLICITUD);

        assertSame(SOLICITUD, controller.rechazar(50L,
                new SolicitudProveedorController.RechazoRequest("Documentos incompletos"), ADMIN).getBody());
    }

    @Test
    @DisplayName("El administrador consulta el perfil de un proveedor")
    void adminQueriesProfile() {
        when(useCase.consultarPerfilProveedor(42L)).thenReturn(PERFIL);

        assertEquals(PERFIL, controller.consultarPerfilProveedor(42L).getBody());
    }

    @Test
    @DisplayName("El administrador autenticado actualiza el perfil de un proveedor")
    void adminUpdatesProfile() {
        // Arrange
        when(useCase.actualizarPerfilProveedor(
                new ActualizarProveedorPerfilComando(1L, 42L, "Empresa SAS", "900123-1", "3001234567"))).thenReturn(PERFIL);

        // Act
        ResponseEntity<ProveedorPerfilDTO> response = controller.actualizarPerfilProveedor(42L,
                new SolicitudProveedorController.ActualizarPerfilProveedorRequest("Empresa SAS", "900123-1", "3001234567"), ADMIN);

        // Assert
        assertEquals(PERFIL, response.getBody());
    }
}
