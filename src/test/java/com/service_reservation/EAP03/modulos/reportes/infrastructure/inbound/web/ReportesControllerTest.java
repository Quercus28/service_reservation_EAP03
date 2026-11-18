package com.service_reservation.EAP03.modulos.reportes.infrastructure.inbound.web;

import com.service_reservation.EAP03.modulos.identidad.domain.ports.in.ConsultarProveedorUseCase;
import com.service_reservation.EAP03.modulos.identidad.infrastructure.inbound.security.UsuarioAutenticadoPrincipal;
import com.service_reservation.EAP03.modulos.reportes.domain.model.ConsultaOcupacionComando;
import com.service_reservation.EAP03.modulos.reportes.domain.model.OcupacionServicio;
import com.service_reservation.EAP03.modulos.reportes.domain.model.PeriodoReporte;
import com.service_reservation.EAP03.modulos.reportes.domain.model.ReporteOcupacion;
import com.service_reservation.EAP03.modulos.reportes.domain.ports.in.ObtenerReporteOcupacionUseCase;
import com.service_reservation.EAP03.modulos.reportes.infrastructure.inbound.web.dto.ReporteOcupacionResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportesControllerTest {

    @Mock
    private ObtenerReporteOcupacionUseCase obtenerReporteOcupacionUseCase;

    @Mock
    private ConsultarProveedorUseCase consultarProveedorUseCase;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private ReportesController controller;

    private static final Long USUARIO_ID = 25L;
    private static final Integer PROVEEDOR_ID = 7;
    private static final Authentication AUTH = new UsernamePasswordAuthenticationToken(
            new UsuarioAutenticadoPrincipal(USUARIO_ID, "prov@test.com", List.of("PROVEEDOR")),
            null, List.of()
    );

    @Test
    @DisplayName("Consulta exitosa de reporte para proveedor autenticado")
    void consultarOcupacionExitoso() {
        LocalDate start = LocalDate.of(2026, 11, 1);
        LocalDate end = LocalDate.of(2026, 11, 30);

        when(consultarProveedorUseCase.buscarIdProveedorPorUsuario(USUARIO_ID))
                .thenReturn(Optional.of(PROVEEDOR_ID));

        OcupacionServicio os = OcupacionServicio.builder(1, "Servicio A")
                .totalCitas(4)
                .horasOcupadas(4.0)
                .horasDisponibles(20.0)
                .porcentajeOcupacion(20.0)
                .citasCanceladas(1)
                .porcentajeCancelacion(20.0)
                .build();

        ReporteOcupacion reporte = ReporteOcupacion.builder(PROVEEDOR_ID, new PeriodoReporte(start, end))
                .totalCitasGlobal(4)
                .horasOcupadasGlobal(4.0)
                .horasDisponiblesGlobal(20.0)
                .porcentajeOcupacionGlobal(20.0)
                .totalCanceladasGlobal(1)
                .porcentajeCancelacionGlobal(20.0)
                .detalleServicios(List.of(os))
                .build();

        when(obtenerReporteOcupacionUseCase.ejecutar(any(ConsultaOcupacionComando.class)))
                .thenReturn(reporte);

        ResponseEntity<ReporteOcupacionResponseDTO> response = controller.consultarOcupacion(
                start, end, AUTH, request);

        assertNotNull(response.getBody());
        assertEquals(200, response.getStatusCode().value());
        assertEquals(PROVEEDOR_ID, response.getBody().proveedorId());
        assertEquals(4, response.getBody().totalCitasGlobal());
        assertEquals(1, response.getBody().totalCanceladasGlobal());
        assertEquals(20.0, response.getBody().porcentajeCancelacionGlobal());
        assertEquals(1, response.getBody().servicios().size());
    }

    @Test
    @DisplayName("Bloquea la petición con AccessDeniedException si se intenta enviar parámetro sospechoso providerId")
    void bloquearParametroProhibido() {
        LocalDate start = LocalDate.of(2026, 11, 1);
        LocalDate end = LocalDate.of(2026, 11, 30);

        when(request.getParameter("providerId")).thenReturn("999");

        AccessDeniedException ex = assertThrows(
                AccessDeniedException.class,
                () -> controller.consultarOcupacion(start, end, AUTH, request)
        );

        assertEquals("No está permitido especificar el identificador del proveedor en la petición", ex.getMessage());
        verifyNoInteractions(obtenerReporteOcupacionUseCase);
    }

    @Test
    @DisplayName("Bloquea la petición si el usuario autenticado no tiene un perfil de proveedor asociado")
    void bloquearUsuarioSinPerfilProveedor() {
        LocalDate start = LocalDate.of(2026, 11, 1);
        LocalDate end = LocalDate.of(2026, 11, 30);

        when(consultarProveedorUseCase.buscarIdProveedorPorUsuario(USUARIO_ID))
                .thenReturn(Optional.empty());

        AccessDeniedException ex = assertThrows(
                AccessDeniedException.class,
                () -> controller.consultarOcupacion(start, end, AUTH, request)
        );

        assertEquals("El usuario autenticado no tiene un perfil de proveedor asociado", ex.getMessage());
        verifyNoInteractions(obtenerReporteOcupacionUseCase);
    }

    @Test
    @DisplayName("Bloquea la petición si no hay autenticación válida")
    void bloquearSinAutenticacion() {
        LocalDate start = LocalDate.of(2026, 11, 1);
        LocalDate end = LocalDate.of(2026, 11, 30);

        assertThrows(
                AccessDeniedException.class,
                () -> controller.consultarOcupacion(start, end, null, request)
        );
    }
}
