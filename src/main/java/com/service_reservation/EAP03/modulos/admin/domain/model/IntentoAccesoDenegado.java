package com.service_reservation.EAP03.modulos.admin.domain.model;

import java.time.LocalDateTime;

/**
 * Intento de acceder a un endpoint administrativo sin ROLE_ADMIN.
 *
 * No lleva correo ni IP: la auditoría no guarda datos personales.
 *
 * @param idUsuario id del usuario autenticado (null si no se pudo resolver)
 * @param metodo    método HTTP
 * @param endpoint  ruta solicitada
 */
public record IntentoAccesoDenegado(
        LocalDateTime fecha,
        Long idUsuario,
        String metodo,
        String endpoint
) {
}
