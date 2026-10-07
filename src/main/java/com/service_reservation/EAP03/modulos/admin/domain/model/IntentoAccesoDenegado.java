package com.service_reservation.EAP03.modulos.admin.domain.model;

import java.time.LocalDateTime;

/**
 * Intento de acceder a un endpoint administrativo sin ROLE_ADMIN.
 *
 * @param idUsuario id del usuario autenticado (null si no se pudo resolver)
 * @param metodo    método HTTP
 * @param endpoint  ruta solicitada
 */
public record IntentoAccesoDenegado(
        LocalDateTime fecha,
        Long idUsuario,
        String email,
        String metodo,
        String endpoint,
        String ip
) {
}
