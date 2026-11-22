package com.service_reservation.EAP03.modulos.reservas.infrastructure.inbound;

import com.service_reservation.EAP03.modulos.reservas.domain.exception.CambioEstadoNoPermitidoException;
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
import com.service_reservation.EAP03.modulos.admin.domain.model.AccionAdmin;
import com.service_reservation.EAP03.modulos.admin.domain.model.EventoAuditoria;
import com.service_reservation.EAP03.modulos.admin.domain.model.ResultadoAuditoria;
import com.service_reservation.EAP03.modulos.admin.domain.ports.in.RegistrarAuditoriaUseCase;
import com.service_reservation.EAP03.modulos.reservas.domain.exception.UsuarioNoEsProveedorException;
import org.junit.jupiter.api.DisplayName;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.Test;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class ReservaExceptionHandlerTest {

    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 11, 2, 9, 0);

    private final RegistrarAuditoriaUseCase registrarAuditoriaUseCase = mock(RegistrarAuditoriaUseCase.class);
    private final ReservaExceptionHandler handler = new ReservaExceptionHandler(registrarAuditoriaUseCase);

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
    @DisplayName("Quien no es proveedor con permiso activo responde 403 NO_ES_PROVEEDOR")
    void notProviderReturns403() {
        UsuarioNoEsProveedorException noProveedor = new UsuarioNoEsProveedorException();

        assertError(handler.manejarNoEsProveedor(noProveedor), 403, "NO_ES_PROVEEDOR", noProveedor.getMessage());
    }

    @Test
    @DisplayName("Un cambio de estado no permitido responde 409 con su propio código CAMBIO_ESTADO_NO_PERMITIDO")
    void invalidStateChangeReturns409() {
        CambioEstadoNoPermitidoException ex = new CambioEstadoNoPermitidoException(8,
                com.service_reservation.EAP03.modulos.reservas.domain.model.EstadoReserva.RESERVADO,
                com.service_reservation.EAP03.modulos.reservas.domain.model.EstadoReserva.COMPLETADO);

        assertError(handler.manejarCambioEstadoNoPermitido(ex), 409, "CAMBIO_ESTADO_NO_PERMITIDO", ex.getMessage());
    }

    @Test
    @DisplayName("Un choque de concurrencia en la base de datos responde 409 con un mensaje para reintentar")
    void concurrencyConflictReturns409() {
        assertError(handler.manejarConcurrencia(new PessimisticLockingFailureException("serialización")), 409, "HORARIO_NO_DISPONIBLE",
                "Otra persona acaba de tomar este horario o uno de los recursos que necesita. Intenta de nuevo.");
    }

    @Test
    @DisplayName("El historial ajeno responde 403 con un mensaje fijo")
    void otherClientsHistoryReturns403() {
        org.springframework.mock.web.MockHttpServletRequest request = new org.springframework.mock.web.MockHttpServletRequest("GET", "/api/v1/reservas/mias");
        com.service_reservation.EAP03.modulos.reservas.domain.exception.HistorialAjenoException ex =
                new com.service_reservation.EAP03.modulos.reservas.domain.exception.HistorialAjenoException();

        assertError(handler.manejarHistorialAjeno(ex, request), 403, "HISTORIAL_AJENO", ex.getMessage());
    }

    @Test
    @DisplayName("Una consulta de historial inválida responde 400 sin repetir lo enviado, con o sin usuario autenticado")
    void invalidHistoryQueryReturns400() {
        org.springframework.mock.web.MockHttpServletRequest request = new org.springframework.mock.web.MockHttpServletRequest("GET", "/api/v1/reservas/mias");
        com.service_reservation.EAP03.modulos.reservas.domain.exception.ConsultaHistorialInvalidaException ex =
                new com.service_reservation.EAP03.modulos.reservas.domain.exception.ConsultaHistorialInvalidaException(
                        com.service_reservation.EAP03.modulos.reservas.domain.exception.ConsultaHistorialInvalidaException.Motivo.PATRON_SOSPECHOSO);

        // Sin autenticación
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
        assertError(handler.manejarConsultaHistorialInvalida(ex, request), 400, "CONSULTA_INVALIDA", "La solicitud de consulta no es válida.");

        // Con un usuario del sistema autenticado
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        new com.service_reservation.EAP03.modulos.identidad.infrastructure.inbound.security.UsuarioAutenticadoPrincipal(20L, "c@test.com", List.of("CLIENTE")), null, List.of()));
        try {
            assertError(handler.manejarConsultaHistorialInvalida(ex, request), 400, "CONSULTA_INVALIDA", "La solicitud de consulta no es válida.");
            assertError(handler.manejarHistorialAjeno(new com.service_reservation.EAP03.modulos.reservas.domain.exception.HistorialAjenoException(), request),
                    403, "HISTORIAL_AJENO", "No tiene permiso para consultar el historial de otro cliente.");
        } finally {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }
    }

    private static void autenticar(Long idUsuario, String email) {
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        new com.service_reservation.EAP03.modulos.identidad.infrastructure.inbound.security.UsuarioAutenticadoPrincipal(
                                idUsuario, email, List.of("CLIENTE")), null, List.of()));
    }

    @Test
    @DisplayName("El historial ajeno queda en la auditoría con la acción HISTORIAL_AJENO_DENEGADO y sin datos personales")
    void otherClientsHistoryIsAudited() {
        org.springframework.mock.web.MockHttpServletRequest request =
                new org.springframework.mock.web.MockHttpServletRequest("GET", "/api/v1/reservas/mias");
        request.setQueryString("idCliente=99");
        autenticar(20L, "cliente.privado@test.com");
        try {
            handler.manejarHistorialAjeno(new com.service_reservation.EAP03.modulos.reservas.domain.exception.HistorialAjenoException(), request);
        } finally {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }

        ArgumentCaptor<EventoAuditoria> captor = ArgumentCaptor.forClass(EventoAuditoria.class);
        verify(registrarAuditoriaUseCase).ejecutar(captor.capture());
        EventoAuditoria evento = captor.getValue();
        assertEquals(20L, evento.idActor());
        assertNull(evento.idUsuarioAfectado());
        assertEquals(AccionAdmin.HISTORIAL_AJENO_DENEGADO, evento.accion());
        assertEquals(ResultadoAuditoria.DENEGADO, evento.resultado());
        assertEquals("GET /api/v1/reservas/mias", evento.endpoint());
        assertEquals("motivo=historial de otro cliente", evento.detalle());
        String todo = evento.endpoint() + evento.detalle();
        assertFalse(todo.contains("@"));
        assertFalse(todo.contains("cliente.privado"));
        assertFalse(todo.contains("idCliente=99"));
    }

    @Test
    @DisplayName("Sin usuario identificado, el historial ajeno se audita con el usuario vacío")
    void otherClientsHistoryAuditedWithoutUser() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();

        handler.manejarHistorialAjeno(new com.service_reservation.EAP03.modulos.reservas.domain.exception.HistorialAjenoException(),
                new org.springframework.mock.web.MockHttpServletRequest("GET", "/api/v1/reservas/mias"));

        ArgumentCaptor<EventoAuditoria> captor = ArgumentCaptor.forClass(EventoAuditoria.class);
        verify(registrarAuditoriaUseCase).ejecutar(captor.capture());
        assertNull(captor.getValue().idActor());
    }

    @Test
    @DisplayName("Si la auditoría falla, el cliente igual recibe su 403")
    void auditFailureStillReturns403() {
        doThrow(new IllegalStateException("base caída")).when(registrarAuditoriaUseCase).ejecutar(any());
        com.service_reservation.EAP03.modulos.reservas.domain.exception.HistorialAjenoException ex =
                new com.service_reservation.EAP03.modulos.reservas.domain.exception.HistorialAjenoException();
        org.springframework.mock.web.MockHttpServletRequest request =
                new org.springframework.mock.web.MockHttpServletRequest("GET", "/api/v1/reservas/mias");

        assertError(handler.manejarHistorialAjeno(ex, request), 403, "HISTORIAL_AJENO", ex.getMessage());
    }

    @Test
    @DisplayName("Una consulta de historial inválida solo va al log, no a la auditoría de admin")
    void invalidHistoryQueryNotAudited() {
        handler.manejarConsultaHistorialInvalida(
                new com.service_reservation.EAP03.modulos.reservas.domain.exception.ConsultaHistorialInvalidaException(
                        com.service_reservation.EAP03.modulos.reservas.domain.exception.ConsultaHistorialInvalidaException.Motivo.VALOR_INVALIDO),
                new org.springframework.mock.web.MockHttpServletRequest("GET", "/api/v1/reservas/mias"));

        verifyNoInteractions(registrarAuditoriaUseCase);
    }

    @Test
    @DisplayName("Si el principal no es un usuario de la aplicación, el historial ajeno se audita sin usuario")
    void otherClientsHistoryAuditedWithUnknownPrincipal() {
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("anonimo", null, List.of()));
        try {
            handler.manejarHistorialAjeno(new com.service_reservation.EAP03.modulos.reservas.domain.exception.HistorialAjenoException(),
                    new org.springframework.mock.web.MockHttpServletRequest("GET", "/api/v1/reservas/mias"));
        } finally {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }

        ArgumentCaptor<EventoAuditoria> captor = ArgumentCaptor.forClass(EventoAuditoria.class);
        verify(registrarAuditoriaUseCase).ejecutar(captor.capture());
        assertNull(captor.getValue().idActor());
    }

    @Test
    @DisplayName("Con el nivel WARN apagado, el historial ajeno igual responde 403 y queda en la auditoría")
    void otherClientsHistoryWithWarnDisabled() {
        ch.qos.logback.classic.Logger logger =
                (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(ReservaExceptionHandler.class);
        ch.qos.logback.classic.Level nivelOriginal = logger.getLevel();
        logger.setLevel(ch.qos.logback.classic.Level.ERROR);
        try {
            com.service_reservation.EAP03.modulos.reservas.domain.exception.HistorialAjenoException ex =
                    new com.service_reservation.EAP03.modulos.reservas.domain.exception.HistorialAjenoException();
            assertError(handler.manejarHistorialAjeno(ex,
                    new org.springframework.mock.web.MockHttpServletRequest("GET", "/api/v1/reservas/mias")),
                    403, "HISTORIAL_AJENO", ex.getMessage());
        } finally {
            logger.setLevel(nivelOriginal);
        }

        verify(registrarAuditoriaUseCase).ejecutar(any());
    }
}
