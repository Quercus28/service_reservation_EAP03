package com.service_reservation.EAP03.modulos.identidad.domain.ports.in;

import com.service_reservation.EAP03.modulos.identidad.domain.model.TokenRespuesta;

public interface Activar2faUseCase {
    void ejecutar(Long idUsuario, String codigoTotp);

    // Primer ingreso de un administrador sin 2FA (con el token de configuración): activa el 2FA
    // y entrega el JWT definitivo. Se rechaza si el 2FA ya estaba activo.
    TokenRespuesta ejecutarEnConfiguracionInicial(Long idUsuario, String codigoTotp);
}
