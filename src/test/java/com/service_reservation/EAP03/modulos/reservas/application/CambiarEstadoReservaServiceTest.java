package com.service_reservation.EAP03.modulos.reservas.application;

import com.service_reservation.EAP03.modulos.admin.domain.model.AccionAdmin;
import com.service_reservation.EAP03.modulos.admin.domain.model.EventoAuditoria;
import com.service_reservation.EAP03.modulos.admin.domain.model.ResultadoAuditoria;
import com.service_reservation.EAP03.modulos.admin.domain.model.RolOperativo;
import com.service_reservation.EAP03.modulos.admin.domain.ports.in.RegistrarAuditoriaUseCase;
import com.service_reservation.EAP03.modulos.admin.domain.ports.in.VerificarPermisoOperativoUseCase;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.in.ConsultarProveedorUseCase;
import com.service_reservation.EAP03.modulos.reservas.domain.exception.DatosReservaInvalidosException;
import com.service_reservation.EAP03.modulos.reservas.domain.exception.ReservaAjenaException;
import com.service_reservation.EAP03.modulos.reservas.domain.exception.CambioEstadoNoPermitidoException;
import com.service_reservation.EAP03.modulos.reservas.domain.exception.ReservaNoEncontradaException;
import com.service_reservation.EAP03.modulos.reservas.domain.exception.UsuarioNoEsProveedorException;
import com.service_reservation.EAP03.modulos.reservas.domain.model.CambiarEstadoReservaComando;
import com.service_reservation.EAP03.modulos.reservas.domain.model.EstadoReserva;
import com.service_reservation.EAP03.modulos.reservas.domain.model.IntervaloReservado;
import com.service_reservation.EAP03.modulos.reservas.domain.model.PartesReserva;
import com.service_reservation.EAP03.modulos.reservas.domain.model.Reserva;
import com.service_reservation.EAP03.modulos.reservas.domain.ports.out.ReservaRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CambiarEstadoReservaServiceTest {

    private static final Long ID_USUARIO = 50L;
    private static final Integer ID_PROVEEDOR = 7;
    private static final Integer ID_RESERVA = 8;
    private static final String ENDPOINT = "PATCH /api/v1/reservas/8/estado";
    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 12, 2, 9, 0);

    @Mock private VerificarPermisoOperativoUseCase verificarPermisoOperativoUseCase;
    @Mock private ConsultarProveedorUseCase consultarProveedorUseCase;
    @Mock private ReservaRepositoryPort reservaRepositoryPort;
    @Mock private RegistrarAuditoriaUseCase registrarAuditoriaUseCase;

    @InjectMocks private CambiarEstadoReservaService service;

    private static Reserva reserva(Integer idProveedor, EstadoReserva estado) {
        return Reserva.reconstruir(ID_RESERVA, new PartesReserva(3L, 10, idProveedor),
                new IntervaloReservado(INICIO, INICIO.plusHours(1)), estado, null, INICIO.minusDays(5), null);
    }

    private static CambiarEstadoReservaComando comando(EstadoReserva nuevo) {
        return new CambiarEstadoReservaComando(ID_USUARIO, ID_RESERVA, nuevo, ENDPOINT);
    }

    private void esProveedorActivo() {
        when(verificarPermisoOperativoUseCase.tienePermisoOperativo(ID_USUARIO, RolOperativo.PROVEEDOR)).thenReturn(true);
        when(consultarProveedorUseCase.buscarIdProveedorPorUsuario(ID_USUARIO)).thenReturn(Optional.of(ID_PROVEEDOR));
    }

    private EventoAuditoria auditado() {
        ArgumentCaptor<EventoAuditoria> captor = ArgumentCaptor.forClass(EventoAuditoria.class);
        verify(registrarAuditoriaUseCase).ejecutar(captor.capture());
        return captor.getValue();
    }

    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource({"RESERVADO,CONFIRMADO", "CONFIRMADO,COMPLETADO"})
    @DisplayName("El proveedor dueño con permiso activo cambia el estado y se guarda, sin auditoría")
    void ownerChangesState(EstadoReserva desde, EstadoReserva hacia) {
        // Arrange
        esProveedorActivo();
        when(reservaRepositoryPort.buscarPorId(ID_RESERVA)).thenReturn(Optional.of(reserva(ID_PROVEEDOR, desde)));
        when(reservaRepositoryPort.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Reserva resultado = service.ejecutar(comando(hacia));

        // Assert
        assertEquals(hacia, resultado.getEstado());
        verify(reservaRepositoryPort).guardar(resultado);
        verifyNoInteractions(registrarAuditoriaUseCase);
    }

    @Test
    @DisplayName("Un cliente (sin permiso de proveedor) recibe 403 y queda en la auditoría sin datos personales")
    void clientRejectedAndAudited() {
        // Arrange
        when(verificarPermisoOperativoUseCase.tienePermisoOperativo(ID_USUARIO, RolOperativo.PROVEEDOR)).thenReturn(false);

        // Act
        CambiarEstadoReservaComando comando = comando(EstadoReserva.CONFIRMADO);
        assertThrows(UsuarioNoEsProveedorException.class, () -> service.ejecutar(comando));

        // Assert
        EventoAuditoria evento = auditado();
        assertEquals(ID_USUARIO, evento.idActor());
        assertNull(evento.idUsuarioAfectado());
        assertEquals(AccionAdmin.CAMBIO_ESTADO_RESERVA_DENEGADO, evento.accion());
        assertEquals(ResultadoAuditoria.DENEGADO, evento.resultado());
        assertEquals(ENDPOINT, evento.endpoint());
        assertEquals("idReserva=8 estadoPedido=CONFIRMADO motivo=no es proveedor con permiso activo", evento.detalle());
        verifyNoInteractions(reservaRepositoryPort, consultarProveedorUseCase);
    }

    @Test
    @DisplayName("Un proveedor con el permiso inactivo también recibe 403 y queda auditado")
    void inactiveProviderRejectedAndAudited() {
        // Arrange
        when(verificarPermisoOperativoUseCase.tienePermisoOperativo(ID_USUARIO, RolOperativo.PROVEEDOR)).thenReturn(false);

        // Act
        assertThrows(UsuarioNoEsProveedorException.class, () -> service.ejecutar(comando(EstadoReserva.COMPLETADO)));

        // Assert
        assertEquals(AccionAdmin.CAMBIO_ESTADO_RESERVA_DENEGADO, auditado().accion());
        verify(reservaRepositoryPort, never()).guardar(any());
    }

    @Test
    @DisplayName("Con permiso pero sin perfil de proveedor recibe 403 y queda auditado")
    void permissionWithoutProviderProfile() {
        // Arrange
        when(verificarPermisoOperativoUseCase.tienePermisoOperativo(ID_USUARIO, RolOperativo.PROVEEDOR)).thenReturn(true);
        when(consultarProveedorUseCase.buscarIdProveedorPorUsuario(ID_USUARIO)).thenReturn(Optional.empty());

        // Act
        assertThrows(UsuarioNoEsProveedorException.class, () -> service.ejecutar(comando(EstadoReserva.CONFIRMADO)));

        // Assert
        assertEquals(AccionAdmin.CAMBIO_ESTADO_RESERVA_DENEGADO, auditado().accion());
        verifyNoInteractions(reservaRepositoryPort);
    }

    @Test
    @DisplayName("Sin usuario identificado se rechaza y se audita, sin consultar permisos")
    void withoutUser() {
        // Act
        assertThrows(UsuarioNoEsProveedorException.class, () -> service.ejecutar(
                new CambiarEstadoReservaComando(null, ID_RESERVA, EstadoReserva.CONFIRMADO, ENDPOINT)));

        // Assert
        assertNull(auditado().idActor());
        verifyNoInteractions(verificarPermisoOperativoUseCase, reservaRepositoryPort);
    }

    @Test
    @DisplayName("Un proveedor que no es el dueño recibe 403, queda auditado y la reserva no cambia")
    void otherProviderRejectedAndAudited() {
        // Arrange
        esProveedorActivo();
        Reserva ajena = reserva(99, EstadoReserva.RESERVADO);
        when(reservaRepositoryPort.buscarPorId(ID_RESERVA)).thenReturn(Optional.of(ajena));

        // Act
        ReservaAjenaException ex = assertThrows(ReservaAjenaException.class,
                () -> service.ejecutar(comando(EstadoReserva.CONFIRMADO)));

        // Assert
        assertEquals(ReservaAjenaException.deOtroProveedor(ID_RESERVA).getMessage(), ex.getMessage());
        EventoAuditoria evento = auditado();
        assertEquals(AccionAdmin.CAMBIO_ESTADO_RESERVA_DENEGADO, evento.accion());
        assertEquals("idReserva=8 estadoPedido=CONFIRMADO motivo=reserva de otro proveedor", evento.detalle());
        assertEquals(EstadoReserva.RESERVADO, ajena.getEstado());
        verify(reservaRepositoryPort, never()).guardar(any());
    }

    @Test
    @DisplayName("Si la reserva no existe responde 404 y no se audita")
    void reservationNotFound() {
        // Arrange
        esProveedorActivo();
        when(reservaRepositoryPort.buscarPorId(ID_RESERVA)).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(ReservaNoEncontradaException.class, () -> service.ejecutar(comando(EstadoReserva.CONFIRMADO)));
        verifyNoInteractions(registrarAuditoriaUseCase);
    }

    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource({"RESERVADO,COMPLETADO", "CONFIRMADO,RESERVADO", "COMPLETADO,CONFIRMADO", "CANCELADO,CONFIRMADO",
            "RESERVADO,CANCELADO", "RESERVADO,RESERVADO"})
    @DisplayName("El dueño no puede hacer un cambio no permitido: error de estado, sin guardar ni auditar")
    void invalidChange(EstadoReserva desde, EstadoReserva hacia) {
        // Arrange
        esProveedorActivo();
        when(reservaRepositoryPort.buscarPorId(ID_RESERVA)).thenReturn(Optional.of(reserva(ID_PROVEEDOR, desde)));

        // Act
        CambioEstadoNoPermitidoException ex = assertThrows(CambioEstadoNoPermitidoException.class,
                () -> service.ejecutar(comando(hacia)));

        // Assert
        assertTrue(ex.getMessage().startsWith("La acción no es válida para el estado actual"));
        verify(reservaRepositoryPort, never()).guardar(any());
        verifyNoInteractions(registrarAuditoriaUseCase);
    }

    @Test
    @DisplayName("Sin id de reserva o sin estado responde datos inválidos")
    void missingData() {
        assertThrows(DatosReservaInvalidosException.class, () -> service.ejecutar(
                new CambiarEstadoReservaComando(ID_USUARIO, null, EstadoReserva.CONFIRMADO, ENDPOINT)));
        assertThrows(DatosReservaInvalidosException.class, () -> service.ejecutar(
                new CambiarEstadoReservaComando(ID_USUARIO, ID_RESERVA, null, ENDPOINT)));
        verifyNoInteractions(verificarPermisoOperativoUseCase, registrarAuditoriaUseCase);
    }
}
