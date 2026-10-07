package com.service_reservation.EAP03.modulos.identidad.domain.events;

public record UsuarioRegistradoEvent(
        Long idUsuario,
        String rolNormalizado
) {
}
