package com.service_reservation.EAP03.modulos.servicios_catalogo.infrastructure.config;

import com.service_reservation.EAP03.modulos.identidad.infrastructure.inbound.security.UsuarioAutenticadoPrincipal;
import com.service_reservation.EAP03.modulos.servicios_catalogo.domain.model.Servicio;
import com.service_reservation.EAP03.modulos.servicios_catalogo.domain.repository.ServicioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Componente de seguridad para expresiones SpEL en @PreAuthorize.
 * Uso: @PreAuthorize("hasRole(''ADMIN'') or (hasRole(''PROVEEDOR'') and @servicioSecurity.esPropietario(authentication, #id))")
 */
@Component("servicioSecurity")
public class ServicioSecurity {

    private final ServicioRepository servicioRepository;

    public ServicioSecurity(ServicioRepository servicioRepository) {
        this.servicioRepository = servicioRepository;
    }

    /**
     * Verifica si el proveedor autenticado es propietario del servicio con el id dado.
     *
     * @param authentication autenticacion del contexto de seguridad
     * @param id             id del servicio a verificar
     * @return true si el principal autenticado es el proveedor propietario
     */
    public boolean esPropietario(Authentication authentication, Integer id) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UsuarioAutenticadoPrincipal usuario)) {
            return false;
        }

        if (id == null) {
            return false;
        }

        Optional<Servicio> servicioOpt = servicioRepository.buscarPorId(id);
        return servicioOpt
                .map(s -> s.getIdProveedor() != null
                        && s.getIdProveedor().longValue() == usuario.getIdUsuario())
                .orElse(false);
    }
}