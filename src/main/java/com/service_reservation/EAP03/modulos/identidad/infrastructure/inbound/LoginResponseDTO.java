package com.service_reservation.EAP03.modulos.identidad.infrastructure.inbound;

public record LoginResponseDTO(
    String token,
    String tipo,
    long expiracionSegundos,
    boolean requiere2fa,
    String tokenTemporal,
    String mensaje
) {
    public LoginResponseDTO(String token, String tipo, long expiracionSegundos) {
        this(token, tipo, expiracionSegundos, false, null, null);
    }

    public LoginResponseDTO(String token, String tipo, long expiracionSegundos, boolean requiere2fa, String tokenTemporal) {
        this(token, tipo, expiracionSegundos, requiere2fa, tokenTemporal, null);
    }
}
