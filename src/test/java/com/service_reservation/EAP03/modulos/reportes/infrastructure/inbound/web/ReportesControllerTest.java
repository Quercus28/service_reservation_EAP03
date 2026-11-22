package com.service_reservation.EAP03.modulos.reportes.infrastructure.inbound.web;

import com.service_reservation.EAP03.modulos.identidad.domain.ports.in.ConsultarProveedorUseCase;
import com.service_reservation.EAP03.modulos.identidad.infrastructure.inbound.security.UsuarioAutenticadoPrincipal;
import com.service_reservation.EAP03.modulos.reportes.domain.exception.ReporteAjenoException;
import com.service_reservation.EAP03.modulos.reportes.domain.model.ConsultaOcupacionComando;
import com.service_reservation.EAP03.modulos.reportes.domain.model.OcupacionServicio;
import com.service_reservation.EAP03.modulos.reportes.domain.model.PeriodoReporte;
import com.service_reservation.EAP03.modulos.reportes.domain.model.ReporteOcupacion;
import com.service_reservation.EAP03.modulos.reportes.domain.ports.in.ObtenerReporteOcupacionUseCase;
import com.service_reservation.EAP03.modulos.reportes.infrastructure.inbound.web.dto.ReporteOcupacionResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
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

    @InjectMocks
    private ReportesController controller;

    private static final Long USUARIO_ID = 25L;
    private static final Integer PROVEEDOR_ID = 7;
    private static final Authentication AUTH = new UsernamePasswordAuthenticationToken(
            new UsuarioAutenticadoPrincipal(USUARIO_ID, "prov@test.com", List.of("PROVEEDOR")),
            null, List.of()
    );

    private static MockHttpServletRequest peticion(String start, String end) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/reportes/ocupacion");
        request.addParameter("startDate", start);
        request.addParameter("endDate", end);
        return request;
    }

    @Test
    @DisplayName("Consulta exitosa de reporte para proveedor autenticado, con el proveedor sacado del JWT")
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
                peticion("2026-11-01", "2026-11-30"), AUTH);

        ArgumentCaptor<ConsultaOcupacionComando> comando = ArgumentCaptor.forClass(ConsultaOcupacionComando.class);
        verify(obtenerReporteOcupacionUseCase).ejecutar(comando.capture());
        assertEquals(new ConsultaOcupacionComando(PROVEEDOR_ID, start, end), comando.getValue());

        assertNotNull(response.getBody());
        assertEquals(200, response.getStatusCode().value());
        assertEquals(PROVEEDOR_ID, response.getBody().proveedorId());
        assertEquals(4, response.getBody().totalCitasGlobal());
        assertEquals(1, response.getBody().totalCanceladasGlobal());
        assertEquals(20.0, response.getBody().porcentajeCancelacionGlobal());
        assertEquals(1, response.getBody().servicios().size());
    }

    @Test
    @DisplayName("Si la petición nombra a un proveedor se corta antes de buscar el proveedor o generar el reporte")
    void bloquearParametroProveedor() {
        MockHttpServletRequest request = peticion("2026-11-01", "2026-11-30");
        request.addParameter("providerId", "999");

        assertThrows(ReporteAjenoException.class, () -> controller.consultarOcupacion(request, AUTH));
        verifyNoInteractions(obtenerReporteOcupacionUseCase, consultarProveedorUseCase);
    }

    @Test
    @DisplayName("Bloquea la petición si el usuario autenticado no tiene un perfil de proveedor asociado")
    void bloquearUsuarioSinPerfilProveedor() {
        when(consultarProveedorUseCase.buscarIdProveedorPorUsuario(USUARIO_ID))
                .thenReturn(Optional.empty());

        MockHttpServletRequest request = peticion("2026-11-01", "2026-11-30");

        AccessDeniedException ex = assertThrows(
                AccessDeniedException.class,
                () -> controller.consultarOcupacion(request, AUTH)
        );

        assertEquals("El usuario autenticado no tiene un perfil de proveedor asociado", ex.getMessage());
        verifyNoInteractions(obtenerReporteOcupacionUseCase);
    }

    @Test
    @DisplayName("Bloquea la petición si no hay autenticación válida")
    void bloquearSinAutenticacion() {
        MockHttpServletRequest request = peticion("2026-11-01", "2026-11-30");

        assertThrows(
                AccessDeniedException.class,
                () -> controller.consultarOcupacion(request, null)
        );
    }
}
