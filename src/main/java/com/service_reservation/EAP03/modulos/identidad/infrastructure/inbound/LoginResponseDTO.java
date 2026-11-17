package com.service_reservation.EAP03.modulos.identidad.infrastructure.inbound;

import com.service_reservation.EAP03.modulos.identidad.domain.model.Etapa2fa;
import com.service_reservation.EAP03.modulos.identidad.domain.model.TokenRespuesta;

// etapa2fa: VERIFICAR (pedir el código), CONFIGURAR (mostrar el QR) o null en un login normal.
public record LoginResponseDTO(
    String token,
    String tipo,
    long expiracionSegundos,
    boolean requiere2fa,
    String tokenTemporal,
    Etapa2fa etapa2fa
) {
    public LoginResponseDTO(String token, String tipo, long expiracionSegundos) {
        this(token, tipo, expiracionSegundos, false, null, null);
    }

    public LoginResponseDTO(String token, String tipo, long expiracionSegundos, boolean requiere2fa, String tokenTemporal) {
        this(token, tipo, expiracionSegundos, requiere2fa, tokenTemporal, null);
    }

    public static LoginResponseDTO desde(TokenRespuesta respuesta) {
        return new LoginResponseDTO(respuesta.token(), respuesta.tipo(), respuesta.expiracionSegundos(),
                respuesta.requiere2fa(), respuesta.tokenTemporal(), respuesta.etapa2fa());
    }
}
