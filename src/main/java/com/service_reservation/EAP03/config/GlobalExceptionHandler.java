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
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String ERROR_CODE = "errorCode";
    private static final String MESSAGE = "message";
    private static final String TRACE_ID = "traceId";

    private final ObjectProvider<RegistrarAccesoDenegadoUseCase> registrarAccesoDenegadoUseCaseProvider;

    public GlobalExceptionHandler(ObjectProvider<RegistrarAccesoDenegadoUseCase> registrarAccesoDenegadoUseCaseProvider) {
        this.registrarAccesoDenegadoUseCaseProvider = registrarAccesoDenegadoUseCaseProvider;
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<Map<String, Object>> handleCredencialesInvalidas(CredencialesInvalidasException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put(ERROR_CODE,"CREDENCIALES_INVALIDAS");
        error.put(MESSAGE,ex.getMessage());
        error.put(TRACE_ID,TraceIdFilter.traceIdActual());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(CuentaBloqueadaException.class)
    public ResponseEntity<Map<String, Object>> handleCuentaBloqueada(CuentaBloqueadaException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put(ERROR_CODE,"CUENTA_BLOQUEADA");
        error.put(MESSAGE,ex.getMessage());
        error.put(TRACE_ID,TraceIdFilter.traceIdActual());
        return ResponseEntity.status(HttpStatus.LOCKED).body(error);
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<Map<String, Object>> handleReglaNegocio(ReglaNegocioException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put(ERROR_CODE,"REGLA_NEGOCIO");
        error.put(MESSAGE,ex.getMessage());
        error.put(TRACE_ID,TraceIdFilter.traceIdActual());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(HorarioSolapadoException.class)
    public ResponseEntity<Map<String, Object>> handleHorarioSolapado(
            HorarioSolapadoException ex) {

        Map<String, Object> error = new HashMap<>();
        error.put(ERROR_CODE,"REGLA_NEGOCIO");
        error.put(MESSAGE,ex.getMessage());
        error.put(TRACE_ID,TraceIdFilter.traceIdActual());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(error);
    }

    @ExceptionHandler(AgendaNoEncontradaException.class)
    public ResponseEntity<Map<String, Object>> handleAgendaNoEncontrada(AgendaNoEncontradaException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put(ERROR_CODE,"AGENDA_NO_ENCONTRADA");
        error.put(MESSAGE,ex.getMessage());
        error.put(TRACE_ID,TraceIdFilter.traceIdActual());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(AgendaDuplicadaException.class)
    public ResponseEntity<Map<String, Object>> handleAgendaDuplicada(AgendaDuplicadaException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put(ERROR_CODE,"AGENDA_DUPLICADA");
        error.put(MESSAGE,ex.getMessage());
        error.put(TRACE_ID,TraceIdFilter.traceIdActual());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(ServicioNoPerteneceAlProveedorException.class)
    public ResponseEntity<Map<String, Object>> handleServicioNoPerteneceAlProveedor(ServicioNoPerteneceAlProveedorException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put(ERROR_CODE,"SERVICIO_NO_PERTENECE_AL_PROVEEDOR");
        error.put(MESSAGE,ex.getMessage());
        error.put(TRACE_ID,TraceIdFilter.traceIdActual());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(DatosAgendaInvalidosException.class)
    public ResponseEntity<Map<String, Object>> handleDatosAgendaInvalidos(DatosAgendaInvalidosException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put(ERROR_CODE,"DATOS_AGENDA_INVALIDOS");
        error.put(MESSAGE,ex.getMessage());
        error.put(TRACE_ID,TraceIdFilter.traceIdActual());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(PersistenciaAgendasHorariosNoDisponibleException.class)
    public ResponseEntity<Map<String, Object>> handlePersistenciaNoDisponible(
            PersistenciaAgendasHorariosNoDisponibleException ex) {

        Map<String, Object> error = new HashMap<>();
        error.put(ERROR_CODE,"PERSISTENCIA_NO_DISPONIBLE");
        error.put(MESSAGE,ex.getMessage());
        error.put(TRACE_ID,TraceIdFilter.traceIdActual());

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
                        LocalDateTime.now(ZoneId.systemDefault()),
                        idUsuario,
                        request.getMethod(),
                        request.getRequestURI()
                ));
            }
        }

        Map<String, Object> error = new HashMap<>();
        error.put(ERROR_CODE,"ACCESO_NO_AUTORIZADO");
        error.put(MESSAGE,"No tiene privilegios para realizar esta operación");
        error.put(TRACE_ID,TraceIdFilter.traceIdActual());

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(error);
    }

    private static final Set<String> CODIGOS_CAMPO_OBLIGATORIO = Set.of("NotBlank", "NotNull", "NotEmpty");

    private static final Comparator<FieldError> PRIORIDAD_ERROR_CAMPO = Comparator
            .comparing((FieldError e) -> !CODIGOS_CAMPO_OBLIGATORIO.contains(e.getCode()))
            .thenComparing(e -> String.valueOf(e.getCode()));

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidations(MethodArgumentNotValidException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put(ERROR_CODE,"VALIDACION_ENTRADA");
        error.put(MESSAGE,"Error en los datos enviados");
        
        Map<String, String> details = new HashMap<>();
        // Un campo puede romper varias reglas a la vez (p. ej. rol "" rompe @NotBlank y @Pattern) y
        // Bean Validation no garantiza el orden. Se elige siempre el mismo error para que el mensaje
        // no cambie entre peticiones: primero el de campo obligatorio, si no el de código menor.
        Map<String, FieldError> errorPorCampo = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(e ->
                errorPorCampo.merge(e.getField(), e, (actual, nuevo) ->
                        PRIORIDAD_ERROR_CAMPO.compare(nuevo, actual) < 0 ? nuevo : actual));
        errorPorCampo.forEach((campo, e) -> details.put(campo, e.getDefaultMessage()));
        if (!ex.getBindingResult().getGlobalErrors().isEmpty()) {
            ex.getBindingResult().getGlobalErrors().forEach(e -> details.put(e.getObjectName(), e.getDefaultMessage()));
        }
        
        error.put("details", details);
        error.put(TRACE_ID,TraceIdFilter.traceIdActual());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(com.service_reservation.EAP03.modulos.availability.domain.exception.DisponibilidadInvalidaException.class)
    public ResponseEntity<Map<String, Object>> handleDisponibilidadInvalida(
            com.service_reservation.EAP03.modulos.availability.domain.exception.DisponibilidadInvalidaException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put(ERROR_CODE,"PARAMETROS_INVALIDOS");
        error.put(MESSAGE,ex.getMessage());
        error.put(TRACE_ID,TraceIdFilter.traceIdActual());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(com.service_reservation.EAP03.modulos.availability.domain.exception.RangoFechasExcedidoException.class)
    public ResponseEntity<Map<String, Object>> handleRangoFechasExcedido(
            com.service_reservation.EAP03.modulos.availability.domain.exception.RangoFechasExcedidoException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put(ERROR_CODE,"RANGO_FECHAS_EXCEDIDO");
        error.put(MESSAGE,ex.getMessage());
        error.put(TRACE_ID,TraceIdFilter.traceIdActual());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(com.service_reservation.EAP03.modulos.availability.domain.exception.ServicioParaDisponibilidadNoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> handleServicioNoEncontrado(
            com.service_reservation.EAP03.modulos.availability.domain.exception.ServicioParaDisponibilidadNoEncontradoException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put(ERROR_CODE,"SERVICIO_NO_ENCONTRADO");
        error.put(MESSAGE,ex.getMessage());
        error.put(TRACE_ID,TraceIdFilter.traceIdActual());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(
            org.springframework.web.method.annotation.MethodArgumentTypeMismatchException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put(ERROR_CODE,"FORMATO_PARAMETRO_INVALIDO");
        error.put(MESSAGE,"El parámetro '" + ex.getName() + "' tiene un formato o valor inválido");
        error.put(TRACE_ID,TraceIdFilter.traceIdActual());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(DatosAdminInvalidosException.class)
    public ResponseEntity<Map<String, Object>> handleDatosAdminInvalidos(DatosAdminInvalidosException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put(ERROR_CODE,"DATOS_ADMIN_INVALIDOS");
        error.put(MESSAGE,ex.getMessage());
        error.put(TRACE_ID,TraceIdFilter.traceIdActual());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(OperacionAdminInvalidaException.class)
    public ResponseEntity<Map<String, Object>> handleOperacionAdminInvalida(OperacionAdminInvalidaException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put(ERROR_CODE,"OPERACION_ADMIN_INVALIDA");
        error.put(MESSAGE,ex.getMessage());
        error.put(TRACE_ID,TraceIdFilter.traceIdActual());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(RecursoAdminNoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> handleRecursoAdminNoEncontrado(RecursoAdminNoEncontradoException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put(ERROR_CODE,"RECURSO_ADMIN_NO_ENCONTRADO");
        error.put(MESSAGE,ex.getMessage());
        error.put(TRACE_ID,TraceIdFilter.traceIdActual());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(AdminDominioException.class)
    public ResponseEntity<Map<String, Object>> handleAdminDominio(AdminDominioException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put(ERROR_CODE,"ERROR_DOMINIO_ADMIN");
        error.put(MESSAGE,ex.getMessage());
        error.put(TRACE_ID,TraceIdFilter.traceIdActual());
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
        error.put(ERROR_CODE,"CUERPO_NO_VALIDO");
        error.put(MESSAGE,"El cuerpo de la petición no es válido");
        error.put(TRACE_ID,TraceIdFilter.traceIdActual());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleTipoContenidoNoSoportado(
            HttpMediaTypeNotSupportedException ex,
            HttpServletRequest request) {
        log.warn("Tipo de contenido no soportado: metodo={}, ruta={}", request.getMethod(), request.getRequestURI());
        Map<String, Object> error = new HashMap<>();
        error.put(ERROR_CODE,"TIPO_CONTENIDO_NO_SOPORTADO");
        error.put(MESSAGE,"El tipo de contenido no es soportado, usa application/json");
        error.put(TRACE_ID,TraceIdFilter.traceIdActual());
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(error);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleMetodoNoPermitido(
            HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request) {
        log.warn("Método no permitido: metodo={}, ruta={}", request.getMethod(), request.getRequestURI());
        Map<String, Object> error = new HashMap<>();
        error.put(ERROR_CODE,"METODO_NO_PERMITIDO");
        error.put(MESSAGE,"Este método no está permitido en esta ruta");
        error.put(TRACE_ID,TraceIdFilter.traceIdActual());
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(error);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, Object>> handleRutaNoEncontrada(
            NoResourceFoundException ex,
            HttpServletRequest request) {
        log.warn("Ruta no encontrada: metodo={}, ruta={}", request.getMethod(), request.getRequestURI());
        Map<String, Object> error = new HashMap<>();
        error.put(ERROR_CODE,"RUTA_NO_ENCONTRADA");
        error.put(MESSAGE,"La ruta no existe");
        error.put(TRACE_ID,TraceIdFilter.traceIdActual());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
}