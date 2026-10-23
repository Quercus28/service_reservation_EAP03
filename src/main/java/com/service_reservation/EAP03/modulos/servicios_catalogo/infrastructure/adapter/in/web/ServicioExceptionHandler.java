package com.service_reservation.EAP03.modulos.servicios_catalogo.infrastructure.adapter.in.web;

import com.service_reservation.EAP03.config.TraceIdFilter;
import com.service_reservation.EAP03.modulos.servicios_catalogo.domain.exception.DomainException;
import com.service_reservation.EAP03.modulos.servicios_catalogo.domain.exception.ServicioNoEncontradoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

// Mismo esquema de error que el resto de módulos: {errorCode, message, traceId}.
// Los errores de validación (@Valid) los responde GlobalExceptionHandler.
@RestControllerAdvice
public class ServicioExceptionHandler {

    @ExceptionHandler(ServicioNoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> manejarServicioNoEncontrado(ServicioNoEncontradoException ex) {
        return construir(HttpStatus.NOT_FOUND, "SERVICIO_NO_ENCONTRADO", ex.getMessage());
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<Map<String, Object>> manejarDomainException(DomainException ex) {
        return construir(HttpStatus.BAD_REQUEST, "SERVICIO_INVALIDO", ex.getMessage());
    }

    private ResponseEntity<Map<String, Object>> construir(HttpStatus status, String errorCode, String mensaje) {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("errorCode", errorCode);
        cuerpo.put("message", mensaje);
        cuerpo.put("traceId", TraceIdFilter.traceIdActual());
        return ResponseEntity.status(status).body(cuerpo);
    }
}
