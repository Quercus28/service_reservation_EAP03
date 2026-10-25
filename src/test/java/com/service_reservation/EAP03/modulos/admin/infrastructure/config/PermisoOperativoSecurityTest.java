package com.service_reservation.EAP03.modulos.admin.infrastructure.config;

import com.service_reservation.EAP03.modulos.admin.domain.model.RolOperativo;
import com.service_reservation.EAP03.modulos.admin.domain.ports.in.VerificarPermisoOperativoUseCase;
import com.service_reservation.EAP03.modulos.identidad.infrastructure.inbound.security.UsuarioAutenticadoPrincipal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PermisoOperativoSecurityTest {

    private static final UsuarioAutenticadoPrincipal PRINCIPAL = new UsuarioAutenticadoPrincipal(42L, "prov@test.com", List.of("PROVEEDOR"));

    @Mock private VerificarPermisoOperativoUseCase verificarPermisoOperativoUseCase;

    @InjectMocks private PermisoOperativoSecurity security;

    private static Authentication authenticated(Object principal) {
        return new UsernamePasswordAuthenticationToken(principal, null, List.of());
    }

    @Test
    @DisplayName("Está activo cuando el usuario tiene el permiso operativo del rol")
    void activeWhenUserHasPermission() {
        when(verificarPermisoOperativoUseCase.tienePermisoOperativo(42L, RolOperativo.PROVEEDOR)).thenReturn(true);

        assertTrue(security.activo(authenticated(PRINCIPAL), "PROVEEDOR"));
    }

    @Test
    @DisplayName("No está activo cuando el permiso está revocado")
    void inactiveWhenPermissionRevoked() {
        when(verificarPermisoOperativoUseCase.tienePermisoOperativo(42L, RolOperativo.CLIENTE)).thenReturn(false);

        assertFalse(security.activo(authenticated(PRINCIPAL), "CLIENTE"));
    }

    @Test
    @DisplayName("No está activo sin una autenticación válida del sistema")
    void inactiveWithoutValidAuthentication() {
        assertFalse(security.activo(null, "PROVEEDOR"));
        assertFalse(security.activo(new UsernamePasswordAuthenticationToken(PRINCIPAL, null), "PROVEEDOR"));
        assertFalse(security.activo(authenticated("anonimo"), "PROVEEDOR"));
        verifyNoInteractions(verificarPermisoOperativoUseCase);
    }

    @Test
    @DisplayName("No está activo para un rol que no es operativo")
    void inactiveForUnknownRole() {
        assertFalse(security.activo(authenticated(PRINCIPAL), "ADMIN"));
        verifyNoInteractions(verificarPermisoOperativoUseCase);
    }
}
