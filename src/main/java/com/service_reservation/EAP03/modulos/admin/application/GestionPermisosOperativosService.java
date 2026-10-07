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
import com.service_reservation.EAP03.modulos.admin.domain.ports.in.GestionarPermisosOperativosUseCase;
import com.service_reservation.EAP03.modulos.admin.domain.ports.out.AuditoriaPort;
import com.service_reservation.EAP03.modulos.admin.domain.ports.out.PermisoOperativoPort;
import com.service_reservation.EAP03.modulos.admin.domain.ports.out.ReservasPort;
import com.service_reservation.EAP03.modulos.admin.domain.ports.out.RevocacionPermisoRepositoryPort;
import com.service_reservation.EAP03.modulos.admin.domain.ports.out.SolicitudProveedorRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class GestionPermisosOperativosService implements GestionarPermisosOperativosUseCase {

    private final PermisoOperativoPort permisoOperativoPort;
    private final RevocacionPermisoRepositoryPort revocacionRepository;
    private final SolicitudProveedorRepositoryPort solicitudRepository;
    private final ReservasPort reservasPort;
    private final AuditoriaPort auditoriaPort;
    private final Clock clock;

    public GestionPermisosOperativosService(PermisoOperativoPort permisoOperativoPort,
                                            RevocacionPermisoRepositoryPort revocacionRepository,
                                            SolicitudProveedorRepositoryPort solicitudRepository,
                                            ReservasPort reservasPort,
                                            AuditoriaPort auditoriaPort,
                                            Clock clock) {
        this.permisoOperativoPort = permisoOperativoPort;
        this.revocacionRepository = revocacionRepository;
        this.solicitudRepository = solicitudRepository;
        this.reservasPort = reservasPort;
        this.auditoriaPort = auditoriaPort;
        this.clock = clock;
    }

    @Override
    @Transactional
    public RevocacionPermiso revocar(RevocarPermisoComando comando) {
        validarComando(comando.idAdmin(), comando.idUsuario(), comando.rol());
        if (comando.idAdmin().equals(comando.idUsuario())) {
            throw new OperacionAdminInvalidaException("Un administrador no puede revocar sus propios permisos operativos");
        }

        boolean activo = obtenerEstado(comando.idUsuario(), comando.rol());
        if (!activo) {
            if (comando.rol() == RolOperativo.PROVEEDOR
                    && solicitudRepository.existePendienteParaUsuario(comando.idUsuario())) {
                throw new OperacionAdminInvalidaException(
                        "El proveedor aún está pendiente de aprobación; rechace la solicitud en lugar de revocar");
            }
            throw new OperacionAdminInvalidaException("Los permisos operativos de " + comando.rol()
                    + " ya se encuentran revocados para el usuario " + comando.idUsuario());
        }

        LocalDateTime ahora = LocalDateTime.now(clock);

        // Se apagan de inmediato: no puede crear/modificar nada operativo desde este momento
        permisoOperativoPort.actualizarEstado(comando.idUsuario(), comando.rol(), false);

        RevocacionPermiso revocacion = switch (comando.rol()) {
            case CLIENTE -> revocarCliente(comando, ahora);
            case PROVEEDOR -> revocarProveedor(comando, ahora);
        };
        return revocacionRepository.guardar(revocacion);
    }

    private RevocacionPermiso revocarCliente(RevocarPermisoComando comando, LocalDateTime ahora) {
        int canceladas = reservasPort.cancelarReservasActivasDeCliente(
                comando.idUsuario(), RevocacionPermiso.MOTIVO_CANCELACION_RESERVAS);

        RevocacionPermiso revocacion = RevocacionPermiso.inmediata(comando.idUsuario(), RolOperativo.CLIENTE,
                comando.motivo(), comando.idAdmin(), ahora, canceladas);

        auditoriaPort.registrar(RegistroAuditoria.exito(ahora, comando.idAdmin(), comando.idUsuario(),
                AccionAdmin.REVOCACION_COMPLETADA, RolOperativo.CLIENTE,
                "reservasCanceladas=" + canceladas + ", motivo=" + revocacion.getMotivo()));
        return revocacion;
    }

    private RevocacionPermiso revocarProveedor(RevocarPermisoComando comando, LocalDateTime ahora) {
        int activas = reservasPort.contarReservasActivasDeProveedor(comando.idUsuario());

        if (activas == 0) {
            RevocacionPermiso revocacion = RevocacionPermiso.inmediata(comando.idUsuario(), RolOperativo.PROVEEDOR,
                    comando.motivo(), comando.idAdmin(), ahora, 0);
            auditoriaPort.registrar(RegistroAuditoria.exito(ahora, comando.idAdmin(), comando.idUsuario(),
                    AccionAdmin.REVOCACION_COMPLETADA, RolOperativo.PROVEEDOR,
                    "sin reservas activas, motivo=" + revocacion.getMotivo()));
            return revocacion;
        }

        RevocacionPermiso revocacion = RevocacionPermiso.conPlazoDeGracia(
                comando.idUsuario(), comando.motivo(), comando.idAdmin(), ahora);
        auditoriaPort.registrar(RegistroAuditoria.exito(ahora, comando.idAdmin(), comando.idUsuario(),
                AccionAdmin.REVOCACION_INICIADA, RolOperativo.PROVEEDOR,
                "reservasActivas=" + activas + ", debe cancelarlas antes de " + revocacion.getFechaLimite()
                        + ", motivo=" + revocacion.getMotivo()));
        return revocacion;
    }

    @Override
    @Transactional
    public void restaurar(RestaurarPermisoComando comando) {
        validarComando(comando.idAdmin(), comando.idUsuario(), comando.rol());

        if (obtenerEstado(comando.idUsuario(), comando.rol())) {
            throw new OperacionAdminInvalidaException("El usuario " + comando.idUsuario()
                    + " ya tiene permisos operativos de " + comando.rol());
        }
        if (comando.rol() == RolOperativo.PROVEEDOR
                && !solicitudRepository.existeAprobadaParaUsuario(comando.idUsuario())) {
            throw new OperacionAdminInvalidaException(
                    "La cuenta de proveedor no ha sido aprobada; gestione su solicitud de aprobación");
        }

        LocalDateTime ahora = LocalDateTime.now(clock);

        revocacionRepository.buscarPendiente(comando.idUsuario(), comando.rol()).ifPresent(pendiente -> {
            pendiente.anular(ahora);
            revocacionRepository.guardar(pendiente);
        });

        permisoOperativoPort.actualizarEstado(comando.idUsuario(), comando.rol(), true);

        String motivo = comando.motivo() == null || comando.motivo().isBlank() ? "-" : comando.motivo().trim();
        auditoriaPort.registrar(RegistroAuditoria.exito(ahora, comando.idAdmin(), comando.idUsuario(),
                AccionAdmin.PERMISOS_RESTAURADOS, comando.rol(), "motivo=" + motivo));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermisoOperativo> consultarPermisos(Long idUsuario) {
        Map<RolOperativo, Boolean> permisos = permisoOperativoPort.listarPermisos(idUsuario);
        if (permisos.isEmpty()) {
            throw new RecursoAdminNoEncontradoException(
                    "El usuario " + idUsuario + " no existe o no tiene roles operativos");
        }
        return permisos.entrySet().stream()
                .map(e -> new PermisoOperativo(e.getKey(), e.getValue(),
                        revocacionRepository.buscarPendiente(idUsuario, e.getKey()).isPresent()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RevocacionPermiso> listarRevocaciones(EstadoRevocacion estado) {
        return revocacionRepository.listar(estado);
    }

    private boolean obtenerEstado(Long idUsuario, RolOperativo rol) {
        return permisoOperativoPort.estadoPermiso(idUsuario, rol)
                .orElseThrow(() -> new RecursoAdminNoEncontradoException(
                        "El usuario " + idUsuario + " no tiene el rol " + rol));
    }

    private void validarComando(Long idAdmin, Long idUsuario, RolOperativo rol) {
        if (idAdmin == null || idUsuario == null || rol == null) {
            throw new DatosAdminInvalidosException("Administrador, usuario y rol son obligatorios");
        }
    }
}
