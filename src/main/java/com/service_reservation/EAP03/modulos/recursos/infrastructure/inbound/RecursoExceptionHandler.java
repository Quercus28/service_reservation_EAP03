package com.service_reservation.EAP03.modulos.recursos.infrastructure.inbound;

import com.service_reservation.EAP03.modulos.recursos.domain.exception.BloqueoEnConflictoException;
import com.service_reservation.EAP03.modulos.recursos.domain.exception.BloqueoNoEncontradoException;
import com.service_reservation.EAP03.modulos.recursos.domain.exception.DatosInvalidosException;
import com.service_reservation.EAP03.modulos.recursos.domain.exception.DisponibilidadInsuficienteException;
import com.service_reservation.EAP03.modulos.recursos.domain.exception.HorarioNoEncontradoException;
import com.service_reservation.EAP03.modulos.recursos.domain.exception.HorarioSolapadoException;
import com.service_reservation.EAP03.modulos.recursos.domain.exception.RecursoDuplicadoException;
import com.service_reservation.EAP03.modulos.recursos.domain.exception.RecursoNoEncontradoException;
import com.service_reservation.EAP03.modulos.recursos.domain.exception.RecursoNoOperativoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@RestControllerAdvice(assignableTypes = {RecursoController.class, DisponibilidadRecursoController.class})
public class RecursoExceptionHandler {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> manejarNoEncontrado(RecursoNoEncontradoException ex) {
        return construir(HttpStatus.NOT_FOUND, "RECURSO_NO_ENCONTRADO", ex.getMessage());
    }

    @ExceptionHandler(DisponibilidadInsuficienteException.class)
    public ResponseEntity<Map<String, Object>> manejarDisponibilidad(DisponibilidadInsuficienteException ex) {
        return construir(HttpStatus.CONFLICT, "DISPONIBILIDAD_INSUFICIENTE", ex.getMessage());
    }

    @ExceptionHandler(RecursoDuplicadoException.class)
    public ResponseEntity<Map<String, Object>> manejarDuplicado(RecursoDuplicadoException ex) {
        return construir(HttpStatus.CONFLICT, "RECURSO_DUPLICADO", ex.getMessage());
    }

    @ExceptionHandler(HorarioNoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> manejarHorarioNoEncontrado(HorarioNoEncontradoException ex) {
        return construir(HttpStatus.NOT_FOUND, "HORARIO_NO_ENCONTRADO", ex.getMessage());
    }

    @ExceptionHandler(BloqueoNoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> manejarBloqueoNoEncontrado(BloqueoNoEncontradoException ex) {
        return construir(HttpStatus.NOT_FOUND, "BLOQUEO_NO_ENCONTRADO", ex.getMessage());
    }

    @ExceptionHandler(HorarioSolapadoException.class)
    public ResponseEntity<Map<String, Object>> manejarHorarioSolapado(HorarioSolapadoException ex) {
        return construir(HttpStatus.CONFLICT, "HORARIO_SOLAPADO", ex.getMessage());
    }

    @ExceptionHandler(BloqueoEnConflictoException.class)
    public ResponseEntity<Map<String, Object>> manejarBloqueoEnConflicto(BloqueoEnConflictoException ex) {
        return construir(HttpStatus.CONFLICT, "BLOQUEO_EN_CONFLICTO", ex.getMessage());
    }

    @ExceptionHandler(RecursoNoOperativoException.class)
    public ResponseEntity<Map<String, Object>> manejarNoOperativo(RecursoNoOperativoException ex) {
        return construir(HttpStatus.CONFLICT, "RECURSO_NO_OPERATIVO", ex.getMessage());
    }

    @ExceptionHandler(DatosInvalidosException.class)
    public ResponseEntity<Map<String, Object>> manejarDatosInvalidos(DatosInvalidosException ex) {
        return construir(HttpStatus.BAD_REQUEST, "DATOS_INVALIDOS", ex.getMessage());
    }

    private ResponseEntity<Map<String, Object>> construir(HttpStatus status, String errorCode, String mensaje) {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("errorCode", errorCode);
        cuerpo.put("message", mensaje);
        cuerpo.put("traceId", UUID.randomUUID().toString());
        return ResponseEntity.status(status).body(cuerpo);
    }
}
