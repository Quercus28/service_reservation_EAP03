package com.service_reservation.EAP03.modulos.reportes.infrastructure.inbound.web;

import com.service_reservation.EAP03.config.RespuestaErrorJson;
import com.service_reservation.EAP03.config.TraceIdFilter;
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
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Manejador de excepciones específico para el módulo de reportes.
 * Traduce las excepciones de dominio a respuestas HTTP coherentes con el estándar del proyecto
 * ({errorCode, message, traceId}).
 */
@RestControllerAdvice(basePackages = "com.service_reservation.EAP03.modulos.reportes")
public class ReportesExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ReportesExceptionHandler.class);

    private final RegistrarAuditoriaUseCase registrarAuditoriaUseCase;

    public ReportesExceptionHandler(RegistrarAuditoriaUseCase registrarAuditoriaUseCase) {
        this.registrarAuditoriaUseCase = registrarAuditoriaUseCase;
    }

    @ExceptionHandler(PeriodoReporteInvalidoException.class)
    public ResponseEntity<Map<String, Object>> manejarPeriodoReporteInvalido(PeriodoReporteInvalidoException ex) {
        return construir(HttpStatus.BAD_REQUEST, "PERIODO_INVALIDO", ex.getMessage());
    }

    // Mismo código que usa el proyecto para un dato obligatorio que falta (VALIDACION_ENTRADA).
    @ExceptionHandler(ParametroReporteFaltanteException.class)
    public ResponseEntity<Map<String, Object>> manejarParametroFaltante(ParametroReporteFaltanteException ex) {
        return construir(HttpStatus.BAD_REQUEST, "VALIDACION_ENTRADA", ex.getMessage());
    }

    // Mismo código y mensaje que da el manejador global cuando Spring no puede convertir un parámetro.
    @ExceptionHandler(FormatoParametroReporteInvalidoException.class)
    public ResponseEntity<Map<String, Object>> manejarFormatoInvalido(FormatoParametroReporteInvalidoException ex) {
        return construir(HttpStatus.BAD_REQUEST, "FORMATO_PARAMETRO_INVALIDO", ex.getMessage());
    }

    // Parámetro desconocido o repetido: 400 con mensaje fijo (no repite lo enviado) y queda en el log.
    @ExceptionHandler(ConsultaReporteInvalidaException.class)
    public ResponseEntity<Map<String, Object>> manejarConsultaInvalida(ConsultaReporteInvalidaException ex,
                                                                       HttpServletRequest request) {
        if (log.isWarnEnabled()) {
            log.warn("AUDITORIA evento=REPORTE_ENTRADA_RECHAZADA motivo={} idUsuario={} metodo={} ruta={} traceId={}",
                    ex.getMotivo().name(), idUsuarioActual(), request.getMethod(), request.getRequestURI(),
                    TraceIdFilter.traceIdActual());
        }
        return construir(HttpStatus.BAD_REQUEST, "CONSULTA_INVALIDA", ex.getMessage());
    }

    // La petición nombra a un proveedor: es un intento de ver el reporte de otro. Se responde 403
    // igual que cualquier acceso denegado y se guarda en la auditoría de admin.
    @ExceptionHandler(ReporteAjenoException.class)
    public ResponseEntity<Map<String, Object>> manejarReporteAjeno(ReporteAjenoException ex, HttpServletRequest request) {
        Long idUsuario = idUsuarioActual();
        if (log.isWarnEnabled()) {
            log.warn("AUDITORIA evento=REPORTE_AJENO_DENEGADO motivo=reporte de otro proveedor idUsuario={} metodo={} ruta={} traceId={}",
                    idUsuario, request.getMethod(), request.getRequestURI(), TraceIdFilter.traceIdActual());
        }
        guardarEnAuditoria(idUsuario, request);
        return construir(HttpStatus.FORBIDDEN, RespuestaErrorJson.ACCESO_NO_AUTORIZADO,
                RespuestaErrorJson.MENSAJE_ACCESO_NO_AUTORIZADO);
    }

    // RegistrarAuditoriaUseCase guarda en su propia transacción, así que el registro queda aunque
    // la consulta se deshaga. Solo números y textos fijos: ni correo ni nombre ni los valores que
    // envió el usuario (la ruta va sin parámetros). Si la auditoría falla, el proveedor igual
    // recibe su 403 y el fallo queda en el log.
    private void guardarEnAuditoria(Long idUsuario, HttpServletRequest request) {
        try {
            registrarAuditoriaUseCase.ejecutar(new EventoAuditoria(
                    idUsuario,
                    null,
                    AccionAdmin.REPORTE_AJENO_DENEGADO,
                    ResultadoAuditoria.DENEGADO,
                    request.getMethod() + " " + request.getRequestURI(),
                    "motivo=reporte de otro proveedor"
            ));
        } catch (RuntimeException e) {
            log.error("No se pudo guardar en la auditoría el intento de ver un reporte ajeno: traceId={}",
                    TraceIdFilter.traceIdActual(), e);
        }
    }

    private static Long idUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getPrincipal() instanceof UsuarioAutenticadoPrincipal principal
                ? principal.getIdUsuario()
                : null;
    }

    private static ResponseEntity<Map<String, Object>> construir(HttpStatus status, String errorCode, String mensaje) {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("errorCode", errorCode);
        cuerpo.put("message", mensaje);
        cuerpo.put("traceId", TraceIdFilter.traceIdActual());
        return ResponseEntity.status(status).body(cuerpo);
    }
}
