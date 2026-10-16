package com.service_reservation.EAP03.modulos.reservas.infrastructure.inbound;

import com.service_reservation.EAP03.modulos.reservas.domain.exception.CancelacionFueraDeTiempoException;
import com.service_reservation.EAP03.modulos.reservas.domain.exception.DatosReservaInvalidosException;
import com.service_reservation.EAP03.modulos.reservas.domain.exception.FechaEnElPasadoException;
import com.service_reservation.EAP03.modulos.reservas.domain.exception.HorarioNoDisponibleException;
import com.service_reservation.EAP03.modulos.reservas.domain.exception.ReservaAjenaException;
import com.service_reservation.EAP03.modulos.reservas.domain.exception.ReservaNoCancelableException;
import com.service_reservation.EAP03.modulos.reservas.domain.exception.ReservaNoEncontradaException;
import com.service_reservation.EAP03.modulos.reservas.domain.exception.ServicioNoReservableException;
import com.service_reservation.EAP03.modulos.reservas.domain.exception.UsuarioNoEsClienteException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@RestControllerAdvice(assignableTypes = ReservaController.class)
public class ReservaExceptionHandler {

    @ExceptionHandler(FechaEnElPasadoException.class)
    public ResponseEntity<Map<String, Object>> manejarFechaEnElPasado(FechaEnElPasadoException ex) {
        return construir(HttpStatus.BAD_REQUEST, "FECHA_EN_EL_PASADO", ex.getMessage());
    }

    @ExceptionHandler(HorarioNoDisponibleException.class)
    public ResponseEntity<Map<String, Object>> manejarHorarioNoDisponible(HorarioNoDisponibleException ex) {
        return construir(HttpStatus.CONFLICT, "HORARIO_NO_DISPONIBLE", ex.getMessage());
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
                "El horario no está disponible: el horario acaba de ser reservado.");
    }

    private ResponseEntity<Map<String, Object>> construir(HttpStatus status, String errorCode, String mensaje) {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("errorCode", errorCode);
        cuerpo.put("message", mensaje);
        cuerpo.put("traceId", UUID.randomUUID().toString());
        return ResponseEntity.status(status).body(cuerpo);
    }
}
