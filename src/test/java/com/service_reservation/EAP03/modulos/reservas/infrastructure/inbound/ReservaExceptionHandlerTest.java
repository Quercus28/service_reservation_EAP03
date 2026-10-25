package com.service_reservation.EAP03.modulos.reservas.infrastructure.inbound;

import com.service_reservation.EAP03.modulos.reservas.domain.exception.CancelacionFueraDeTiempoException;
import com.service_reservation.EAP03.modulos.reservas.domain.exception.DatosReservaInvalidosException;
import com.service_reservation.EAP03.modulos.reservas.domain.exception.FechaEnElPasadoException;
import com.service_reservation.EAP03.modulos.reservas.domain.exception.HoraFueraDeTurnoException;
import com.service_reservation.EAP03.modulos.reservas.domain.exception.HorarioNoDisponibleException;
import com.service_reservation.EAP03.modulos.reservas.domain.exception.RecursoNoDisponibleException;
import com.service_reservation.EAP03.modulos.reservas.domain.exception.RecursoOpcionalInvalidoException;
import com.service_reservation.EAP03.modulos.reservas.domain.exception.ReservaAjenaException;
import com.service_reservation.EAP03.modulos.reservas.domain.exception.ReservaNoCancelableException;
import com.service_reservation.EAP03.modulos.reservas.domain.exception.ReservaNoEncontradaException;
import com.service_reservation.EAP03.modulos.reservas.domain.exception.ServicioNoReservableException;
import com.service_reservation.EAP03.modulos.reservas.domain.exception.UsuarioNoEsClienteException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ReservaExceptionHandlerTest {

    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 11, 2, 9, 0);

    private final ReservaExceptionHandler handler = new ReservaExceptionHandler();

    private static void assertError(ResponseEntity<Map<String, Object>> response, int status, String errorCode, String message) {
        assertEquals(status, response.getStatusCode().value());
        assertEquals(List.of("errorCode", "message", "traceId"), List.copyOf(response.getBody().keySet()));
        assertEquals(errorCode, response.getBody().get("errorCode"));
        assertEquals(message, response.getBody().get("message"));
        assertNotNull(response.getBody().get("traceId"));
    }

    @Test
    @DisplayName("Los datos de reserva inválidos responden 400")
    void invalidDataReturns400() {
        FechaEnElPasadoException pasado = new FechaEnElPasadoException(INICIO);
        HoraFueraDeTurnoException turno = new HoraFueraDeTurnoException(60, LocalTime.of(8, 0));
        RecursoOpcionalInvalidoException opcional = new RecursoOpcionalInvalidoException("No es opcional");
        DatosReservaInvalidosException datos = new DatosReservaInvalidosException("Dato inválido");

        assertError(handler.manejarFechaEnElPasado(pasado), 400, "FECHA_EN_EL_PASADO", pasado.getMessage());
        assertError(handler.manejarHoraFueraDeTurno(turno), 400, "HORA_FUERA_DE_TURNO", turno.getMessage());
        assertError(handler.manejarRecursoOpcionalInvalido(opcional), 400, "RECURSO_OPCIONAL_NO_VALIDO", opcional.getMessage());
        assertError(handler.manejarDatosInvalidos(datos), 400, "DATOS_INVALIDOS", datos.getMessage());
    }

    @Test
    @DisplayName("Los conflictos de horario, recursos y cancelación responden 409")
    void conflictsReturn409() {
        HorarioNoDisponibleException horario = HorarioNoDisponibleException.sinAgenda(10);
        RecursoNoDisponibleException recurso = new RecursoNoDisponibleException("Sin unidades");
        ReservaNoCancelableException noCancelable = ReservaNoCancelableException.yaCancelada(8);
        CancelacionFueraDeTiempoException fueraDeTiempo = CancelacionFueraDeTiempoException.yaEmpezo(8);

        assertError(handler.manejarHorarioNoDisponible(horario), 409, "HORARIO_NO_DISPONIBLE", horario.getMessage());
        assertError(handler.manejarRecursoNoDisponible(recurso), 409, "RECURSO_NO_DISPONIBLE", recurso.getMessage());
        assertError(handler.manejarNoCancelable(noCancelable), 409, "RESERVA_NO_CANCELABLE", noCancelable.getMessage());
        assertError(handler.manejarFueraDeTiempo(fueraDeTiempo), 409, "CANCELACION_FUERA_DE_TIEMPO", fueraDeTiempo.getMessage());
    }

    @Test
    @DisplayName("Un servicio o una reserva inexistentes responden 404")
    void notFoundReturns404() {
        ServicioNoReservableException servicio = new ServicioNoReservableException(10);
        ReservaNoEncontradaException reserva = new ReservaNoEncontradaException(8);

        assertError(handler.manejarServicioNoReservable(servicio), 404, "SERVICIO_NO_ENCONTRADO", servicio.getMessage());
        assertError(handler.manejarReservaNoEncontrada(reserva), 404, "RESERVA_NO_ENCONTRADA", reserva.getMessage());
    }

    @Test
    @DisplayName("Un usuario que no es cliente o una reserva ajena responden 403")
    void forbiddenReturns403() {
        UsuarioNoEsClienteException noCliente = new UsuarioNoEsClienteException();
        ReservaAjenaException ajena = new ReservaAjenaException(8);

        assertError(handler.manejarNoEsCliente(noCliente), 403, "NO_ES_CLIENTE", noCliente.getMessage());
        assertError(handler.manejarReservaAjena(ajena), 403, "RESERVA_AJENA", ajena.getMessage());
    }

    @Test
    @DisplayName("Un choque de concurrencia en la base de datos responde 409 con un mensaje para reintentar")
    void concurrencyConflictReturns409() {
        assertError(handler.manejarConcurrencia(new PessimisticLockingFailureException("serialización")), 409, "HORARIO_NO_DISPONIBLE",
                "Otra persona acaba de tomar este horario o uno de los recursos que necesita. Intenta de nuevo.");
    }
}
