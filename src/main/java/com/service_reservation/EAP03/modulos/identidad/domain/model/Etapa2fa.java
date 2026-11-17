package com.service_reservation.EAP03.modulos.identidad.domain.model;

// Qué le falta al usuario para terminar de iniciar sesión cuando el login responde requiere2fa = true.
public enum Etapa2fa {
    // Ya tiene 2FA activo: debe enviar el código en /2fa/verificar-login.
    VERIFICAR,
    // Es administrador y todavía no tiene 2FA: debe pedir el QR en /2fa/qr y activarlo en /2fa/activar.
    CONFIGURAR
}
