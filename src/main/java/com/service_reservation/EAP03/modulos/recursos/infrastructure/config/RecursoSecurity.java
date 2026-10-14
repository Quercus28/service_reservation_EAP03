package com.service_reservation.EAP03.modulos.recursos.infrastructure.config;

import com.service_reservation.EAP03.modulos.identidad.domain.ports.in.ConsultarProveedorUseCase;
import com.service_reservation.EAP03.modulos.identidad.infrastructure.inbound.security.UsuarioAutenticadoPrincipal;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.out.RecursoRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("recursoSecurity")
public class RecursoSecurity {

    private static final Logger log = LoggerFactory.getLogger(RecursoSecurity.class);

    private final ConsultarProveedorUseCase consultarProveedorUseCase;
    private final RecursoRepositoryPort recursoRepositoryPort;

    public RecursoSecurity(ConsultarProveedorUseCase consultarProveedorUseCase,
                           RecursoRepositoryPort recursoRepositoryPort) {
        this.consultarProveedorUseCase = consultarProveedorUseCase;
        this.recursoRepositoryPort = recursoRepositoryPort;
    }

    public Optional<Integer> idProveedorDe(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        if (!(authentication.getPrincipal() instanceof UsuarioAutenticadoPrincipal principal)) {
            return Optional.empty();
        }
        return consultarProveedorUseCase.buscarIdProveedorPorUsuario(principal.getIdUsuario());
    }

    public boolean esPropietario(Authentication authentication, Integer idRecurso) {
        boolean propietario = idRecurso != null && idProveedorDe(authentication)
                .flatMap(idProveedor -> recursoRepositoryPort.buscarPorId(idRecurso)
                        .map(recurso -> idProveedor.equals(recurso.getIdProveedor())))
                .orElse(false);

        if (!propietario) {
            log.warn("Acceso denegado a recurso ajeno o inexistente: idUsuario={}, idRecurso={}",
                    idUsuarioDe(authentication), idRecurso);
        }
        return propietario;
    }

    private static Long idUsuarioDe(Authentication authentication) {
        if (authentication != null
                && authentication.getPrincipal() instanceof UsuarioAutenticadoPrincipal principal) {
            return principal.getIdUsuario();
        }
        return null;
    }
}
