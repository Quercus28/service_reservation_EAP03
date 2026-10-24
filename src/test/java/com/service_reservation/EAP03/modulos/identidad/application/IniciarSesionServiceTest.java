package com.service_reservation.EAP03.modulos.identidad.application;

import com.service_reservation.EAP03.modulos.identidad.application.exception.ReglaNegocioException;
import com.service_reservation.EAP03.modulos.identidad.domain.exception.CredencialesInvalidasException;
import com.service_reservation.EAP03.modulos.identidad.domain.exception.CuentaBloqueadaException;
import com.service_reservation.EAP03.modulos.identidad.domain.model.IniciarSesionComando;
import com.service_reservation.EAP03.modulos.identidad.domain.model.TokenRespuesta;
import com.service_reservation.EAP03.modulos.identidad.domain.model.Usuario;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.out.BloqueoFuerzaBrutaPort;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.out.JwtPort;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.out.PasswordEncoderPort;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.out.UsuarioRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IniciarSesionServiceTest {

    private static final String EMAIL = "ana@test.com";

    @Mock private UsuarioRepositoryPort usuarioRepository;
    @Mock private PasswordEncoderPort passwordEncoder;
    @Mock private JwtPort jwtPort;
    @Mock private BloqueoFuerzaBrutaPort bloqueoFuerzaBrutaPort;

    @InjectMocks private IniciarSesionService service;

    private static Usuario user(boolean habilitado, boolean con2fa) {
        return new Usuario(1L, EMAIL, "hash", habilitado, Set.of("ROLE_CLIENTE"), con2fa ? "SECRETO" : null, con2fa);
    }

    @Test
    @DisplayName("Login exitoso devuelve el JWT y reinicia los intentos fallidos")
    void successfulLogin() {
        // Arrange
        Usuario usuario = user(true, false);
        when(usuarioRepository.buscarPorEmail(EMAIL)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.coincide("Clave123", "hash")).thenReturn(true);
        when(jwtPort.generarToken(usuario)).thenReturn("jwt");
        when(jwtPort.getExpiracionSegundos()).thenReturn(3600L);

        // Act
        TokenRespuesta respuesta = service.ejecutar(new IniciarSesionComando(EMAIL, "Clave123"));

        // Assert
        assertEquals(TokenRespuesta.exitoso("jwt", "Bearer", 3600L), respuesta);
        verify(bloqueoFuerzaBrutaPort).resetearIntentos(EMAIL);
        verify(bloqueoFuerzaBrutaPort, never()).registrarIntentoFallido(anyString());
    }

    @Test
    @DisplayName("Normaliza el correo antes de buscar al usuario")
    void normalizesEmail() {
        // Arrange
        when(usuarioRepository.buscarPorEmail(EMAIL)).thenReturn(Optional.of(user(true, false)));
        when(passwordEncoder.coincide("Clave123", "hash")).thenReturn(true);
        when(jwtPort.generarToken(any())).thenReturn("jwt");

        // Act
        service.ejecutar(new IniciarSesionComando("  Ana@TEST.com ", "Clave123"));

        // Assert
        verify(bloqueoFuerzaBrutaPort).estaBloqueado(EMAIL);
        verify(usuarioRepository).buscarPorEmail(EMAIL);
    }

    @Test
    @DisplayName("Con 2FA activado devuelve un token temporal en lugar del JWT")
    void twoFactorLoginReturnsTemporaryToken() {
        // Arrange
        Usuario usuario = user(true, true);
        when(usuarioRepository.buscarPorEmail(EMAIL)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.coincide("Clave123", "hash")).thenReturn(true);
        when(jwtPort.generarTokenTemporal2fa(usuario)).thenReturn("temporal");

        // Act
        TokenRespuesta respuesta = service.ejecutar(new IniciarSesionComando(EMAIL, "Clave123"));

        // Assert
        assertEquals(TokenRespuesta.requiere2fa("temporal"), respuesta);
        verify(jwtPort, never()).generarToken(any());
    }

    @Test
    @DisplayName("Rechaza el login si la cuenta está bloqueada, sin consultar al usuario")
    void lockedAccount() {
        // Arrange
        when(bloqueoFuerzaBrutaPort.estaBloqueado(EMAIL)).thenReturn(true);
        IniciarSesionComando comando = new IniciarSesionComando(EMAIL, "Clave123");

        // Act
        CuentaBloqueadaException ex = assertThrows(CuentaBloqueadaException.class, () -> service.ejecutar(comando));

        // Assert
        assertTrue(ex.getMessage().contains("15 minutos"));
        verifyNoInteractions(usuarioRepository, passwordEncoder, jwtPort);
    }

    @Test
    @DisplayName("Un correo inexistente registra un intento fallido y da un error genérico")
    void unknownUser() {
        // Arrange
        when(usuarioRepository.buscarPorEmail(EMAIL)).thenReturn(Optional.empty());
        IniciarSesionComando comando = new IniciarSesionComando(EMAIL, "Clave123");

        // Act
        CredencialesInvalidasException ex = assertThrows(CredencialesInvalidasException.class, () -> service.ejecutar(comando));

        // Assert
        assertEquals("Credenciales incorrectas", ex.getMessage());
        verify(bloqueoFuerzaBrutaPort).registrarIntentoFallido(EMAIL);
    }

    @Test
    @DisplayName("Un correo nulo se trata como vacío y no encuentra usuario")
    void nullEmail() {
        // Arrange
        when(usuarioRepository.buscarPorEmail("")).thenReturn(Optional.empty());
        IniciarSesionComando comando = new IniciarSesionComando(null, "Clave123");

        // Act
        assertThrows(CredencialesInvalidasException.class, () -> service.ejecutar(comando));

        // Assert
        verify(bloqueoFuerzaBrutaPort).estaBloqueado("");
        verify(bloqueoFuerzaBrutaPort).registrarIntentoFallido("");
    }

    @Test
    @DisplayName("Rechaza el login de una cuenta inactiva sin validar la contraseña")
    void inactiveAccount() {
        // Arrange
        when(usuarioRepository.buscarPorEmail(EMAIL)).thenReturn(Optional.of(user(false, false)));
        IniciarSesionComando comando = new IniciarSesionComando(EMAIL, "Clave123");

        // Act
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () -> service.ejecutar(comando));

        // Assert
        assertEquals("La cuenta de usuario se encuentra inactiva", ex.getMessage());
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    @DisplayName("Una contraseña incorrecta registra un intento fallido y no emite token")
    void wrongPassword() {
        // Arrange
        when(usuarioRepository.buscarPorEmail(EMAIL)).thenReturn(Optional.of(user(true, false)));
        when(passwordEncoder.coincide("Mala123", "hash")).thenReturn(false);
        IniciarSesionComando comando = new IniciarSesionComando(EMAIL, "Mala123");

        // Act
        CredencialesInvalidasException ex = assertThrows(CredencialesInvalidasException.class, () -> service.ejecutar(comando));

        // Assert
        assertEquals("Credenciales incorrectas", ex.getMessage());
        verify(bloqueoFuerzaBrutaPort).registrarIntentoFallido(EMAIL);
        verify(bloqueoFuerzaBrutaPort, never()).resetearIntentos(anyString());
        verifyNoInteractions(jwtPort);
    }
}
