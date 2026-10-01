package com.service_reservation.EAP03.modulos.identidad.domain.model;

public record ResultadoConfiguracion2fa(
    byte[] imagenQr,
    String secreto
) {}
