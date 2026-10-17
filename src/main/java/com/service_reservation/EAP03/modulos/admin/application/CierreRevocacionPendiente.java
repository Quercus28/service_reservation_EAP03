package com.service_reservation.EAP03.modulos.admin.application;

import com.service_reservation.EAP03.modulos.admin.domain.model.AccionAdmin;
import com.service_reservation.EAP03.modulos.admin.domain.model.RegistroAuditoria;
import com.service_reservation.EAP03.modulos.admin.domain.model.RevocacionPermiso;
import com.service_reservation.EAP03.modulos.admin.domain.ports.out.AuditoriaPort;
import com.service_reservation.EAP03.modulos.admin.domain.ports.out.ReservasPort;
import com.service_reservation.EAP03.modulos.admin.domain.ports.out.RevocacionPermisoRepositoryPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Cierra una revocación pendiente dentro de su propia transacción, de modo que un fallo
 * en una revocación no revierta las demás procesadas en la misma ejecución del job.
 */
@Component
class CierreRevocacionPendiente {

    private final RevocacionPermisoRepositoryPort revocacionRepository;
    private final ReservasPort reservasPort;
    private final AuditoriaPort auditoriaPort;

    CierreRevocacionPendiente(RevocacionPermisoRepositoryPort revocacionRepository,
                              ReservasPort reservasPort,
                              AuditoriaPort auditoriaPort) {
        this.revocacionRepository = revocacionRepository;
        this.reservasPort = reservasPort;
        this.auditoriaPort = auditoriaPort;
    }

    /** @return true si la revocación quedó completada */
    @Transactional
    public boolean procesar(RevocacionPermiso revocacion, LocalDateTime ahora) {
        int activas = reservasPort.contarReservasActivasDeProveedor(revocacion.getIdUsuario());

        if (activas == 0) {
            revocacion.completar(ahora, 0);
            revocacionRepository.guardar(revocacion);
            auditoriaPort.registrar(RegistroAuditoria.exito(ahora, null, revocacion.getIdUsuario(),
                    AccionAdmin.REVOCACION_COMPLETADA, revocacion.getRol(),
                    "revocacion=" + revocacion.getId() + ", el proveedor no tiene reservas activas"));
            return true;
        }

        if (revocacion.plazoVencido(ahora)) {
            int canceladas = reservasPort.cancelarReservasActivasDeProveedor(
                    revocacion.getIdUsuario(), RevocacionPermiso.MOTIVO_CANCELACION_RESERVAS);
            revocacion.completar(ahora, canceladas);
            revocacionRepository.guardar(revocacion);
            auditoriaPort.registrar(RegistroAuditoria.exito(ahora, null, revocacion.getIdUsuario(),
                    AccionAdmin.REVOCACION_FORZADA, revocacion.getRol(),
                    "revocacion=" + revocacion.getId() + ", plazo vencido el " + revocacion.getFechaLimite()
                            + ", reservasCanceladasForzosamente=" + canceladas));
            return true;
        }

        return false;
    }
}
