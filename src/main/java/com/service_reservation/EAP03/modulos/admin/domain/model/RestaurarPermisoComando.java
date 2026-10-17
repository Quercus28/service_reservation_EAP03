package com.service_reservation.EAP03.modulos.admin.domain.model;

/** El motivo es opcional al restaurar. */
public record RestaurarPermisoComando(Long idAdmin, Long idUsuario, RolOperativo rol, String motivo) {
}
