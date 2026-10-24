package com.service_reservation.EAP03.modulos.servicios_catalogo.infrastructure.config;

import com.service_reservation.EAP03.modulos.identidad.domain.ports.in.ConsultarProveedorUseCase;
import com.service_reservation.EAP03.modulos.identidad.infrastructure.inbound.security.UsuarioAutenticadoPrincipal;
import com.service_reservation.EAP03.modulos.servicios_catalogo.domain.model.Servicio;
import com.service_reservation.EAP03.modulos.servicios_catalogo.domain.repository.ServicioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicioSecurityTest {

    private static final Long ID_USUARIO = 10L;
    private static final Integer ID_PROVEEDOR = 5;
    private static final Integer ID_SERVICIO = 7;

    @Mock
    private ServicioRepository servicioRepository;

    @Mock
    private ConsultarProveedorUseCase consultarProveedorUseCase;

    private ServicioSecurity servicioSecurity;

    @BeforeEach
    void setUp() {
        servicioSecurity = new ServicioSecurity(servicioRepository, consultarProveedorUseCase);
    }

    private static Authentication autenticado(Object principal) {
        return new UsernamePasswordAuthenticationToken(
                principal, null, List.of(new SimpleGrantedAuthority("ROLE_PROVEEDOR")));
    }

    private static UsuarioAutenticadoPrincipal proveedor() {
        return new UsuarioAutenticadoPrincipal(ID_USUARIO, "proveedor@test.com", List.of("PROVEEDOR"));
    }

    @Test
    @DisplayName("Debe reconocer como propietario al proveedor dueño del servicio")
    void proveedorDuenoEsPropietario() {
        // Arrange
        when(consultarProveedorUseCase.buscarIdProveedorPorUsuario(ID_USUARIO)).thenReturn(Optional.of(ID_PROVEEDOR));
        when(servicioRepository.buscarPorId(ID_SERVICIO))
                .thenReturn(Optional.of(Servicio.reconstituir(ID_SERVICIO, ID_PROVEEDOR, "Corte", 30, null)));

        // Act & Assert
        assertTrue(servicioSecurity.esPropietario(autenticado(proveedor()), ID_SERVICIO));
    }

    @Test
    @DisplayName("No debe reconocer como propietario a un proveedor distinto al dueño")
    void otroProveedorNoEsPropietario() {
        // Arrange
        when(consultarProveedorUseCase.buscarIdProveedorPorUsuario(ID_USUARIO)).thenReturn(Optional.of(ID_PROVEEDOR));
        when(servicioRepository.buscarPorId(ID_SERVICIO))
                .thenReturn(Optional.of(Servicio.reconstituir(ID_SERVICIO, 99, "Corte", 30, null)));

        // Act & Assert
        assertFalse(servicioSecurity.esPropietario(autenticado(proveedor()), ID_SERVICIO));
    }

    @Test
    @DisplayName("No es propietario si el servicio no existe")
    void servicioInexistenteNoTienePropietario() {
        // Arrange
        when(consultarProveedorUseCase.buscarIdProveedorPorUsuario(ID_USUARIO)).thenReturn(Optional.of(ID_PROVEEDOR));
        when(servicioRepository.buscarPorId(ID_SERVICIO)).thenReturn(Optional.empty());

        // Act & Assert
        assertFalse(servicioSecurity.esPropietario(autenticado(proveedor()), ID_SERVICIO));
    }

    @Test
    @DisplayName("No es propietario si el usuario no tiene perfil de proveedor")
    void usuarioSinPerfilProveedorNoEsPropietario() {
        // Arrange
        when(consultarProveedorUseCase.buscarIdProveedorPorUsuario(ID_USUARIO)).thenReturn(Optional.empty());

        // Act & Assert
        assertFalse(servicioSecurity.esPropietario(autenticado(proveedor()), ID_SERVICIO));
        verifyNoInteractions(servicioRepository);
    }

    @Test
    @DisplayName("No es propietario si la autenticación es nula")
    void autenticacionNula() {
        assertFalse(servicioSecurity.esPropietario(null, ID_SERVICIO));
        verifyNoInteractions(servicioRepository, consultarProveedorUseCase);
    }

    @Test
    @DisplayName("No es propietario si la autenticación no está autenticada")
    void autenticacionNoAutenticada() {
        // Arrange
        Authentication noAutenticada = new UsernamePasswordAuthenticationToken(proveedor(), null);

        // Act & Assert
        assertFalse(servicioSecurity.esPropietario(noAutenticada, ID_SERVICIO));
        verifyNoInteractions(servicioRepository, consultarProveedorUseCase);
    }

    @Test
    @DisplayName("No es propietario si el principal no es un usuario autenticado del sistema")
    void principalDesconocido() {
        assertFalse(servicioSecurity.esPropietario(autenticado("anonimo"), ID_SERVICIO));
        verifyNoInteractions(servicioRepository, consultarProveedorUseCase);
    }

    @Test
    @DisplayName("No es propietario si el id del servicio es nulo")
    void idServicioNulo() {
        assertFalse(servicioSecurity.esPropietario(autenticado(proveedor()), null));
        verifyNoInteractions(servicioRepository, consultarProveedorUseCase);
    }
}
