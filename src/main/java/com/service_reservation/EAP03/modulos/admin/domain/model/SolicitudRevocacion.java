package com.service_reservation.EAP03.modulos.admin.domain.model;

import java.time.LocalDateTime;

/** Lo que pidió el administrador al revocar: sobre qué usuario y rol, por qué y cuándo. */
public record SolicitudRevocacion(Long idUsuario, RolOperativo rol, String motivo, Long idAdmin,
                                  LocalDateTime fechaSolicitud) {
}
