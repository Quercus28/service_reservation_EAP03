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
import com.service_reservation.EAP03.modulos.admin.domain.exception.AdminDominioException;
import com.service_reservation.EAP03.modulos.admin.domain.exception.DatosAdminInvalidosException;
import com.service_reservation.EAP03.modulos.admin.domain.exception.OperacionAdminInvalidaException;
import com.service_reservation.EAP03.modulos.admin.domain.exception.RecursoAdminNoEncontradoException;
import com.service_reservation.EAP03.modulos.admin.domain.model.IntentoAccesoDenegado;
import com.service_reservation.EAP03.modulos.admin.domain.ports.in.RegistrarAccesoDenegadoUseCase;
import com.service_reservation.EAP03.modulos.identidad.infrastructure.inbound.security.UsuarioAutenticadoPrincipal;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final ObjectProvider<RegistrarAccesoDenegadoUseCase> registrarAccesoDenegadoUseCaseProvider;

    public GlobalExceptionHandler(ObjectProvider<RegistrarAccesoDenegadoUseCase> registrarAccesoDenegadoUseCaseProvider) {
        this.registrarAccesoDenegadoUseCaseProvider = registrarAccesoDenegadoUseCaseProvider;
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<Map<String, Object>> handleCredencialesInvalidas(CredencialesInvalidasException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("errorCode", "CREDENCIALES_INVALIDAS");
        error.put("message", ex.getMessage());
        error.put("traceId", TraceIdFilter.traceIdActual());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(CuentaBloqueadaException.class)
    public ResponseEntity<Map<String, Object>> handleCuentaBloqueada(CuentaBloqueadaException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("errorCode", "CUENTA_BLOQUEADA");
        error.put("message", ex.getMessage());
        error.put("traceId", TraceIdFilter.traceIdActual());
        return ResponseEntity.status(HttpStatus.LOCKED).body(error);
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<Map<String, Object>> handleReglaNegocio(ReglaNegocioException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("errorCode", "REGLA_NEGOCIO");
        error.put("message", ex.getMessage());
        error.put("traceId", TraceIdFilter.traceIdActual());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(HorarioSolapadoException.class)
    public ResponseEntity<Map<String, Object>> handleHorarioSolapado(
            HorarioSolapadoException ex) {

        Map<String, Object> error = new HashMap<>();
        error.put("errorCode", "REGLA_NEGOCIO");
        error.put("message", ex.getMessage());
        error.put("traceId", TraceIdFilter.traceIdActual());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(error);
    }

    @ExceptionHandler(AgendaNoEncontradaException.class)
    public ResponseEntity<Map<String, Object>> handleAgendaNoEncontrada(AgendaNoEncontradaException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("errorCode", "AGENDA_NO_ENCONTRADA");
        error.put("message", ex.getMessage());
        error.put("traceId", TraceIdFilter.traceIdActual());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(AgendaDuplicadaException.class)
    public ResponseEntity<Map<String, Object>> handleAgendaDuplicada(AgendaDuplicadaException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("errorCode", "AGENDA_DUPLICADA");
        error.put("message", ex.getMessage());
        error.put("traceId", TraceIdFilter.traceIdActual());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(ServicioNoPerteneceAlProveedorException.class)
    public ResponseEntity<Map<String, Object>> handleServicioNoPerteneceAlProveedor(ServicioNoPerteneceAlProveedorException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("errorCode", "SERVICIO_NO_PERTENECE_AL_PROVEEDOR");
        error.put("message", ex.getMessage());
        error.put("traceId", TraceIdFilter.traceIdActual());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(DatosAgendaInvalidosException.class)
    public ResponseEntity<Map<String, Object>> handleDatosAgendaInvalidos(DatosAgendaInvalidosException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("errorCode", "DATOS_AGENDA_INVALIDOS");
        error.put("message", ex.getMessage());
        error.put("traceId", TraceIdFilter.traceIdActual());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(PersistenciaAgendasHorariosNoDisponibleException.class)
    public ResponseEntity<Map<String, Object>> handlePersistenciaNoDisponible(
            PersistenciaAgendasHorariosNoDisponibleException ex) {

        Map<String, Object> error = new HashMap<>();
        error.put("errorCode", "PERSISTENCIA_NO_DISPONIBLE");
        error.put("message", ex.getMessage());
        error.put("traceId", TraceIdFilter.traceIdActual());

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

        // Auditoría de intentos de acceso denegado a rutas administrativas
        if (request.getRequestURI().startsWith("/api/admin")) {
            RegistrarAccesoDenegadoUseCase auditUseCase = registrarAccesoDenegadoUseCaseProvider.getIfAvailable();
            if (auditUseCase != null) {
                // Solo se guarda el id del usuario (si se sabe), el método y la ruta: nada de
                // correo ni IP, para no dejar datos personales en la auditoría.
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                Long idUsuario = auth != null && auth.getPrincipal() instanceof UsuarioAutenticadoPrincipal principal
                        ? principal.getIdUsuario()
                        : null;
                auditUseCase.registrar(new IntentoAccesoDenegado(
                        LocalDateTime.now(),
                        idUsuario,
                        request.getMethod(),
                        request.getRequestURI()
                ));
            }
        }

        Map<String, Object> error = new HashMap<>();
        error.put("errorCode", "ACCESO_NO_AUTORIZADO");
        error.put("message", "No tiene privilegios para realizar esta operación");
        error.put("traceId", TraceIdFilter.traceIdActual());

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
        error.put("traceId", TraceIdFilter.traceIdActual());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(com.service_reservation.EAP03.modulos.availability.domain.exception.DisponibilidadInvalidaException.class)
    public ResponseEntity<Map<String, Object>> handleDisponibilidadInvalida(
            com.service_reservation.EAP03.modulos.availability.domain.exception.DisponibilidadInvalidaException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("errorCode", "PARAMETROS_INVALIDOS");
        error.put("message", ex.getMessage());
        error.put("traceId", TraceIdFilter.traceIdActual());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(com.service_reservation.EAP03.modulos.availability.domain.exception.RangoFechasExcedidoException.class)
    public ResponseEntity<Map<String, Object>> handleRangoFechasExcedido(
            com.service_reservation.EAP03.modulos.availability.domain.exception.RangoFechasExcedidoException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("errorCode", "RANGO_FECHAS_EXCEDIDO");
        error.put("message", ex.getMessage());
        error.put("traceId", TraceIdFilter.traceIdActual());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(com.service_reservation.EAP03.modulos.availability.domain.exception.ServicioParaDisponibilidadNoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> handleServicioNoEncontrado(
            com.service_reservation.EAP03.modulos.availability.domain.exception.ServicioParaDisponibilidadNoEncontradoException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("errorCode", "SERVICIO_NO_ENCONTRADO");
        error.put("message", ex.getMessage());
        error.put("traceId", TraceIdFilter.traceIdActual());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(
            org.springframework.web.method.annotation.MethodArgumentTypeMismatchException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("errorCode", "FORMATO_PARAMETRO_INVALIDO");
        error.put("message", "El parámetro '" + ex.getName() + "' tiene un formato o valor inválido");
        error.put("traceId", TraceIdFilter.traceIdActual());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(DatosAdminInvalidosException.class)
    public ResponseEntity<Map<String, Object>> handleDatosAdminInvalidos(DatosAdminInvalidosException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("errorCode", "DATOS_ADMIN_INVALIDOS");
        error.put("message", ex.getMessage());
        error.put("traceId", TraceIdFilter.traceIdActual());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(OperacionAdminInvalidaException.class)
    public ResponseEntity<Map<String, Object>> handleOperacionAdminInvalida(OperacionAdminInvalidaException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("errorCode", "OPERACION_ADMIN_INVALIDA");
        error.put("message", ex.getMessage());
        error.put("traceId", TraceIdFilter.traceIdActual());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(RecursoAdminNoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> handleRecursoAdminNoEncontrado(RecursoAdminNoEncontradoException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("errorCode", "RECURSO_ADMIN_NO_ENCONTRADO");
        error.put("message", ex.getMessage());
        error.put("traceId", TraceIdFilter.traceIdActual());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(AdminDominioException.class)
    public ResponseEntity<Map<String, Object>> handleAdminDominio(AdminDominioException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("errorCode", "ERROR_DOMINIO_ADMIN");
        error.put("message", ex.getMessage());
        error.put("traceId", TraceIdFilter.traceIdActual());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // JSON mal formado o cuerpo vacío. El mensaje de la excepción no se devuelve porque
    // trae nombres de clases internas.
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleCuerpoNoLegible(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {
        log.warn("Cuerpo de la petición no válido: metodo={}, ruta={}", request.getMethod(), request.getRequestURI());
        Map<String, Object> error = new HashMap<>();
        error.put("errorCode", "CUERPO_NO_VALIDO");
        error.put("message", "El cuerpo de la petición no es válido");
        error.put("traceId", TraceIdFilter.traceIdActual());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleTipoContenidoNoSoportado(
            HttpMediaTypeNotSupportedException ex,
            HttpServletRequest request) {
        log.warn("Tipo de contenido no soportado: metodo={}, ruta={}", request.getMethod(), request.getRequestURI());
        Map<String, Object> error = new HashMap<>();
        error.put("errorCode", "TIPO_CONTENIDO_NO_SOPORTADO");
        error.put("message", "El tipo de contenido no es soportado, usa application/json");
        error.put("traceId", TraceIdFilter.traceIdActual());
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(error);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleMetodoNoPermitido(
            HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request) {
        log.warn("Método no permitido: metodo={}, ruta={}", request.getMethod(), request.getRequestURI());
        Map<String, Object> error = new HashMap<>();
        error.put("errorCode", "METODO_NO_PERMITIDO");
        error.put("message", "Este método no está permitido en esta ruta");
        error.put("traceId", TraceIdFilter.traceIdActual());
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(error);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, Object>> handleRutaNoEncontrada(
            NoResourceFoundException ex,
            HttpServletRequest request) {
        log.warn("Ruta no encontrada: metodo={}, ruta={}", request.getMethod(), request.getRequestURI());
        Map<String, Object> error = new HashMap<>();
        error.put("errorCode", "RUTA_NO_ENCONTRADA");
        error.put("message", "La ruta no existe");
        error.put("traceId", TraceIdFilter.traceIdActual());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
}