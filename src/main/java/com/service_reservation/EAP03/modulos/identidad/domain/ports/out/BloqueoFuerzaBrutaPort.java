package com.service_reservation.EAP03.modulos.identidad.domain.ports.out;

public interface BloqueoFuerzaBrutaPort {
    boolean estaBloqueado(String identificador);
    void registrarIntentoFallido(String identificador);
    void resetearIntentos(String identificador);
}
