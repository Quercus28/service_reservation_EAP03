package com.service_reservation.EAP03.modulos.admin.domain.ports.out;

import com.service_reservation.EAP03.modulos.admin.domain.model.EstadoSolicitud;
import com.service_reservation.EAP03.modulos.admin.domain.model.SolicitudAprobacionProveedor;

import java.util.List;
import java.util.Optional;

public interface SolicitudProveedorRepositoryPort {

    SolicitudAprobacionProveedor guardar(SolicitudAprobacionProveedor solicitud);

    Optional<SolicitudAprobacionProveedor> buscarPorId(Long id);

    Optional<SolicitudAprobacionProveedor> buscarUltimaPorUsuario(Long idUsuario);

    boolean existePendienteParaUsuario(Long idUsuario);

    boolean existeAprobadaParaUsuario(Long idUsuario);

    /** @param estado null = todas */
    List<SolicitudAprobacionProveedor> listar(EstadoSolicitud estado);
}
