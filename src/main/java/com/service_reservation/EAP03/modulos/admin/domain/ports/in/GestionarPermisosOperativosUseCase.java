package com.service_reservation.EAP03.modulos.admin.domain.ports.in;

import com.service_reservation.EAP03.modulos.admin.domain.model.EstadoRevocacion;
import com.service_reservation.EAP03.modulos.admin.domain.model.PermisoOperativo;
import com.service_reservation.EAP03.modulos.admin.domain.model.RestaurarPermisoComando;
import com.service_reservation.EAP03.modulos.admin.domain.model.RevocacionPermiso;
import com.service_reservation.EAP03.modulos.admin.domain.model.RevocarPermisoComando;

import java.util.List;

public interface GestionarPermisosOperativosUseCase {

    RevocacionPermiso revocar(RevocarPermisoComando comando);

    void restaurar(RestaurarPermisoComando comando);

    List<PermisoOperativo> consultarPermisos(Long idUsuario);

    /** @param estado filtro opcional (null = todas) */
    List<RevocacionPermiso> listarRevocaciones(EstadoRevocacion estado);
}
