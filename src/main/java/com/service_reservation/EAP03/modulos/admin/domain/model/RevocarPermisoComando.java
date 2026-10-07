package com.service_reservation.EAP03.modulos.admin.domain.model;

public record RevocarPermisoComando(Long idAdmin, Long idUsuario, RolOperativo rol, String motivo) {
}
