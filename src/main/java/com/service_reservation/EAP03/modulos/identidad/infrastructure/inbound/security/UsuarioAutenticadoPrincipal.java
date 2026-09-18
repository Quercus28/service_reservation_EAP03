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

    public UsuarioAutenticadoPrincipal(Long idUsuario, String email, List<String> roles) {
        this.idUsuario = idUsuario;
        this.email = email;
        this.roles = roles != null ? List.copyOf(roles) : Collections.emptyList();
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