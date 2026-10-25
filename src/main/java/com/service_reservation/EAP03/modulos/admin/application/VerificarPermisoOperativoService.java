package com.service_reservation.EAP03.modulos.admin.application;

import com.service_reservation.EAP03.modulos.admin.domain.model.RolOperativo;
import com.service_reservation.EAP03.modulos.admin.domain.ports.in.VerificarPermisoOperativoUseCase;
import com.service_reservation.EAP03.modulos.admin.domain.ports.out.PermisoOperativoPort;
import com.service_reservation.EAP03.modulos.admin.domain.ports.out.RevocacionPermisoRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VerificarPermisoOperativoService implements VerificarPermisoOperativoUseCase {

    private final PermisoOperativoPort permisoOperativoPort;
    private final RevocacionPermisoRepositoryPort revocacionRepository;

    public VerificarPermisoOperativoService(PermisoOperativoPort permisoOperativoPort,
                                            RevocacionPermisoRepositoryPort revocacionRepository) {
        this.permisoOperativoPort = permisoOperativoPort;
        this.revocacionRepository = revocacionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean tienePermisoOperativo(Long idUsuario, RolOperativo rol) {
        return idUsuario != null && rol != null && permisoActivo(idUsuario, rol);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean puedeCancelarReservas(Long idUsuario, RolOperativo rol) {
        if (idUsuario == null || rol == null) {
            return false;
        }
        return permisoActivo(idUsuario, rol)
                || revocacionRepository.buscarPendiente(idUsuario, rol).isPresent();
    }

    private boolean permisoActivo(Long idUsuario, RolOperativo rol) {
        return permisoOperativoPort.estadoPermiso(idUsuario, rol).orElse(false);
    }
}
