package com.service_reservation.EAP03.modulos.admin.infrastructure.config;

import com.service_reservation.EAP03.modulos.admin.domain.model.RolOperativo;
import com.service_reservation.EAP03.modulos.admin.domain.ports.in.VerificarPermisoOperativoUseCase;
import com.service_reservation.EAP03.modulos.identidad.infrastructure.inbound.security.UsuarioAutenticadoPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("permisoOperativo")
public class PermisoOperativoSecurity {

    private final VerificarPermisoOperativoUseCase verificarPermisoOperativoUseCase;

    public PermisoOperativoSecurity(VerificarPermisoOperativoUseCase verificarPermisoOperativoUseCase) {
        this.verificarPermisoOperativoUseCase = verificarPermisoOperativoUseCase;
    }

    public boolean activo(Authentication authentication, String rolNombre) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        if (!(authentication.getPrincipal() instanceof UsuarioAutenticadoPrincipal principal)) {
            return false;
        }
        
        RolOperativo rol;
        try {
            rol = RolOperativo.valueOf(rolNombre);
        } catch (IllegalArgumentException e) {
            return false;
        }

        return verificarPermisoOperativoUseCase.tienePermisoOperativo(principal.getIdUsuario(), rol);
    }
}
