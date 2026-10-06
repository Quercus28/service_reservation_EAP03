// src/main/java/config/GlobalExceptionHandler.java
package com.service_reservation.EAP03.config;

import com.service_reservation.EAP03.modulos.agendas_horarios.domain.exception.AgendaDuplicadaException;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.exception.AgendaNoEncontradaException;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.exception.DatosAgendaInvalidosException;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.exception.HorarioSolapadoException;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.exception.ServicioNoPerteneceAlProveedorException;
import com.service_reservation.EAP03.modulos.identidad.application.exception.ReglaNegocioException;
import com.service_reservation.EAP03.modulos.agendas_horarios.application.PersistenciaAgendasHorariosNoDisponibleException;
import com.service_reservation.EAP03.modulos.identidad.domain.exception.CredencialesInvalidasException;
import com.service_reservation.EAP03.modulos.identidad.domain.exception.CuentaBloqueadaException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import lombok.extern.slf4j.Slf4j;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
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

    @ExceptionHandler(HorarioSolapadoException.class)
    public ResponseEntity<Map<String, Object>> handleHorarioSolapado(
            HorarioSolapadoException ex) {

        Map<String, Object> error = new HashMap<>();
        error.put("errorCode", "REGLA_NEGOCIO");
        error.put("message", ex.getMessage());
        error.put("traceId", UUID.randomUUID().toString());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(error);
    }

    @ExceptionHandler(AgendaNoEncontradaException.class)
    public ResponseEntity<Map<String, Object>> handleAgendaNoEncontrada(AgendaNoEncontradaException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("errorCode", "AGENDA_NO_ENCONTRADA");
        error.put("message", ex.getMessage());
        error.put("traceId", UUID.randomUUID().toString());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(AgendaDuplicadaException.class)
    public ResponseEntity<Map<String, Object>> handleAgendaDuplicada(AgendaDuplicadaException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("errorCode", "AGENDA_DUPLICADA");
        error.put("message", ex.getMessage());
        error.put("traceId", UUID.randomUUID().toString());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(ServicioNoPerteneceAlProveedorException.class)
    public ResponseEntity<Map<String, Object>> handleServicioNoPerteneceAlProveedor(ServicioNoPerteneceAlProveedorException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("errorCode", "SERVICIO_NO_PERTENECE_AL_PROVEEDOR");
        error.put("message", ex.getMessage());
        error.put("traceId", UUID.randomUUID().toString());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(DatosAgendaInvalidosException.class)
    public ResponseEntity<Map<String, Object>> handleDatosAgendaInvalidos(DatosAgendaInvalidosException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("errorCode", "DATOS_AGENDA_INVALIDOS");
        error.put("message", ex.getMessage());
        error.put("traceId", UUID.randomUUID().toString());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(PersistenciaAgendasHorariosNoDisponibleException.class)
    public ResponseEntity<Map<String, Object>> handlePersistenciaNoDisponible(
            PersistenciaAgendasHorariosNoDisponibleException ex) {

        Map<String, Object> error = new HashMap<>();
        error.put("errorCode", "PERSISTENCIA_NO_DISPONIBLE");
        error.put("message", ex.getMessage());
        error.put("traceId", UUID.randomUUID().toString());

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(error);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request) {

        log.warn(
                "Intento de acceso no autorizado: metodo={}, ruta={}",
                request.getMethod(),
                request.getRequestURI()
        );

        Map<String, Object> error = new HashMap<>();
        error.put("errorCode", "ACCESO_NO_AUTORIZADO");
        error.put("message", "No tiene privilegios para realizar esta operación");
        error.put("traceId", UUID.randomUUID().toString());

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(error);
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