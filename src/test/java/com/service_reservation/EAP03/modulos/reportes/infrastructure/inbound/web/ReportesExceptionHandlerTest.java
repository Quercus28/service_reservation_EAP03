package com.service_reservation.EAP03.modulos.reportes.infrastructure.inbound.web;

import com.service_reservation.EAP03.modulos.reportes.domain.exception.PeriodoReporteInvalidoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ReportesExceptionHandlerTest {

    private final ReportesExceptionHandler handler = new ReportesExceptionHandler();

    @Test
    @DisplayName("Mapea PeriodoReporteInvalidoException a HTTP 400 con errorCode PERIODO_INVALIDO")
    void manejarPeriodoReporteInvalido() {
        String mensaje = "La fecha de fin no puede ser anterior a la fecha de inicio";
        PeriodoReporteInvalidoException ex = new PeriodoReporteInvalidoException(mensaje);

        ResponseEntity<Map<String, Object>> response = handler.manejarPeriodoReporteInvalido(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("PERIODO_INVALIDO", response.getBody().get("errorCode"));
        assertEquals(mensaje, response.getBody().get("message"));
        assertTrue(response.getBody().containsKey("traceId"));
    }
}
