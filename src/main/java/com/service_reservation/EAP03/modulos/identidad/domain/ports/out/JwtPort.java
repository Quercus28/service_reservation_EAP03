package com.service_reservation.EAP03.modulos.identidad.domain.ports.out;

import com.service_reservation.EAP03.modulos.identidad.domain.model.Usuario;

import java.util.List;

public interface JwtPort {

    // Etapas de los tokens temporales. Un token con etapa nunca es una sesión normal.
    String ETAPA_VERIFICAR_2FA = "2FA_PENDING";
    String ETAPA_CONFIGURAR_2FA = "2FA_SETUP";

    // --- Generación ---
    String generarToken(Usuario usuario);
    long getExpiracionSegundos();

    // --- Validación y extracción ---
    boolean validarToken(String token);
    String extraerEmail(String token);
    Long extraerUsuarioId(String token);
    List<String> extraerRoles(String token);
    // Etapa del token temporal, o null si es un JWT definitivo (o no se puede leer).
    String extraerEtapa(String token);

    // --- Flujo 2FA ---
    String generarTokenTemporal2fa(Usuario usuario);
    boolean esTokenTemporal2faValido(String token);
    // Token para que un administrador sin 2FA lo configure: solo sirve en /2fa/qr y /2fa/activar.
    String generarTokenConfiguracion2fa(Usuario usuario);
}
