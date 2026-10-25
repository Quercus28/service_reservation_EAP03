package com.service_reservation.EAP03.modulos.admin.application;

import com.service_reservation.EAP03.modulos.admin.domain.exception.DatosAdminInvalidosException;
import com.service_reservation.EAP03.modulos.admin.domain.exception.OperacionAdminInvalidaException;
import com.service_reservation.EAP03.modulos.admin.domain.exception.RecursoAdminNoEncontradoException;
import com.service_reservation.EAP03.modulos.admin.domain.model.AccionAdmin;
import com.service_reservation.EAP03.modulos.admin.domain.model.ActualizarProveedorPerfilComando;
import com.service_reservation.EAP03.modulos.admin.domain.model.EstadoSolicitud;
import com.service_reservation.EAP03.modulos.admin.domain.model.ProveedorPerfilDTO;
import com.service_reservation.EAP03.modulos.admin.domain.model.RegistroAuditoria;
import com.service_reservation.EAP03.modulos.admin.domain.model.RolOperativo;
import com.service_reservation.EAP03.modulos.admin.domain.model.SolicitudAprobacionProveedor;
import com.service_reservation.EAP03.modulos.admin.domain.ports.out.AuditoriaPort;
import com.service_reservation.EAP03.modulos.admin.domain.ports.out.PermisoOperativoPort;
import com.service_reservation.EAP03.modulos.admin.domain.ports.out.ProveedorPerfilPort;
import com.service_reservation.EAP03.modulos.admin.domain.ports.out.SolicitudProveedorRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GestionSolicitudesProveedorServiceTest {

    @Mock
    private SolicitudProveedorRepositoryPort solicitudRepository;

    @Mock
    private PermisoOperativoPort permisoOperativoPort;

    @Mock
    private ProveedorPerfilPort proveedorPerfilPort;

    @Mock
    private AuditoriaPort auditoriaPort;

    private Clock clock;
    private GestionSolicitudesProveedorService service;

    private static final Long ID_USUARIO = 42L;
    private static final Long ID_ADMIN = 1L;
    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 10, 17, 10, 0);

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(Instant.parse("2026-10-17T15:00:00Z"), ZoneId.of("UTC"));
        service = new GestionSolicitudesProveedorService(
                solicitudRepository,
                permisoOperativoPort,
                proveedorPerfilPort,
                auditoriaPort,
                clock
        );
    }

    @Test
    @DisplayName("Cuando no existe solicitud previa, registrarSolicitud crea una nueva en PENDIENTE y apaga permisos")
    void registrarSolicitud_cuandoNoExisteSolicitudPrevia_creaNuevaSolicitud() {
        when(solicitudRepository.buscarUltimaPorUsuario(ID_USUARIO)).thenReturn(Optional.empty());
        when(solicitudRepository.guardar(any(SolicitudAprobacionProveedor.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        SolicitudAprobacionProveedor resultado = service.registrarSolicitud(ID_USUARIO);

        assertNotNull(resultado);
        assertEquals(ID_USUARIO, resultado.getIdUsuario());
        assertEquals(EstadoSolicitud.PENDIENTE, resultado.getEstado());

        verify(permisoOperativoPort).actualizarEstado(ID_USUARIO, RolOperativo.PROVEEDOR, false);
        verify(solicitudRepository).guardar(any(SolicitudAprobacionProveedor.class));
        verify(auditoriaPort).registrar(any());
    }

    @Test
    @DisplayName("Cuando la última solicitud está PENDIENTE, registrarSolicitud es idempotente y no altera permisos")
    void registrarSolicitud_cuandoExisteSolicitudPendiente_retornaExistenteSinModificarPermisos() {
        SolicitudAprobacionProveedor pendiente = SolicitudAprobacionProveedor.nueva(ID_USUARIO, AHORA);
        when(solicitudRepository.buscarUltimaPorUsuario(ID_USUARIO)).thenReturn(Optional.of(pendiente));

        SolicitudAprobacionProveedor resultado = service.registrarSolicitud(ID_USUARIO);

        assertSame(pendiente, resultado);
        assertEquals(EstadoSolicitud.PENDIENTE, resultado.getEstado());

        verify(permisoOperativoPort, never()).actualizarEstado(anyLong(), any(), anyBoolean());
        verify(solicitudRepository, never()).guardar(any());
    }

    @Test
    @DisplayName("Cuando el proveedor ya está APROBADO y activo, registrarSolicitud lanza excepción y mantiene activo=true")
    void registrarSolicitud_cuandoExisteSolicitudAprobadaYActivo_lanzaExcepcion() {
        SolicitudAprobacionProveedor aprobada = new SolicitudAprobacionProveedor(
                10L, ID_USUARIO, EstadoSolicitud.APROBADA, AHORA, AHORA, ID_ADMIN, null);
        when(solicitudRepository.buscarUltimaPorUsuario(ID_USUARIO)).thenReturn(Optional.of(aprobada));
        when(permisoOperativoPort.estadoPermiso(ID_USUARIO, RolOperativo.PROVEEDOR)).thenReturn(Optional.of(true));

        OperacionAdminInvalidaException ex = assertThrows(
                OperacionAdminInvalidaException.class,
                () -> service.registrarSolicitud(ID_USUARIO)
        );

        assertTrue(ex.getMessage().contains("ya se encuentra aprobada y activa"));
        verify(permisoOperativoPort, never()).actualizarEstado(anyLong(), any(), anyBoolean());
        verify(solicitudRepository, never()).guardar(any());
    }

    @Test
    @DisplayName("Cuando el proveedor está APROBADO pero fue REVOCADO (activo=false), registrarSolicitud bloquea el bypass y exige restauración")
    void registrarSolicitud_cuandoExisteSolicitudAprobadaPeroRevocado_lanzaExcepcion() {
        SolicitudAprobacionProveedor aprobada = new SolicitudAprobacionProveedor(
                10L, ID_USUARIO, EstadoSolicitud.APROBADA, AHORA, AHORA, ID_ADMIN, null);
        when(solicitudRepository.buscarUltimaPorUsuario(ID_USUARIO)).thenReturn(Optional.of(aprobada));
        when(permisoOperativoPort.estadoPermiso(ID_USUARIO, RolOperativo.PROVEEDOR)).thenReturn(Optional.of(false));

        OperacionAdminInvalidaException ex = assertThrows(
                OperacionAdminInvalidaException.class,
                () -> service.registrarSolicitud(ID_USUARIO)
        );

        assertTrue(ex.getMessage().contains("fueron revocados"));
        assertTrue(ex.getMessage().contains("restauración"));
        verify(permisoOperativoPort, never()).actualizarEstado(anyLong(), any(), anyBoolean());
        verify(solicitudRepository, never()).guardar(any());
    }

    @Test
    @DisplayName("Cuando la última solicitud fue RECHAZADA, registrarSolicitud indica usar el endpoint de reenvío")
    void registrarSolicitud_cuandoExisteSolicitudRechazada_indicaUsarReenviar() {
        SolicitudAprobacionProveedor rechazada = new SolicitudAprobacionProveedor(
                10L, ID_USUARIO, EstadoSolicitud.RECHAZADA, AHORA, AHORA, ID_ADMIN, "Documentación incompleta");
        when(solicitudRepository.buscarUltimaPorUsuario(ID_USUARIO)).thenReturn(Optional.of(rechazada));

        OperacionAdminInvalidaException ex = assertThrows(
                OperacionAdminInvalidaException.class,
                () -> service.registrarSolicitud(ID_USUARIO)
        );

        assertTrue(ex.getMessage().contains("fue rechazada"));
        assertTrue(ex.getMessage().contains("reenviar"));
        verify(permisoOperativoPort, never()).actualizarEstado(anyLong(), any(), anyBoolean());
        verify(solicitudRepository, never()).guardar(any());
    }

    @Test
    @DisplayName("reenviarSolicitud permite volver a solicitar solo cuando el estado previo fue RECHAZADA")
    void reenviarSolicitud_cuandoFueRechazada_creaNuevaSolicitud() {
        SolicitudAprobacionProveedor rechazada = new SolicitudAprobacionProveedor(
                10L, ID_USUARIO, EstadoSolicitud.RECHAZADA, AHORA, AHORA, ID_ADMIN, "Documentación incompleta");
        when(permisoOperativoPort.estadoPermiso(ID_USUARIO, RolOperativo.PROVEEDOR)).thenReturn(Optional.of(false));
        when(solicitudRepository.buscarUltimaPorUsuario(ID_USUARIO)).thenReturn(Optional.of(rechazada));
        when(solicitudRepository.guardar(any(SolicitudAprobacionProveedor.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        SolicitudAprobacionProveedor resultado = service.reenviarSolicitud(ID_USUARIO);

        assertNotNull(resultado);
        assertEquals(EstadoSolicitud.PENDIENTE, resultado.getEstado());
        verify(permisoOperativoPort).actualizarEstado(ID_USUARIO, RolOperativo.PROVEEDOR, false);
        verify(solicitudRepository).guardar(any(SolicitudAprobacionProveedor.class));
    }

    @Test
    @DisplayName("aprobar enciende los permisos operativos del proveedor")
    void aprobar_enciendePermisosOperativos() {
        SolicitudAprobacionProveedor pendiente = SolicitudAprobacionProveedor.nueva(ID_USUARIO, AHORA);
        when(solicitudRepository.buscarPorId(50L)).thenReturn(Optional.of(pendiente));
        when(permisoOperativoPort.estadoPermiso(ID_USUARIO, RolOperativo.PROVEEDOR)).thenReturn(Optional.of(false));
        when(solicitudRepository.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        SolicitudAprobacionProveedor aprobada = service.aprobar(ID_ADMIN, 50L);

        assertEquals(EstadoSolicitud.APROBADA, aprobada.getEstado());
        verify(permisoOperativoPort).actualizarEstado(ID_USUARIO, RolOperativo.PROVEEDOR, true);
        verify(auditoriaPort).registrar(any());
    }

    @Test
    @DisplayName("rechazar cambia estado a RECHAZADA con motivo y no enciende permisos")
    void rechazar_cambiaEstadoARechazada() {
        SolicitudAprobacionProveedor pendiente = SolicitudAprobacionProveedor.nueva(ID_USUARIO, AHORA);
        when(solicitudRepository.buscarPorId(50L)).thenReturn(Optional.of(pendiente));
        when(solicitudRepository.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        SolicitudAprobacionProveedor rechazada = service.rechazar(ID_ADMIN, 50L, "Falta documentación legal");

        assertEquals(EstadoSolicitud.RECHAZADA, rechazada.getEstado());
        assertEquals("Falta documentación legal", rechazada.getMotivoRechazo());
        verify(permisoOperativoPort, never()).actualizarEstado(anyLong(), any(), eq(true));
        verify(auditoriaPort).registrar(any());
    }

    private void hasProviderRole() {
        when(permisoOperativoPort.estadoPermiso(ID_USUARIO, RolOperativo.PROVEEDOR)).thenReturn(Optional.of(false));
    }

    private static ProveedorPerfilDTO profile(String razonSocial) {
        return new ProveedorPerfilDTO(ID_USUARIO, razonSocial, "900123-1", "3001234567");
    }

    @Test
    @DisplayName("Reenviar sin solicitudes previas crea una solicitud nueva")
    void resubmitWithoutPreviousRequestCreatesOne() {
        // Arrange
        hasProviderRole();
        when(solicitudRepository.buscarUltimaPorUsuario(ID_USUARIO)).thenReturn(Optional.empty());
        when(solicitudRepository.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        SolicitudAprobacionProveedor solicitud = service.reenviarSolicitud(ID_USUARIO);

        // Assert
        assertEquals(EstadoSolicitud.PENDIENTE, solicitud.getEstado());
        ArgumentCaptor<RegistroAuditoria> captor = ArgumentCaptor.forClass(RegistroAuditoria.class);
        verify(auditoriaPort).registrar(captor.capture());
        assertEquals(AccionAdmin.SOLICITUD_PROVEEDOR_REENVIADA, captor.getValue().accion());
    }

    @Test
    @DisplayName("Reenviar solo se permite tras un rechazo")
    void resubmitOnlyAfterRejection() {
        hasProviderRole();
        when(solicitudRepository.buscarUltimaPorUsuario(ID_USUARIO))
                .thenReturn(Optional.of(SolicitudAprobacionProveedor.nueva(ID_USUARIO, AHORA)));

        OperacionAdminInvalidaException ex = assertThrows(OperacionAdminInvalidaException.class,
                () -> service.reenviarSolicitud(ID_USUARIO));

        assertEquals("Solo se puede volver a solicitar aprobación tras un rechazo. Estado actual: PENDIENTE", ex.getMessage());
        verify(solicitudRepository, never()).guardar(any());
    }

    @Test
    @DisplayName("Reenviar falla si el usuario no tiene el rol de proveedor")
    void resubmitRequiresProviderRole() {
        when(permisoOperativoPort.estadoPermiso(ID_USUARIO, RolOperativo.PROVEEDOR)).thenReturn(Optional.empty());

        RecursoAdminNoEncontradoException ex = assertThrows(RecursoAdminNoEncontradoException.class,
                () -> service.reenviarSolicitud(ID_USUARIO));

        assertEquals("El usuario 42 no tiene el rol PROVEEDOR", ex.getMessage());
    }

    @Test
    @DisplayName("Consulta la última solicitud del usuario")
    void queriesLastRequest() {
        SolicitudAprobacionProveedor ultima = SolicitudAprobacionProveedor.nueva(ID_USUARIO, AHORA);
        when(solicitudRepository.buscarUltimaPorUsuario(ID_USUARIO)).thenReturn(Optional.of(ultima));

        assertSame(ultima, service.consultarUltimaSolicitud(ID_USUARIO));
    }

    @Test
    @DisplayName("Consultar la última solicitud falla si el usuario no tiene ninguna")
    void queryLastRequestWithoutRequests() {
        when(solicitudRepository.buscarUltimaPorUsuario(ID_USUARIO)).thenReturn(Optional.empty());

        assertThrows(RecursoAdminNoEncontradoException.class, () -> service.consultarUltimaSolicitud(ID_USUARIO));
    }

    @Test
    @DisplayName("Aprobar falla si la solicitud no existe")
    void approveUnknownRequest() {
        when(solicitudRepository.buscarPorId(50L)).thenReturn(Optional.empty());

        RecursoAdminNoEncontradoException ex = assertThrows(RecursoAdminNoEncontradoException.class,
                () -> service.aprobar(ID_ADMIN, 50L));

        assertEquals("Solicitud de aprobación no encontrada: 50", ex.getMessage());
    }

    @Test
    @DisplayName("Listar delega en el repositorio con el filtro de estado")
    void listsRequests() {
        List<SolicitudAprobacionProveedor> solicitudes = List.of(SolicitudAprobacionProveedor.nueva(ID_USUARIO, AHORA));
        when(solicitudRepository.listar(EstadoSolicitud.PENDIENTE)).thenReturn(solicitudes);

        assertEquals(solicitudes, service.listar(EstadoSolicitud.PENDIENTE));
    }

    @Test
    @DisplayName("Consulta el perfil del proveedor")
    void queriesProviderProfile() {
        when(proveedorPerfilPort.buscarPorIdUsuario(ID_USUARIO)).thenReturn(Optional.of(profile("Empresa SAS")));

        assertEquals(profile("Empresa SAS"), service.consultarPerfilProveedor(ID_USUARIO));
    }

    @Test
    @DisplayName("Consultar el perfil exige el usuario y que el perfil exista")
    void queryProfileRequiresExistingUser() {
        when(proveedorPerfilPort.buscarPorIdUsuario(ID_USUARIO)).thenReturn(Optional.empty());

        assertThrows(DatosAdminInvalidosException.class, () -> service.consultarPerfilProveedor(null));
        RecursoAdminNoEncontradoException ex = assertThrows(RecursoAdminNoEncontradoException.class,
                () -> service.consultarPerfilProveedor(ID_USUARIO));
        assertEquals("No se encontró el perfil de proveedor para el usuario 42", ex.getMessage());
    }

    @Test
    @DisplayName("Actualiza el perfil con los datos recortados y lo audita")
    void updatesProfileWithTrimmedData() {
        // Arrange
        when(proveedorPerfilPort.buscarPorIdUsuario(ID_USUARIO))
                .thenReturn(Optional.of(profile("Empresa")), Optional.of(profile("Empresa SAS")));

        // Act
        ProveedorPerfilDTO actualizado = service.actualizarPerfilProveedor(
                new ActualizarProveedorPerfilComando(ID_ADMIN, ID_USUARIO, " Empresa SAS ", " 900123-1 ", " 3001234567 "));

        // Assert
        assertEquals(profile("Empresa SAS"), actualizado);
        verify(proveedorPerfilPort).actualizarPerfil(ID_USUARIO, "Empresa SAS", "900123-1", "3001234567");
        ArgumentCaptor<RegistroAuditoria> captor = ArgumentCaptor.forClass(RegistroAuditoria.class);
        verify(auditoriaPort).registrar(captor.capture());
        assertEquals(AccionAdmin.PERFIL_PROVEEDOR_ACTUALIZADO, captor.getValue().accion());
        assertEquals("perfil actualizado por admin", captor.getValue().detalle());
    }

    @Test
    @DisplayName("Actualiza el perfil dejando nulos el NIT y el teléfono si no se envían")
    void updatesProfileWithoutOptionalData() {
        when(proveedorPerfilPort.buscarPorIdUsuario(ID_USUARIO)).thenReturn(Optional.of(profile("Empresa")));

        service.actualizarPerfilProveedor(new ActualizarProveedorPerfilComando(ID_ADMIN, ID_USUARIO, "Empresa", null, null));

        verify(proveedorPerfilPort).actualizarPerfil(ID_USUARIO, "Empresa", null, null);
    }

    @Test
    @DisplayName("Actualizar el perfil exige comando, administrador, usuario y razón social")
    void updateProfileRequiresData() {
        assertThrows(DatosAdminInvalidosException.class, () -> service.actualizarPerfilProveedor(null));
        ActualizarProveedorPerfilComando sinAdmin = new ActualizarProveedorPerfilComando(null, ID_USUARIO, "Empresa", null, null);
        assertThrows(DatosAdminInvalidosException.class, () -> service.actualizarPerfilProveedor(sinAdmin));
        ActualizarProveedorPerfilComando sinUsuario = new ActualizarProveedorPerfilComando(ID_ADMIN, null, "Empresa", null, null);
        assertThrows(DatosAdminInvalidosException.class, () -> service.actualizarPerfilProveedor(sinUsuario));
        ActualizarProveedorPerfilComando sinRazonSocial = new ActualizarProveedorPerfilComando(ID_ADMIN, ID_USUARIO, null, null, null);
        assertThrows(DatosAdminInvalidosException.class, () -> service.actualizarPerfilProveedor(sinRazonSocial));
        ActualizarProveedorPerfilComando razonSocialEnBlanco = new ActualizarProveedorPerfilComando(ID_ADMIN, ID_USUARIO, "  ", null, null);
        assertThrows(DatosAdminInvalidosException.class, () -> service.actualizarPerfilProveedor(razonSocialEnBlanco));
        verifyNoInteractions(proveedorPerfilPort);
    }

    @Test
    @DisplayName("Actualizar el perfil falla si el proveedor no tiene perfil")
    void updateProfileOfUnknownProvider() {
        when(proveedorPerfilPort.buscarPorIdUsuario(ID_USUARIO)).thenReturn(Optional.empty());
        ActualizarProveedorPerfilComando comando = new ActualizarProveedorPerfilComando(ID_ADMIN, ID_USUARIO, "Empresa", null, null);

        assertThrows(RecursoAdminNoEncontradoException.class, () -> service.actualizarPerfilProveedor(comando));
        verify(proveedorPerfilPort, never()).actualizarPerfil(any(), any(), any(), any());
    }
}
