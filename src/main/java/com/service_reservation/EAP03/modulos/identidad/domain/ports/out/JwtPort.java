package com.service_reservation.EAP03.modulos.identidad.domain.ports.out;

import com.service_reservation.EAP03.modulos.identidad.domain.model.Usuario;

public interface JwtPort {
    String generarToken(Usuario usuario);
    long getExpiracionSegundos();
}
