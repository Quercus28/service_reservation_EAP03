package com.service_reservation.EAP03.modulos.identidad.domain.model;

public record TokenRespuesta(
    String token,
    String tipo,
    long expiracionSegundos,
    boolean requiere2fa,
    String tokenTemporal
) {
    public TokenRespuesta(String token, String tipo, long expiracionSegundos) {
        this(token, tipo, expiracionSegundos, false, null);
    }

    public static TokenRespuesta exitoso(String token, String tipo, long expiracionSegundos) {
        return new TokenRespuesta(token, tipo, expiracionSegundos, false, null);
    }

    public static TokenRespuesta requiere2fa(String tokenTemporal) {
        return new TokenRespuesta(null, null, 0, true, tokenTemporal);
    }
}
