package com.service_reservation.EAP03.modulos.admin.application;

import com.service_reservation.EAP03.modulos.admin.domain.exception.OperacionAdminInvalidaException;
import com.service_reservation.EAP03.modulos.admin.domain.exception.RecursoAdminNoEncontradoException;
import com.service_reservation.EAP03.modulos.admin.domain.model.AccionAdmin;
import com.service_reservation.EAP03.modulos.admin.domain.model.EstadoSolicitud;
import com.service_reservation.EAP03.modulos.admin.domain.model.RegistroAuditoria;
import com.service_reservation.EAP03.modulos.admin.domain.model.RolOperativo;
import com.service_reservation.EAP03.modulos.admin.domain.model.SolicitudAprobacionProveedor;
import com.service_reservation.EAP03.modulos.admin.domain.ports.in.GestionarSolicitudesProveedorUseCase;
import com.service_reservation.EAP03.modulos.admin.domain.ports.out.AuditoriaPort;
import com.service_reservation.EAP03.modulos.admin.domain.ports.out.PermisoOperativoPort;
import com.service_reservation.EAP03.modulos.admin.domain.ports.out.SolicitudProveedorRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class GestionSolicitudesProveedorService implements GestionarSolicitudesProveedorUseCase {

    private final SolicitudProveedorRepositoryPort solicitudRepository;
    private final PermisoOperativoPort permisoOperativoPort;
    private final AuditoriaPort auditoriaPort;
    private final Clock clock;

    public GestionSolicitudesProveedorService(SolicitudProveedorRepositoryPort solicitudRepository,
                                              PermisoOperativoPort permisoOperativoPort,
                                              AuditoriaPort auditoriaPort,
                                              Clock clock) {
        this.solicitudRepository = solicitudRepository;
        this.permisoOperativoPort = permisoOperativoPort;
        this.auditoriaPort = auditoriaPort;
        this.clock = clock;
    }

    @Override
    @Transactional
    public SolicitudAprobacionProveedor registrarSolicitud(Long idUsuario) {
        if (solicitudRepository.existePendienteParaUsuario(idUsuario)) {
            return solicitudRepository.buscarUltimaPorUsuario(idUsuario).orElseThrow();
        }
        return crearSolicitud(idUsuario, AccionAdmin.SOLICITUD_PROVEEDOR_CREADA);
    }

    @Override
    @Transactional
    public SolicitudAprobacionProveedor reenviarSolicitud(Long idUsuario) {
        validarTieneRolProveedor(idUsuario);
        SolicitudAprobacionProveedor ultima = solicitudRepository.buscarUltimaPorUsuario(idUsuario).orElse(null);
        if (ultima != null && ultima.getEstado() != EstadoSolicitud.RECHAZADA) {
            throw new OperacionAdminInvalidaException(
                    "Solo se puede volver a solicitar aprobación tras un rechazo. Estado actual: " + ultima.getEstado());
        }
        return crearSolicitud(idUsuario, AccionAdmin.SOLICITUD_PROVEEDOR_REENVIADA);
    }

    @Override
    @Transactional(readOnly = true)
    public SolicitudAprobacionProveedor consultarUltimaSolicitud(Long idUsuario) {
        return solicitudRepository.buscarUltimaPorUsuario(idUsuario)
                .orElseThrow(() -> new RecursoAdminNoEncontradoException(
                        "El usuario no tiene solicitudes de aprobación"));
    }

    @Override
    @Transactional
    public SolicitudAprobacionProveedor aprobar(Long idAdmin, Long idSolicitud) {
        SolicitudAprobacionProveedor solicitud = buscar(idSolicitud);
        validarTieneRolProveedor(solicitud.getIdUsuario());

        LocalDateTime ahora = LocalDateTime.now(clock);
        solicitud.aprobar(idAdmin, ahora);
        SolicitudAprobacionProveedor guardada = solicitudRepository.guardar(solicitud);

        // Al aprobar se encienden los permisos operativos de ROLE_PROVEEDOR
        permisoOperativoPort.actualizarEstado(solicitud.getIdUsuario(), RolOperativo.PROVEEDOR, true);

        auditoriaPort.registrar(RegistroAuditoria.exito(ahora, idAdmin, solicitud.getIdUsuario(),
                AccionAdmin.SOLICITUD_PROVEEDOR_APROBADA, RolOperativo.PROVEEDOR,
                "solicitud=" + idSolicitud));
        return guardada;
    }

    @Override
    @Transactional
    public SolicitudAprobacionProveedor rechazar(Long idAdmin, Long idSolicitud, String motivo) {
        SolicitudAprobacionProveedor solicitud = buscar(idSolicitud);

        LocalDateTime ahora = LocalDateTime.now(clock);
        solicitud.rechazar(idAdmin, motivo, ahora);
        SolicitudAprobacionProveedor guardada = solicitudRepository.guardar(solicitud);

        auditoriaPort.registrar(RegistroAuditoria.exito(ahora, idAdmin, solicitud.getIdUsuario(),
                AccionAdmin.SOLICITUD_PROVEEDOR_RECHAZADA, RolOperativo.PROVEEDOR,
                "solicitud=" + idSolicitud + ", motivo=" + guardada.getMotivoRechazo()));
        return guardada;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SolicitudAprobacionProveedor> listar(EstadoSolicitud estado) {
        return solicitudRepository.listar(estado);
    }

    private SolicitudAprobacionProveedor crearSolicitud(Long idUsuario, AccionAdmin accion) {
        LocalDateTime ahora = LocalDateTime.now(clock);
        SolicitudAprobacionProveedor guardada =
                solicitudRepository.guardar(SolicitudAprobacionProveedor.nueva(idUsuario, ahora));
        auditoriaPort.registrar(RegistroAuditoria.exito(ahora, idUsuario, idUsuario,
                accion, RolOperativo.PROVEEDOR, "solicitud=" + guardada.getId()));
        return guardada;
    }

    private SolicitudAprobacionProveedor buscar(Long idSolicitud) {
        return solicitudRepository.buscarPorId(idSolicitud)
                .orElseThrow(() -> new RecursoAdminNoEncontradoException(
                        "Solicitud de aprobación no encontrada: " + idSolicitud));
    }

    private void validarTieneRolProveedor(Long idUsuario) {
        if (permisoOperativoPort.estadoPermiso(idUsuario, RolOperativo.PROVEEDOR).isEmpty()) {
            throw new RecursoAdminNoEncontradoException(
                    "El usuario " + idUsuario + " no tiene el rol PROVEEDOR");
        }
    }
}
