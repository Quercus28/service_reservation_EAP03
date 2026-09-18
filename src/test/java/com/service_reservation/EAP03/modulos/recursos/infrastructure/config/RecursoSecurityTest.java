package com.service_reservation.EAP03.modulos.recursos.infrastructure.config;

import com.service_reservation.EAP03.modulos.identidad.domain.ports.in.ConsultarProveedorUseCase;
import com.service_reservation.EAP03.modulos.identidad.infrastructure.inbound.security.UsuarioAutenticadoPrincipal;
import com.service_reservation.EAP03.modulos.recursos.domain.model.Recurso;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.out.RecursoRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecursoSecurityTest {

    private static final BigDecimal PRECIO = new BigDecimal("50000.00");

    @Mock private ConsultarProveedorUseCase consultarProveedorUseCase;
    @Mock private RecursoRepositoryPort recursoRepositoryPort;

    @InjectMocks private RecursoSecurity recursoSecurity;

    private Authentication autenticadoComoUsuario(long idUsuario) {
        UsuarioAutenticadoPrincipal principal =
                new UsuarioAutenticadoPrincipal(idUsuario, "test@test.com", List.of("PROVEEDOR"));
        return new UsernamePasswordAuthenticationToken(principal, null, List.of());
    }

    private Recurso recursoDelProveedor(int idProveedor) {
        return Recurso.reconstruir(1, idProveedor, "Camilla", PRECIO, 3,
                LocalDateTime.of(2026, 9, 1, 8, 0), null);
    }

    @Test
    @DisplayName("El proveedor dueño del recurso pasa la verificacion")
    void dueñoPasa() {
        when(consultarProveedorUseCase.buscarIdProveedorPorUsuario(10L)).thenReturn(Optional.of(4));
        when(recursoRepositoryPort.buscarPorId(1)).thenReturn(Optional.of(recursoDelProveedor(4)));

        assertTrue(recursoSecurity.esPropietario(autenticadoComoUsuario(10L), 1));
    }

    @Test
    @DisplayName("Un proveedor de otro negocio NO pasa la verificacion")
    void otroProveedorNoPasa() {
        when(consultarProveedorUseCase.buscarIdProveedorPorUsuario(10L)).thenReturn(Optional.of(4));
        when(recursoRepositoryPort.buscarPorId(1)).thenReturn(Optional.of(recursoDelProveedor(9)));

        assertFalse(recursoSecurity.esPropietario(autenticadoComoUsuario(10L), 1));
    }

    @Test
    @DisplayName("Un usuario sin perfil de proveedor NO pasa")
    void usuarioSinPerfilProveedor() {
        when(consultarProveedorUseCase.buscarIdProveedorPorUsuario(10L)).thenReturn(Optional.empty());

        assertFalse(recursoSecurity.esPropietario(autenticadoComoUsuario(10L), 1));
    }

    @Test
    @DisplayName("Si el recurso no existe NO pasa")
    void recursoInexistente() {
        when(consultarProveedorUseCase.buscarIdProveedorPorUsuario(10L)).thenReturn(Optional.of(4));
        when(recursoRepositoryPort.buscarPorId(1)).thenReturn(Optional.empty());

        assertFalse(recursoSecurity.esPropietario(autenticadoComoUsuario(10L), 1));
    }

    @Test
    @DisplayName("Sin autenticacion NO pasa")
    void sinAutenticacion() {
        assertFalse(recursoSecurity.esPropietario(null, 1));
    }

    @Test
    @DisplayName("Con id de recurso nulo NO pasa")
    void idNulo() {
        assertFalse(recursoSecurity.esPropietario(autenticadoComoUsuario(10L), null));
    }
}
