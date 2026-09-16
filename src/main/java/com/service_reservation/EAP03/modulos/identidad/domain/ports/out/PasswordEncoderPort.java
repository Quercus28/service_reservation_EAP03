package com.service_reservation.EAP03.modulos.identidad.domain.ports.out;

public interface PasswordEncoderPort {
    String codificar(String passwordPlana);
}