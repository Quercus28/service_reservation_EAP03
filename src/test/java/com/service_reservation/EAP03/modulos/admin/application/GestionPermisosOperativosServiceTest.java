package com.service_reservation.EAP03.modulos.admin.application;

import com.service_reservation.EAP03.modulos.admin.domain.exception.DatosAdminInvalidosException;
import com.service_reservation.EAP03.modulos.admin.domain.exception.OperacionAdminInvalidaException;
import com.service_reservation.EAP03.modulos.admin.domain.exception.RecursoAdminNoEncontradoException;
import com.service_reservation.EAP03.modulos.admin.domain.model.AccionAdmin;
import com.service_reservation.EAP03.modulos.admin.domain.model.EstadoRevocacion;
import com.service_reservation.EAP03.modulos.admin.domain.model.PermisoOperativo;
import com.service_reservation.EAP03.modulos.admin.domain.model.RegistroAuditoria;
import com.service_reservation.EAP03.modulos.admin.domain.model.RestaurarPermisoComando;
import com.service_reservation.EAP03.modulos.admin.domain.model.RevocacionPermiso;
import com.service_reservation.EAP03.modulos.admin.domain.model.RevocarPermisoComando;
import com.service_reservation.EAP03.modulos.admin.domain.model.RolOperativo;
import com.service_reservation.EAP03.modulos.admin.domain.ports.out.AuditoriaPort;
import com.service_reservation.EAP03.modulos.admin.domain.ports.out.PermisoOperativoPort;
import com.service_reservation.EAP03.modulos.admin.domain.ports.out.ReservasPort;
import com.service_reservation.EAP03.modulos.admin.domain.ports.out.RevocacionPermisoRepositoryPort;
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
import java.time.ZoneOffset;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GestionPermisosOperativosServiceTest {

    private static final Long ID_ADMIN = 1L;
    private static final Long ID_USUARIO = 42L;
    private static final String MOTIVO = "Incumplimiento reiterado";
    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 10, 17, 15, 0);

    @Mock private PermisoOperativoPort permisoOperativoPort;
    @Mock private RevocacionPermisoRepositoryPort revocacionRepository;
    @Mock private SolicitudProveedorRepositoryPort solicitudRepository;
    @Mock private ReservasPort reservasPort;
    @Mock private AuditoriaPort auditoriaPort;

    private GestionPermisosOperativosService service;

    @BeforeEach
    void setUp() {
        Clock reloj = Clock.fixed(Instant.parse("2026-10-17T15:00:00Z"), ZoneOffset.UTC);
        service = new GestionPermisosOperativosService(permisoOperativoPort, revocacionRepository, solicitudRepository,
                reservasPort, auditoriaPort, reloj);
    }

    private void permissionState(RolOperativo rol, Boolean activo) {
        when(permisoOperativoPort.estadoPermiso(ID_USUARIO, rol)).thenReturn(Optional.ofNullable(activo));
    }

    private RegistroAuditoria auditEntry() {
        ArgumentCaptor<RegistroAuditoria> captor = ArgumentCaptor.forClass(RegistroAuditoria.class);
        verify(auditoriaPort).registrar(captor.capture());
        return captor.getValue();
    }

    private static RevocarPermisoComando revoke(RolOperativo rol) {
        return new RevocarPermisoComando(ID_ADMIN, ID_USUARIO, rol, MOTIVO);
    }

    // ---------------------------------------------------------------- Revocar

    @Test
    @DisplayName("Revocar a un cliente cancela sus reservas y completa la revocación de inmediato")
    void revokesClientImmediately() {
        // Arrange
        permissionState(RolOperativo.CLIENTE, true);
        when(reservasPort.cancelarReservasActivasDeCliente(ID_USUARIO, RevocacionPermiso.MOTIVO_CANCELACION_RESERVAS)).thenReturn(3);
        when(revocacionRepository.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        RevocacionPermiso revocacion = service.revocar(revoke(RolOperativo.CLIENTE));

        // Assert
        assertEquals(EstadoRevocacion.COMPLETADA, revocacion.getEstado());
        assertEquals(3, revocacion.getReservasCanceladas());
        assertEquals(AHORA, revocacion.getFechaCierre());
        verify(permisoOperativoPort).actualizarEstado(ID_USUARIO, RolOperativo.CLIENTE, false);
        RegistroAuditoria auditoria = auditEntry();
        assertEquals(AccionAdmin.REVOCACION_COMPLETADA, auditoria.accion());
        assertEquals("reservasCanceladas=3, motivo=" + MOTIVO, auditoria.detalle());
    }

    @Test
    @DisplayName("Revocar a un proveedor sin reservas activas la completa de inmediato")
    void revokesProviderWithoutReservationsImmediately() {
        // Arrange
        permissionState(RolOperativo.PROVEEDOR, true);
        when(reservasPort.contarReservasActivasDeProveedor(ID_USUARIO)).thenReturn(0);
        when(revocacionRepository.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        RevocacionPermiso revocacion = service.revocar(revoke(RolOperativo.PROVEEDOR));

        // Assert
        assertEquals(EstadoRevocacion.COMPLETADA, revocacion.getEstado());
        assertEquals(0, revocacion.getReservasCanceladas());
        assertEquals("sin reservas activas, motivo=" + MOTIVO, auditEntry().detalle());
    }

    @Test
    @DisplayName("Revocar a un proveedor con reservas activas abre el plazo de gracia")
    void revokesProviderWithReservationsWithGracePeriod() {
        // Arrange
        permissionState(RolOperativo.PROVEEDOR, true);
        when(reservasPort.contarReservasActivasDeProveedor(ID_USUARIO)).thenReturn(2);
        when(revocacionRepository.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        RevocacionPermiso revocacion = service.revocar(revoke(RolOperativo.PROVEEDOR));

        // Assert
        assertEquals(EstadoRevocacion.PENDIENTE_CANCELACION, revocacion.getEstado());
        assertEquals(AHORA.plusDays(5), revocacion.getFechaLimite());
        RegistroAuditoria auditoria = auditEntry();
        assertEquals(AccionAdmin.REVOCACION_INICIADA, auditoria.accion());
        assertEquals("reservasActivas=2, debe cancelarlas antes de " + AHORA.plusDays(5) + ", motivo=" + MOTIVO,
                auditoria.detalle());
        verify(permisoOperativoPort).actualizarEstado(ID_USUARIO, RolOperativo.PROVEEDOR, false);
    }

    @Test
    @DisplayName("Revocar exige administrador, usuario y rol")
    void revokeRequiresAllData() {
        assertThrows(DatosAdminInvalidosException.class,
                () -> service.revocar(new RevocarPermisoComando(null, ID_USUARIO, RolOperativo.CLIENTE, MOTIVO)));
        assertThrows(DatosAdminInvalidosException.class,
                () -> service.revocar(new RevocarPermisoComando(ID_ADMIN, null, RolOperativo.CLIENTE, MOTIVO)));
        assertThrows(DatosAdminInvalidosException.class,
                () -> service.revocar(new RevocarPermisoComando(ID_ADMIN, ID_USUARIO, null, MOTIVO)));
        verifyNoInteractions(permisoOperativoPort);
    }

    @Test
    @DisplayName("Un administrador no puede revocar sus propios permisos")
    void adminCannotRevokeSelf() {
        RevocarPermisoComando comando = new RevocarPermisoComando(ID_ADMIN, ID_ADMIN, RolOperativo.CLIENTE, MOTIVO);

        assertThrows(OperacionAdminInvalidaException.class, () -> service.revocar(comando));
        verifyNoInteractions(permisoOperativoPort);
    }

    @Test
    @DisplayName("Falla si el usuario no tiene el rol")
    void userWithoutRole() {
        permissionState(RolOperativo.CLIENTE, null);

        RecursoAdminNoEncontradoException ex = assertThrows(RecursoAdminNoEncontradoException.class,
                () -> service.revocar(revoke(RolOperativo.CLIENTE)));

        assertEquals("El usuario 42 no tiene el rol CLIENTE", ex.getMessage());
    }

    @Test
    @DisplayName("Un proveedor pendiente de aprobación se rechaza en lugar de revocarse")
    void pendingProviderMustBeRejectedInstead() {
        permissionState(RolOperativo.PROVEEDOR, false);
        when(solicitudRepository.existePendienteParaUsuario(ID_USUARIO)).thenReturn(true);

        OperacionAdminInvalidaException ex = assertThrows(OperacionAdminInvalidaException.class,
                () -> service.revocar(revoke(RolOperativo.PROVEEDOR)));

        assertEquals("El proveedor aún está pendiente de aprobación; rechace la solicitud en lugar de revocar", ex.getMessage());
    }

    @Test
    @DisplayName("No revoca permisos de proveedor que ya están revocados")
    void providerAlreadyRevoked() {
        permissionState(RolOperativo.PROVEEDOR, false);
        when(solicitudRepository.existePendienteParaUsuario(ID_USUARIO)).thenReturn(false);

        OperacionAdminInvalidaException ex = assertThrows(OperacionAdminInvalidaException.class,
                () -> service.revocar(revoke(RolOperativo.PROVEEDOR)));

        assertEquals("Los permisos operativos de PROVEEDOR ya se encuentran revocados para el usuario 42", ex.getMessage());
        verify(revocacionRepository, never()).guardar(any());
    }

    @Test
    @DisplayName("No revoca permisos de cliente que ya están revocados")
    void clientAlreadyRevoked() {
        permissionState(RolOperativo.CLIENTE, false);

        assertThrows(OperacionAdminInvalidaException.class, () -> service.revocar(revoke(RolOperativo.CLIENTE)));
        verifyNoInteractions(solicitudRepository);
    }

    // ---------------------------------------------------------------- Restaurar

    @Test
    @DisplayName("Restaurar a un proveedor aprobado anula su revocación pendiente y enciende los permisos")
    void restoresApprovedProvider() {
        // Arrange
        permissionState(RolOperativo.PROVEEDOR, false);
        when(solicitudRepository.existeAprobadaParaUsuario(ID_USUARIO)).thenReturn(true);
        RevocacionPermiso pendiente = RevocacionPermiso.conPlazoDeGracia(ID_USUARIO, MOTIVO, ID_ADMIN, AHORA.minusDays(1));
        when(revocacionRepository.buscarPendiente(ID_USUARIO, RolOperativo.PROVEEDOR)).thenReturn(Optional.of(pendiente));

        // Act
        service.restaurar(new RestaurarPermisoComando(ID_ADMIN, ID_USUARIO, RolOperativo.PROVEEDOR, "  Apelación aceptada "));

        // Assert
        assertEquals(EstadoRevocacion.ANULADA, pendiente.getEstado());
        verify(revocacionRepository).guardar(pendiente);
        verify(permisoOperativoPort).actualizarEstado(ID_USUARIO, RolOperativo.PROVEEDOR, true);
        RegistroAuditoria auditoria = auditEntry();
        assertEquals(AccionAdmin.PERMISOS_RESTAURADOS, auditoria.accion());
        assertEquals("motivo=Apelación aceptada", auditoria.detalle());
    }

    @Test
    @DisplayName("Restaurar a un cliente sin motivo registra un guion como motivo")
    void restoresClientWithoutReason() {
        // Arrange
        permissionState(RolOperativo.CLIENTE, false);
        when(revocacionRepository.buscarPendiente(ID_USUARIO, RolOperativo.CLIENTE)).thenReturn(Optional.empty());

        // Act
        service.restaurar(new RestaurarPermisoComando(ID_ADMIN, ID_USUARIO, RolOperativo.CLIENTE, null));

        // Assert
        assertEquals("motivo=-", auditEntry().detalle());
        verify(revocacionRepository, never()).guardar(any());
        verifyNoInteractions(solicitudRepository);
    }

    @Test
    @DisplayName("Restaurar con motivo en blanco también registra un guion")
    void restoresWithBlankReason() {
        permissionState(RolOperativo.CLIENTE, false);
        when(revocacionRepository.buscarPendiente(ID_USUARIO, RolOperativo.CLIENTE)).thenReturn(Optional.empty());

        service.restaurar(new RestaurarPermisoComando(ID_ADMIN, ID_USUARIO, RolOperativo.CLIENTE, "   "));

        assertEquals("motivo=-", auditEntry().detalle());
    }

    @Test
    @DisplayName("Restaurar exige administrador, usuario y rol")
    void restoreRequiresAllData() {
        assertThrows(DatosAdminInvalidosException.class,
                () -> service.restaurar(new RestaurarPermisoComando(ID_ADMIN, ID_USUARIO, null, null)));
        verifyNoInteractions(permisoOperativoPort);
    }

    @Test
    @DisplayName("No restaura permisos que ya están activos")
    void rejectsRestoringActivePermissions() {
        permissionState(RolOperativo.CLIENTE, true);

        OperacionAdminInvalidaException ex = assertThrows(OperacionAdminInvalidaException.class,
                () -> service.restaurar(new RestaurarPermisoComando(ID_ADMIN, ID_USUARIO, RolOperativo.CLIENTE, null)));

        assertEquals("El usuario 42 ya tiene permisos operativos de CLIENTE", ex.getMessage());
    }

    @Test
    @DisplayName("No restaura a un proveedor cuya cuenta no fue aprobada")
    void rejectsRestoringUnapprovedProvider() {
        permissionState(RolOperativo.PROVEEDOR, false);
        when(solicitudRepository.existeAprobadaParaUsuario(ID_USUARIO)).thenReturn(false);

        assertThrows(OperacionAdminInvalidaException.class,
                () -> service.restaurar(new RestaurarPermisoComando(ID_ADMIN, ID_USUARIO, RolOperativo.PROVEEDOR, null)));
        verify(permisoOperativoPort, never()).actualizarEstado(any(), any(), anyBoolean());
    }

    // ---------------------------------------------------------------- Consultas

    @Test
    @DisplayName("Consultar permisos indica el estado de cada rol y si hay revocación pendiente")
    void queriesPermissions() {
        // Arrange
        Map<RolOperativo, Boolean> permisos = new EnumMap<>(RolOperativo.class);
        permisos.put(RolOperativo.CLIENTE, true);
        permisos.put(RolOperativo.PROVEEDOR, false);
        when(permisoOperativoPort.listarPermisos(ID_USUARIO)).thenReturn(permisos);
        when(revocacionRepository.buscarPendiente(ID_USUARIO, RolOperativo.CLIENTE)).thenReturn(Optional.empty());
        when(revocacionRepository.buscarPendiente(ID_USUARIO, RolOperativo.PROVEEDOR))
                .thenReturn(Optional.of(RevocacionPermiso.conPlazoDeGracia(ID_USUARIO, MOTIVO, ID_ADMIN, AHORA)));

        // Act
        List<PermisoOperativo> resultado = service.consultarPermisos(ID_USUARIO);

        // Assert
        assertEquals(List.of(new PermisoOperativo(RolOperativo.CLIENTE, true, false),
                new PermisoOperativo(RolOperativo.PROVEEDOR, false, true)), resultado);
    }

    @Test
    @DisplayName("Consultar permisos falla si el usuario no tiene roles operativos")
    void queryPermissionsOfUnknownUser() {
        when(permisoOperativoPort.listarPermisos(ID_USUARIO)).thenReturn(Map.of());

        assertThrows(RecursoAdminNoEncontradoException.class, () -> service.consultarPermisos(ID_USUARIO));
    }

    @Test
    @DisplayName("Listar revocaciones delega en el repositorio")
    void listsRevocations() {
        List<RevocacionPermiso> revocaciones = List.of(RevocacionPermiso.conPlazoDeGracia(ID_USUARIO, MOTIVO, ID_ADMIN, AHORA));
        when(revocacionRepository.listar(EstadoRevocacion.PENDIENTE_CANCELACION)).thenReturn(revocaciones);

        assertEquals(revocaciones, service.listarRevocaciones(EstadoRevocacion.PENDIENTE_CANCELACION));
    }
}
