package com.service_reservation.EAP03.modulos.identidad.infrastructure.inbound;

public record LoginResponseDTO(
    String token,
    String tipo,
    long expiracionSegundos
) {}
