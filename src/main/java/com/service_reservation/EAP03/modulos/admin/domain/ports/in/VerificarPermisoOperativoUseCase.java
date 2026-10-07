package com.service_reservation.EAP03.modulos.admin.domain.ports.in;

import com.service_reservation.EAP03.modulos.admin.domain.model.RolOperativo;

/**
 * Consultado por las expresiones de seguridad (@PreAuthorize) de todos los módulos.
 */
public interface VerificarPermisoOperativoUseCase {

    /** true si el usuario tiene el rol y su permiso operativo está activo. */
    boolean tienePermisoOperativo(Long idUsuario, RolOperativo rol);

    /**
     * true si el usuario puede cancelar sus reservas existentes: tiene permiso operativo
     * o está dentro del plazo de gracia de una revocación.
     */
    boolean puedeCancelarReservas(Long idUsuario, RolOperativo rol);
}
