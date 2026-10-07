package com.service_reservation.EAP03.modulos.admin.domain.ports.in;

import com.service_reservation.EAP03.modulos.admin.domain.model.EstadoSolicitud;
import com.service_reservation.EAP03.modulos.admin.domain.model.SolicitudAprobacionProveedor;

import java.util.List;

public interface GestionarSolicitudesProveedorUseCase {

    /** Invocado al registrarse un proveedor. Idempotente si ya existe una solicitud pendiente. */
    SolicitudAprobacionProveedor registrarSolicitud(Long idUsuario);

    /** El proveedor vuelve a solicitar aprobación tras un rechazo. */
    SolicitudAprobacionProveedor reenviarSolicitud(Long idUsuario);

    SolicitudAprobacionProveedor consultarUltimaSolicitud(Long idUsuario);

    SolicitudAprobacionProveedor aprobar(Long idAdmin, Long idSolicitud);

    SolicitudAprobacionProveedor rechazar(Long idAdmin, Long idSolicitud, String motivo);

    /** @param estado filtro opcional (null = todas) */
    List<SolicitudAprobacionProveedor> listar(EstadoSolicitud estado);
}
