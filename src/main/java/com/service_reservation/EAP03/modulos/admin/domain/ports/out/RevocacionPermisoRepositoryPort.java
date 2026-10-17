package com.service_reservation.EAP03.modulos.admin.domain.ports.out;

import com.service_reservation.EAP03.modulos.admin.domain.model.EstadoRevocacion;
import com.service_reservation.EAP03.modulos.admin.domain.model.RevocacionPermiso;
import com.service_reservation.EAP03.modulos.admin.domain.model.RolOperativo;

import java.util.List;
import java.util.Optional;

public interface RevocacionPermisoRepositoryPort {

    RevocacionPermiso guardar(RevocacionPermiso revocacion);

    Optional<RevocacionPermiso> buscarPendiente(Long idUsuario, RolOperativo rol);

    /** @param estado null = todas */
    List<RevocacionPermiso> listar(EstadoRevocacion estado);
}
