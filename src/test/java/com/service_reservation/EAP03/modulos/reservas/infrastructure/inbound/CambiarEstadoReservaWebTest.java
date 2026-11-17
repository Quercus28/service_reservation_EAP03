package com.service_reservation.EAP03.modulos.reservas.infrastructure.inbound;

import com.service_reservation.EAP03.config.GlobalExceptionHandler;
import com.service_reservation.EAP03.modulos.admin.domain.model.AccionAdmin;
import com.service_reservation.EAP03.modulos.admin.domain.model.EventoAuditoria;
import com.service_reservation.EAP03.modulos.admin.domain.model.RolOperativo;
import com.service_reservation.EAP03.modulos.admin.domain.ports.in.RegistrarAuditoriaUseCase;
import com.service_reservation.EAP03.modulos.admin.domain.ports.in.VerificarPermisoOperativoUseCase;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.in.ConsultarProveedorUseCase;
import com.service_reservation.EAP03.modulos.identidad.infrastructure.inbound.security.UsuarioAutenticadoPrincipal;
import com.service_reservation.EAP03.modulos.reservas.application.CambiarEstadoReservaService;
import com.service_reservation.EAP03.modulos.reservas.domain.model.EstadoReserva;
import com.service_reservation.EAP03.modulos.reservas.domain.model.IntervaloReservado;
import com.service_reservation.EAP03.modulos.reservas.domain.model.PartesReserva;
import com.service_reservation.EAP03.modulos.reservas.domain.model.Reserva;
import com.service_reservation.EAP03.modulos.reservas.domain.ports.in.CancelarReservaUseCase;
import com.service_reservation.EAP03.modulos.reservas.domain.ports.in.CrearReservaUseCase;
import com.service_reservation.EAP03.modulos.reservas.domain.ports.out.ReservaRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// PATCH /api/v1/reservas/{id}/estado de punta a punta en la capa web: validación del cuerpo,
// formato del id, servicio real (con sus puertos simulados) y los manejadores de error reales.
@ExtendWith(MockitoExtension.class)
class CambiarEstadoReservaWebTest {

    private static final Long ID_PROVEEDOR_USUARIO = 30L;
    private static final Long ID_CLIENTE_USUARIO = 20L;
    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 12, 2, 9, 0);

    @Mock private CrearReservaUseCase crearReservaUseCase;
    @Mock private CancelarReservaUseCase cancelarReservaUseCase;
    @Mock private VerificarPermisoOperativoUseCase verificarPermisoOperativoUseCase;
    @Mock private ConsultarProveedorUseCase consultarProveedorUseCase;
    @Mock private ReservaRepositoryPort reservaRepositoryPort;
    @Mock private RegistrarAuditoriaUseCase registrarAuditoriaUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        CambiarEstadoReservaService servicio = new CambiarEstadoReservaService(
                verificarPermisoOperativoUseCase, consultarProveedorUseCase, reservaRepositoryPort, registrarAuditoriaUseCase);
        ReservaController controller = new ReservaController(crearReservaUseCase, cancelarReservaUseCase, servicio);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ReservaExceptionHandler(), new GlobalExceptionHandler(null))
                .build();
    }

    private static UsernamePasswordAuthenticationToken usuario(Long id, String rol) {
        return new UsernamePasswordAuthenticationToken(
                new UsuarioAutenticadoPrincipal(id, rol.toLowerCase() + "@test.com", List.of(rol)), null, List.of());
    }

    private static Reserva reserva(Integer idProveedor, EstadoReserva estado) {
        return Reserva.reconstruir(8, new PartesReserva(3L, 10, idProveedor),
                new IntervaloReservado(INICIO, INICIO.plusHours(1)), estado, null, INICIO.minusDays(5), null);
    }

    private void proveedorDueno(EstadoReserva estadoActual) {
        when(verificarPermisoOperativoUseCase.tienePermisoOperativo(ID_PROVEEDOR_USUARIO, RolOperativo.PROVEEDOR)).thenReturn(true);
        when(consultarProveedorUseCase.buscarIdProveedorPorUsuario(ID_PROVEEDOR_USUARIO)).thenReturn(Optional.of(7));
        when(reservaRepositoryPort.buscarPorId(8)).thenReturn(Optional.of(reserva(7, estadoActual)));
    }

    private org.springframework.test.web.servlet.ResultActions cambiar(String id, String cuerpo,
                                                                       UsernamePasswordAuthenticationToken quien) throws Exception {
        return mockMvc.perform(patch("/api/v1/reservas/" + id + "/estado")
                .principal(quien)
                .contentType(MediaType.APPLICATION_JSON)
                .content(cuerpo));
    }

    @Test
    @DisplayName("El proveedor dueño confirma una reserva pendiente: 200 con el estado CONFIRMADO")
    void ownerConfirms() throws Exception {
        proveedorDueno(EstadoReserva.RESERVADO);
        when(reservaRepositoryPort.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        cambiar("8", "{\"estado\":\"CONFIRMADO\"}", usuario(ID_PROVEEDOR_USUARIO, "PROVEEDOR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CONFIRMADO"));
    }

    @Test
    @DisplayName("El proveedor dueño finaliza una reserva confirmada: 200 con el estado COMPLETADO")
    void ownerCompletes() throws Exception {
        proveedorDueno(EstadoReserva.CONFIRMADO);
        when(reservaRepositoryPort.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        cambiar("8", "{\"estado\":\"COMPLETADO\"}", usuario(ID_PROVEEDOR_USUARIO, "PROVEEDOR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("COMPLETADO"));
    }

    @Test
    @DisplayName("Un cambio no permitido responde 409 CAMBIO_ESTADO_NO_PERMITIDO con el mensaje de acción no válida")
    void invalidChangeReturns409() throws Exception {
        proveedorDueno(EstadoReserva.RESERVADO);

        cambiar("8", "{\"estado\":\"COMPLETADO\"}", usuario(ID_PROVEEDOR_USUARIO, "PROVEEDOR"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("CAMBIO_ESTADO_NO_PERMITIDO"))
                .andExpect(jsonPath("$.message").value(
                        "La acción no es válida para el estado actual de la reserva 8: no se puede pasar de RESERVADO a COMPLETADO."))
                .andExpect(jsonPath("$.traceId").exists());
        verify(reservaRepositoryPort, never()).guardar(any());
    }

    @Test
    @DisplayName("Un cliente recibe 403 NO_ES_PROVEEDOR y el intento queda en la auditoría")
    void clientGets403AndIsAudited() throws Exception {
        when(verificarPermisoOperativoUseCase.tienePermisoOperativo(ID_CLIENTE_USUARIO, RolOperativo.PROVEEDOR)).thenReturn(false);

        cambiar("8", "{\"estado\":\"CONFIRMADO\"}", usuario(ID_CLIENTE_USUARIO, "CLIENTE"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("NO_ES_PROVEEDOR"));

        ArgumentCaptor<EventoAuditoria> evento = ArgumentCaptor.forClass(EventoAuditoria.class);
        verify(registrarAuditoriaUseCase).ejecutar(evento.capture());
        assertEquals(AccionAdmin.CAMBIO_ESTADO_RESERVA_DENEGADO, evento.getValue().accion());
        assertEquals(ID_CLIENTE_USUARIO, evento.getValue().idActor());
        assertEquals("PATCH /api/v1/reservas/8/estado", evento.getValue().endpoint());
        verifyNoInteractions(reservaRepositoryPort);
    }

    @Test
    @DisplayName("Un proveedor que no es el dueño recibe 403 RESERVA_AJENA y el intento queda en la auditoría")
    void otherProviderGets403AndIsAudited() throws Exception {
        when(verificarPermisoOperativoUseCase.tienePermisoOperativo(ID_PROVEEDOR_USUARIO, RolOperativo.PROVEEDOR)).thenReturn(true);
        when(consultarProveedorUseCase.buscarIdProveedorPorUsuario(ID_PROVEEDOR_USUARIO)).thenReturn(Optional.of(99));
        when(reservaRepositoryPort.buscarPorId(8)).thenReturn(Optional.of(reserva(7, EstadoReserva.RESERVADO)));

        cambiar("8", "{\"estado\":\"CONFIRMADO\"}", usuario(ID_PROVEEDOR_USUARIO, "PROVEEDOR"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("RESERVA_AJENA"));

        ArgumentCaptor<EventoAuditoria> evento = ArgumentCaptor.forClass(EventoAuditoria.class);
        verify(registrarAuditoriaUseCase).ejecutar(evento.capture());
        assertEquals(AccionAdmin.CAMBIO_ESTADO_RESERVA_DENEGADO, evento.getValue().accion());
        verify(reservaRepositoryPort, never()).guardar(any());
    }

    @ParameterizedTest(name = "cuerpo = {0}")
    @ValueSource(strings = {"{\"estado\":\"\"}", "{\"estado\":\"   \"}", "{\"estado\":null}", "{}"})
    @DisplayName("Estado vacío o ausente: 400 nombrando el campo estado")
    void emptyStateReturns400(String cuerpo) throws Exception {
        cambiar("8", cuerpo, usuario(ID_PROVEEDOR_USUARIO, "PROVEEDOR"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDACION_ENTRADA"))
                .andExpect(jsonPath("$.details.estado").value("El estado es obligatorio"));
        verifyNoInteractions(verificarPermisoOperativoUseCase, reservaRepositoryPort, registrarAuditoriaUseCase);
    }

    @ParameterizedTest(name = "estado = {0}")
    @ValueSource(strings = {"FINALIZADA", "confirmado", "1", "CONFIRMADO; DROP TABLE"})
    @DisplayName("Estado que no existe: 400 nombrando el campo estado")
    void unknownStateReturns400(String estado) throws Exception {
        cambiar("8", "{\"estado\":\"" + estado + "\"}", usuario(ID_PROVEEDOR_USUARIO, "PROVEEDOR"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDACION_ENTRADA"))
                .andExpect(jsonPath("$.details.estado").exists());
        verifyNoInteractions(verificarPermisoOperativoUseCase, reservaRepositoryPort, registrarAuditoriaUseCase);
    }

    @Test
    @DisplayName("Un id que no es número responde el error de formato que ya existe")
    void nonNumericIdReturnsFormatError() throws Exception {
        cambiar("abc", "{\"estado\":\"CONFIRMADO\"}", usuario(ID_PROVEEDOR_USUARIO, "PROVEEDOR"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("FORMATO_PARAMETRO_INVALIDO"))
                .andExpect(jsonPath("$.message").value("El parámetro 'id' tiene un formato o valor inválido"));
        verifyNoInteractions(verificarPermisoOperativoUseCase, reservaRepositoryPort, registrarAuditoriaUseCase);
    }
}
