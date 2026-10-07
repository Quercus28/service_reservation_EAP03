package com.service_reservation.EAP03.modulos.admin.domain.ports.out;

import com.service_reservation.EAP03.modulos.admin.domain.model.RolOperativo;

import java.util.Map;
import java.util.Optional;

/**
 * Acceso a la columna USUARIO_ROL.activo (permiso operativo de un rol).
 */
public interface PermisoOperativoPort {

    /**
     * @return empty si el usuario no tiene el rol; de lo contrario, el valor de "activo".
     */
    Optional<Boolean> estadoPermiso(Long idUsuario, RolOperativo rol);

    void actualizarEstado(Long idUsuario, RolOperativo rol, boolean activo);

    /** Roles operativos del usuario con su estado (excluye ROLE_ADMIN). */
    Map<RolOperativo, Boolean> listarPermisos(Long idUsuario);
}
