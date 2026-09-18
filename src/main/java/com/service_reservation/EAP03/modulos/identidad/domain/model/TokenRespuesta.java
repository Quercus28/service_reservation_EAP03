package com.service_reservation.EAP03.modulos.identidad.domain.model;

public record TokenRespuesta(
    String token,
    String tipo,
    long expiracionSegundos
) {}
