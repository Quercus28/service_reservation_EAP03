package com.service_reservation.EAP03.modulos.admin.application;

import com.service_reservation.EAP03.modulos.admin.domain.model.EstadoRevocacion;
import com.service_reservation.EAP03.modulos.admin.domain.model.RevocacionPermiso;
import com.service_reservation.EAP03.modulos.admin.domain.ports.in.ProcesarRevocacionesPendientesUseCase;
import com.service_reservation.EAP03.modulos.admin.domain.ports.out.RevocacionPermisoRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProcesarRevocacionesPendientesService implements ProcesarRevocacionesPendientesUseCase {

    private static final Logger log = LoggerFactory.getLogger(ProcesarRevocacionesPendientesService.class);

    private final RevocacionPermisoRepositoryPort revocacionRepository;
    private final CierreRevocacionPendiente cierreRevocacion;
    private final Clock clock;

    public ProcesarRevocacionesPendientesService(RevocacionPermisoRepositoryPort revocacionRepository,
                                                 CierreRevocacionPendiente cierreRevocacion,
                                                 Clock clock) {
        this.revocacionRepository = revocacionRepository;
        this.cierreRevocacion = cierreRevocacion;
        this.clock = clock;
    }

    @Override
    public int procesar() {
        List<RevocacionPermiso> pendientes = revocacionRepository.listar(EstadoRevocacion.PENDIENTE_CANCELACION);
        LocalDateTime ahora = LocalDateTime.now(clock);
        int completadas = 0;

        for (RevocacionPermiso revocacion : pendientes) {
            try {
                if (cierreRevocacion.procesar(revocacion, ahora)) {
                    completadas++;
                }
            } catch (RuntimeException ex) {
                log.error("[ADMIN-JOB] Error procesando revocación id={} usuario={}: {}",
                        revocacion.getId(), revocacion.getIdUsuario(), ex.getMessage(), ex);
            }
        }

        if (!pendientes.isEmpty()) {
            log.info("[ADMIN-JOB] timestamp={} revocacionesPendientes={} completadas={}",
                    ahora, pendientes.size(), completadas);
        }
        return completadas;
    }
}
