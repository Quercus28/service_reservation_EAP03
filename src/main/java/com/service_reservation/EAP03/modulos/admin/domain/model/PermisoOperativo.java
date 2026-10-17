package com.service_reservation.EAP03.modulos.admin.domain.model;

/**
 * Estado del permiso operativo de un usuario sobre uno de sus roles.
 *
 * @param revocacionPendiente true si hay una revocación en plazo de gracia (solo proveedores)
 */
public record PermisoOperativo(RolOperativo rol, boolean activo, boolean revocacionPendiente) {
}
