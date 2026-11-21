package com.service_reservation.EAP03.modulos.reportes.infrastructure.inbound.web;

import com.service_reservation.EAP03.modulos.admin.domain.model.AccionAdmin;
import com.service_reservation.EAP03.modulos.admin.domain.model.EventoAuditoria;
import com.service_reservation.EAP03.modulos.admin.domain.model.ResultadoAuditoria;
import com.service_reservation.EAP03.modulos.admin.domain.ports.in.RegistrarAuditoriaUseCase;
import com.service_reservation.EAP03.modulos.identidad.infrastructure.inbound.security.UsuarioAutenticadoPrincipal;
import com.service_reservation.EAP03.modulos.reportes.domain.exception.ConsultaReporteInvalidaException;
import com.service_reservation.EAP03.modulos.reportes.domain.exception.FormatoParametroReporteInvalidoException;
import com.service_reservation.EAP03.modulos.reportes.domain.exception.ParametroReporteFaltanteException;
import com.service_reservation.EAP03.modulos.reportes.domain.exception.PeriodoReporteInvalidoException;
import com.service_reservation.EAP03.modulos.reportes.domain.exception.ReporteAjenoException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReportesExceptionHandlerTest {

    private final RegistrarAuditoriaUseCase auditoria = mock(RegistrarAuditoriaUseCase.class);
    private final ReportesExceptionHandler handler = new ReportesExceptionHandler(auditoria);
    private final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/reportes/ocupacion");

    @AfterEach
    void limpiarSesion() {
        SecurityContextHolder.clearContext();
    }

    private static void assertFormatoEstandar(ResponseEntity<Map<String, Object>> response, HttpStatus status,
                                              String errorCode, String mensaje) {
        assertEquals(status, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(List.of("errorCode", "message", "traceId"), List.copyOf(response.getBody().keySet()));
        assertEquals(errorCode, response.getBody().get("errorCode"));
        assertEquals(mensaje, response.getBody().get("message"));
    }

    @Test
    @DisplayName("Mapea PeriodoReporteInvalidoException a HTTP 400 con errorCode PERIODO_INVALIDO")
    void manejarPeriodoReporteInvalido() {
        String mensaje = "La fecha de fin no puede ser anterior a la fecha de inicio";

        assertFormatoEstandar(handler.manejarPeriodoReporteInvalido(new PeriodoReporteInvalidoException(mensaje)),
                HttpStatus.BAD_REQUEST, "PERIODO_INVALIDO", mensaje);
    }

    @Test
    @DisplayName("Una fecha faltante responde 400 VALIDACION_ENTRADA nombrando el campo")
    void manejarParametroFaltante() {
        assertFormatoEstandar(handler.manejarParametroFaltante(new ParametroReporteFaltanteException("endDate")),
                HttpStatus.BAD_REQUEST, "VALIDACION_ENTRADA", "El parámetro 'endDate' es obligatorio");
    }

    @Test
    @DisplayName("Una fecha mal escrita responde 400 FORMATO_PARAMETRO_INVALIDO, igual que el manejador global")
    void manejarFormatoInvalido() {
        assertFormatoEstandar(handler.manejarFormatoInvalido(new FormatoParametroReporteInvalidoException("startDate")),
                HttpStatus.BAD_REQUEST, "FORMATO_PARAMETRO_INVALIDO",
                "El parámetro 'startDate' tiene un formato o valor inválido");
    }

    @Test
    @DisplayName("Un parámetro desconocido o repetido responde 400 CONSULTA_INVALIDA con mensaje fijo y sin auditoría en base")
    void manejarConsultaInvalida() {
        ConsultaReporteInvalidaException ex =
                new ConsultaReporteInvalidaException(ConsultaReporteInvalidaException.Motivo.PARAMETRO_DESCONOCIDO);

        assertFormatoEstandar(handler.manejarConsultaInvalida(ex, request),
                HttpStatus.BAD_REQUEST, "CONSULTA_INVALIDA", "La solicitud de consulta no es válida.");
        verifyNoInteractions(auditoria);
    }

    @Test
    @DisplayName("Un reporte ajeno responde 403 ACCESO_NO_AUTORIZADO y queda en la auditoría solo con el id del usuario")
    void manejarReporteAjeno() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                new UsuarioAutenticadoPrincipal(25L, "prov@test.com", List.of("PROVEEDOR")), null, List.of()));
        request.setQueryString("startDate=2026-11-01&endDate=2026-11-30&proveedorId=9");

        assertFormatoEstandar(handler.manejarReporteAjeno(new ReporteAjenoException(), request),
                HttpStatus.FORBIDDEN, "ACCESO_NO_AUTORIZADO", "No tiene privilegios para realizar esta operación");

        ArgumentCaptor<EventoAuditoria> evento = ArgumentCaptor.forClass(EventoAuditoria.class);
        verify(auditoria).ejecutar(evento.capture());
        assertEquals(new EventoAuditoria(25L, null, AccionAdmin.REPORTE_AJENO_DENEGADO, ResultadoAuditoria.DENEGADO,
                "GET /api/reportes/ocupacion", "motivo=reporte de otro proveedor"), evento.getValue());
    }

    @Test
    @DisplayName("Si la auditoría falla, el proveedor igual recibe su 403")
    void reporteAjenoConAuditoriaCaida() {
        doThrow(new IllegalStateException("base caída")).when(auditoria).ejecutar(any());

        ResponseEntity<Map<String, Object>> response = handler.manejarReporteAjeno(new ReporteAjenoException(), request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }
}
