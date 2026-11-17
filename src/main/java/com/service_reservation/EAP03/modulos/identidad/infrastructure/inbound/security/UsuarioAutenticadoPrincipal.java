package com.service_reservation.EAP03.modulos.identidad.infrastructure.inbound.security;

import java.util.Collections;
import java.util.List;

/**
 * Principal personalizado almacenado en el SecurityContext.
 * Contiene el id del usuario, su email y la lista de nombres de roles
 * sin prefijo ROLE_; el prefijo es responsabilidad de Spring Security.
 */
public class UsuarioAutenticadoPrincipal {

    private final Long idUsuario;
    private final String email;
    private final List<String> roles;
    // true solo cuando la petición llegó con el token de configuración de 2FA (sin roles).
    private final boolean configuracion2fa;

    public UsuarioAutenticadoPrincipal(Long idUsuario, String email, List<String> roles) {
        this(idUsuario, email, roles, false);
    }

    private UsuarioAutenticadoPrincipal(Long idUsuario, String email, List<String> roles, boolean configuracion2fa) {
        this.idUsuario = idUsuario;
        this.email = email;
        this.roles = roles != null ? List.copyOf(roles) : Collections.emptyList();
        this.configuracion2fa = configuracion2fa;
    }

    // Administrador sin 2FA que está en su primer ingreso: no tiene roles, solo puede configurar el 2FA.
    public static UsuarioAutenticadoPrincipal enConfiguracion2fa(Long idUsuario, String email) {
        return new UsuarioAutenticadoPrincipal(idUsuario, email, List.of(), true);
    }

    public boolean isConfiguracion2fa() {
        return configuracion2fa;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public String getEmail() {
        return email;
    }

    public List<String> getRoles() {
        return roles;
    }

    @Override
    public String toString() {
        return "UsuarioAutenticadoPrincipal{idUsuario=" + idUsuario + ", email='" + email + "', roles=" + roles + "}";
    }
}