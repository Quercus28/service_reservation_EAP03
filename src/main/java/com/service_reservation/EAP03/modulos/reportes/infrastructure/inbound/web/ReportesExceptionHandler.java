package com.service_reservation.EAP03.modulos.reportes.infrastructure.inbound.web;

import com.service_reservation.EAP03.config.TraceIdFilter;
import com.service_reservation.EAP03.modulos.reportes.domain.exception.PeriodoReporteInvalidoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Manejador de excepciones específico para el módulo de reportes.
 * Traduce las excepciones de dominio a respuestas HTTP coherentes con el estándar del proyecto.
 */
@RestControllerAdvice(basePackages = "com.service_reservation.EAP03.modulos.reportes")
public class ReportesExceptionHandler {

    @ExceptionHandler(PeriodoReporteInvalidoException.class)
    public ResponseEntity<Map<String, Object>> manejarPeriodoReporteInvalido(PeriodoReporteInvalidoException ex) {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("errorCode", "PERIODO_INVALIDO");
        cuerpo.put("message", ex.getMessage());
        cuerpo.put("traceId", TraceIdFilter.traceIdActual());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(cuerpo);
    }
}
