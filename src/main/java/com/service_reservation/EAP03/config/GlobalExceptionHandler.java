// src/main/java/config/GlobalExceptionHandler.java
package com.service_reservation.EAP03.config;

import com.service_reservation.EAP03.modulos.identidad.application.exception.ReglaNegocioException;
import com.service_reservation.EAP03.modulos.identidad.domain.exception.CredencialesInvalidasException;
import com.service_reservation.EAP03.modulos.identidad.domain.exception.CuentaBloqueadaException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<Map<String, Object>> handleCredencialesInvalidas(CredencialesInvalidasException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("errorCode", "CREDENCIALES_INVALIDAS");
        error.put("message", ex.getMessage());
        error.put("traceId", UUID.randomUUID().toString());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(CuentaBloqueadaException.class)
    public ResponseEntity<Map<String, Object>> handleCuentaBloqueada(CuentaBloqueadaException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("errorCode", "CUENTA_BLOQUEADA");
        error.put("message", ex.getMessage());
        error.put("traceId", UUID.randomUUID().toString());
        return ResponseEntity.status(HttpStatus.LOCKED).body(error);
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<Map<String, Object>> handleReglaNegocio(ReglaNegocioException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("errorCode", "REGLA_NEGOCIO");
        error.put("message", ex.getMessage());
        error.put("traceId", UUID.randomUUID().toString());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidations(MethodArgumentNotValidException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("errorCode", "VALIDACION_ENTRADA");
        error.put("message", "Error en los datos enviados");
        
        Map<String, String> details = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(e -> details.put(e.getField(), e.getDefaultMessage()));
        if (ex.getBindingResult().getGlobalErrors().size() > 0) {
            ex.getBindingResult().getGlobalErrors().forEach(e -> details.put(e.getObjectName(), e.getDefaultMessage()));
        }
        
        error.put("details", details);
        error.put("traceId", UUID.randomUUID().toString());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}