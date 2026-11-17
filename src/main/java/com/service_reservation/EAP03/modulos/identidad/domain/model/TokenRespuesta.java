package com.service_reservation.EAP03.modulos.identidad.domain.model;

// etapa2fa va vacío (null) cuando el login entrega el JWT definitivo.
public record TokenRespuesta(
    String token,
    String tipo,
    long expiracionSegundos,
    boolean requiere2fa,
    String tokenTemporal,
    Etapa2fa etapa2fa
) {
    public TokenRespuesta(String token, String tipo, long expiracionSegundos) {
        this(token, tipo, expiracionSegundos, false, null, null);
    }

    public TokenRespuesta(String token, String tipo, long expiracionSegundos, boolean requiere2fa, String tokenTemporal) {
        this(token, tipo, expiracionSegundos, requiere2fa, tokenTemporal, null);
    }

    public static TokenRespuesta exitoso(String token, String tipo, long expiracionSegundos) {
        return new TokenRespuesta(token, tipo, expiracionSegundos, false, null, null);
    }

    public static TokenRespuesta requiere2fa(String tokenTemporal) {
        return new TokenRespuesta(null, null, 0, true, tokenTemporal, Etapa2fa.VERIFICAR);
    }

    // Administrador sin 2FA: no recibe JWT definitivo hasta configurar y activar el 2FA.
    public static TokenRespuesta requiereConfigurar2fa(String tokenConfiguracion) {
        return new TokenRespuesta(null, null, 0, true, tokenConfiguracion, Etapa2fa.CONFIGURAR);
    }
}
