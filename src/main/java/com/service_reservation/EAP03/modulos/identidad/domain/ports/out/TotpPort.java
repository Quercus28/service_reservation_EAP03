package com.service_reservation.EAP03.modulos.identidad.domain.ports.out;

public interface TotpPort {
    String generarSecreto();
    byte[] generarQrPng(String email, String secret);
    boolean validarCodigo(String secret, String codigo);
}
