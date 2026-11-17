package com.service_reservation.EAP03.modulos.reservas.infrastructure.inbound;

import com.service_reservation.EAP03.config.TraceIdFilter;
import com.service_reservation.EAP03.modulos.reservas.domain.exception.CancelacionFueraDeTiempoException;
import com.service_reservation.EAP03.modulos.reservas.domain.exception.ConsultaHistorialInvalidaException;
import com.service_reservation.EAP03.modulos.reservas.domain.exception.DatosReservaInvalidosException;
import com.service_reservation.EAP03.modulos.reservas.domain.exception.FechaEnElPasadoException;
import com.service_reservation.EAP03.modulos.reservas.domain.exception.HistorialAjenoException;
import com.service_reservation.EAP03.modulos.reservas.domain.exception.HoraFueraDeTurnoException;
import com.service_reservation.EAP03.modulos.reservas.domain.exception.HorarioNoDisponibleException;
import com.service_reservation.EAP03.modulos.reservas.domain.exception.RecursoNoDisponibleException;
import com.service_reservation.EAP03.modulos.reservas.domain.exception.RecursoOpcionalInvalidoException;
import com.service_reservation.EAP03.modulos.reservas.domain.exception.ReservaAjenaException;
import com.service_reservation.EAP03.modulos.reservas.domain.exception.ReservaNoCancelableException;
import com.service_reservation.EAP03.modulos.reservas.domain.exception.ReservaNoEncontradaException;
import com.service_reservation.EAP03.modulos.reservas.domain.exception.ServicioNoReservableException;
import com.service_reservation.EAP03.modulos.reservas.domain.exception.UsuarioNoEsClienteException;
import com.service_reservation.EAP03.modulos.identidad.infrastructure.inbound.security.UsuarioAutenticadoPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice(assignableTypes = ReservaController.class)
public class ReservaExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ReservaExceptionHandler.class);

    @ExceptionHandler(FechaEnElPasadoException.class)
    public ResponseEntity<Map<String, Object>> manejarFechaEnElPasado(FechaEnElPasadoException ex) {
        return construir(HttpStatus.BAD_REQUEST, "FECHA_EN_EL_PASADO", ex.getMessage());
    }

    @ExceptionHandler(HoraFueraDeTurnoException.class)
    public ResponseEntity<Map<String, Object>> manejarHoraFueraDeTurno(HoraFueraDeTurnoException ex) {
        return construir(HttpStatus.BAD_REQUEST, "HORA_FUERA_DE_TURNO", ex.getMessage());
    }

    @ExceptionHandler(HorarioNoDisponibleException.class)
    public ResponseEntity<Map<String, Object>> manejarHorarioNoDisponible(HorarioNoDisponibleException ex) {
        return construir(HttpStatus.CONFLICT, "HORARIO_NO_DISPONIBLE", ex.getMessage());
    }

    @ExceptionHandler(RecursoNoDisponibleException.class)
    public ResponseEntity<Map<String, Object>> manejarRecursoNoDisponible(RecursoNoDisponibleException ex) {
        return construir(HttpStatus.CONFLICT, "RECURSO_NO_DISPONIBLE", ex.getMessage());
    }

    @ExceptionHandler(RecursoOpcionalInvalidoException.class)
    public ResponseEntity<Map<String, Object>> manejarRecursoOpcionalInvalido(RecursoOpcionalInvalidoException ex) {
        return construir(HttpStatus.BAD_REQUEST, "RECURSO_OPCIONAL_NO_VALIDO", ex.getMessage());
    }

    @ExceptionHandler(ServicioNoReservableException.class)
    public ResponseEntity<Map<String, Object>> manejarServicioNoReservable(ServicioNoReservableException ex) {
        return construir(HttpStatus.NOT_FOUND, "SERVICIO_NO_ENCONTRADO", ex.getMessage());
    }

    @ExceptionHandler(UsuarioNoEsClienteException.class)
    public ResponseEntity<Map<String, Object>> manejarNoEsCliente(UsuarioNoEsClienteException ex) {
        return construir(HttpStatus.FORBIDDEN, "NO_ES_CLIENTE", ex.getMessage());
    }

    @ExceptionHandler(ReservaNoEncontradaException.class)
    public ResponseEntity<Map<String, Object>> manejarReservaNoEncontrada(ReservaNoEncontradaException ex) {
        return construir(HttpStatus.NOT_FOUND, "RESERVA_NO_ENCONTRADA", ex.getMessage());
    }

    @ExceptionHandler(ReservaAjenaException.class)
    public ResponseEntity<Map<String, Object>> manejarReservaAjena(ReservaAjenaException ex) {
        return construir(HttpStatus.FORBIDDEN, "RESERVA_AJENA", ex.getMessage());
    }

    @ExceptionHandler(ReservaNoCancelableException.class)
    public ResponseEntity<Map<String, Object>> manejarNoCancelable(ReservaNoCancelableException ex) {
        return construir(HttpStatus.CONFLICT, "RESERVA_NO_CANCELABLE", ex.getMessage());
    }

    @ExceptionHandler(CancelacionFueraDeTiempoException.class)
    public ResponseEntity<Map<String, Object>> manejarFueraDeTiempo(CancelacionFueraDeTiempoException ex) {
        return construir(HttpStatus.CONFLICT, "CANCELACION_FUERA_DE_TIEMPO", ex.getMessage());
    }

    @ExceptionHandler(DatosReservaInvalidosException.class)
    public ResponseEntity<Map<String, Object>> manejarDatosInvalidos(DatosReservaInvalidosException ex) {
        return construir(HttpStatus.BAD_REQUEST, "DATOS_INVALIDOS", ex.getMessage());
    }

    // Error que da la base de datos cuando dos reservas del mismo horario se guardan al mismo
    // tiempo y deja pasar solo una (ver CrearReservaService). Según la base, Spring lo
    // reporta con nombres distintos; esta clase los agrupa a todos.
    @ExceptionHandler(PessimisticLockingFailureException.class)
    public ResponseEntity<Map<String, Object>> manejarConcurrencia(PessimisticLockingFailureException ex) {
        return construir(HttpStatus.CONFLICT, "HORARIO_NO_DISPONIBLE",
                "Otra persona acaba de tomar este horario o uno de los recursos que necesita. Intenta de nuevo.");
    }

    // Intento de ver el historial de otro cliente: se responde 403 y queda en el log del backend.
    @ExceptionHandler(HistorialAjenoException.class)
    public ResponseEntity<Map<String, Object>> manejarHistorialAjeno(HistorialAjenoException ex, HttpServletRequest request) {
        auditar("HISTORIAL_AJENO_DENEGADO", "intento de ver historial de otro cliente", request);
        return construir(HttpStatus.FORBIDDEN, "HISTORIAL_AJENO", ex.getMessage());
    }

    // Parámetros raros o con aspecto de comando: 400 con mensaje fijo (no repite lo enviado) y queda en el log.
    @ExceptionHandler(ConsultaHistorialInvalidaException.class)
    public ResponseEntity<Map<String, Object>> manejarConsultaHistorialInvalida(ConsultaHistorialInvalidaException ex, HttpServletRequest request) {
        auditar("HISTORIAL_ENTRADA_RECHAZADA", ex.getMotivo().name(), request);
        return construir(HttpStatus.BAD_REQUEST, "CONSULTA_INVALIDA", ex.getMessage());
    }

    // Auditoría en el log: solo evento, motivo fijo, id del usuario, método, ruta y traceId.
    // Nunca correo, IP ni los valores que envió el usuario.
    private static void auditar(String evento, String motivo, HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long idUsuario = auth != null && auth.getPrincipal() instanceof UsuarioAutenticadoPrincipal principal
                ? principal.getIdUsuario()
                : null;
        log.warn("AUDITORIA evento={} motivo={} idUsuario={} metodo={} ruta={} traceId={}",
                evento, motivo, idUsuario, request.getMethod(), request.getRequestURI(),
                TraceIdFilter.traceIdActual());
    }

    private ResponseEntity<Map<String, Object>> construir(HttpStatus status, String errorCode, String mensaje) {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("errorCode", errorCode);
        cuerpo.put("message", mensaje);
        cuerpo.put("traceId", TraceIdFilter.traceIdActual());
        return ResponseEntity.status(status).body(cuerpo);
    }
}
