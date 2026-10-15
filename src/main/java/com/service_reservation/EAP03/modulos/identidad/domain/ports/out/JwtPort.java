package com.service_reservation.EAP03.modulos.identidad.domain.ports.out;

import com.service_reservation.EAP03.modulos.identidad.domain.model.Usuario;

import java.util.List;

public interface JwtPort {

    // --- Generación ---
    String generarToken(Usuario usuario);
    long getExpiracionSegundos();

    // --- Validación y extracción ---
    boolean validarToken(String token);
    String extraerEmail(String token);
    Long extraerUsuarioId(String token);
    List<String> extraerRoles(String token);

    // --- Flujo 2FA ---
    String generarTokenTemporal2fa(Usuario usuario);
    boolean esTokenTemporal2faValido(String token);
}
