package com.service_reservation.EAP03.modulos.identidad.application;

import com.service_reservation.EAP03.modulos.identidad.application.exception.ReglaNegocioException;
import com.service_reservation.EAP03.modulos.identidad.domain.exception.CredencialesInvalidasException;
import com.service_reservation.EAP03.modulos.identidad.domain.model.TokenRespuesta;
import com.service_reservation.EAP03.modulos.identidad.domain.model.Usuario;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.out.JwtPort;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.out.TotpPort;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerificarLogin2faServiceTest {

    private static final String EMAIL = "admin@test.com";
    private static final String TOKEN_TEMPORAL = "temporal";

    @Mock private UsuarioRepositoryPort usuarioRepository;
    @Mock private TotpPort totpPort;
    @Mock private JwtPort jwtPort;

    @InjectMocks private VerificarLogin2faService service;

    private static Usuario user(boolean habilitado, String secreto, boolean con2fa) {
        return new Usuario(1L, EMAIL, "hash", habilitado, Set.of("ROLE_ADMIN"), secreto, con2fa);
    }

    private void validTemporaryTokenFor(Usuario usuario) {
        when(jwtPort.esTokenTemporal2faValido(TOKEN_TEMPORAL)).thenReturn(true);
        when(jwtPort.extraerEmail(TOKEN_TEMPORAL)).thenReturn(EMAIL);
        when(usuarioRepository.buscarPorEmail(EMAIL)).thenReturn(Optional.ofNullable(usuario));
    }

    @Test
    @DisplayName("Con token temporal y código válidos devuelve el JWT definitivo")
    void successfulVerification() {
        // Arrange
        Usuario usuario = user(true, "SECRETO", true);
        validTemporaryTokenFor(usuario);
        when(totpPort.validarCodigo("SECRETO", "123456")).thenReturn(true);
        when(jwtPort.generarToken(usuario)).thenReturn("jwt");
        when(jwtPort.getExpiracionSegundos()).thenReturn(3600L);

        // Act
        TokenRespuesta respuesta = service.ejecutar(TOKEN_TEMPORAL, "123456");

        // Assert
        assertEquals(TokenRespuesta.exitoso("jwt", "Bearer", 3600L), respuesta);
    }

    @Test
    @DisplayName("Rechaza un token temporal inválido o expirado")
    void invalidTemporaryToken() {
        // Arrange
        when(jwtPort.esTokenTemporal2faValido(TOKEN_TEMPORAL)).thenReturn(false);

        // Act
        CredencialesInvalidasException ex = assertThrows(CredencialesInvalidasException.class,
                () -> service.ejecutar(TOKEN_TEMPORAL, "123456"));

        // Assert
        assertEquals("Token temporal 2FA inválido o expirado", ex.getMessage());
        verifyNoInteractions(usuarioRepository, totpPort);
    }

    @Test
    @DisplayName("Rechaza la verificación si el usuario del token no existe")
    void unknownUser() {
        // Arrange
        validTemporaryTokenFor(null);

        // Act
        CredencialesInvalidasException ex = assertThrows(CredencialesInvalidasException.class,
                () -> service.ejecutar(TOKEN_TEMPORAL, "123456"));

        // Assert
        assertEquals("Usuario no encontrado", ex.getMessage());
    }

    @Test
    @DisplayName("Rechaza la verificación de una cuenta inactiva")
    void inactiveAccount() {
        // Arrange
        validTemporaryTokenFor(user(false, "SECRETO", true));

        // Act
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> service.ejecutar(TOKEN_TEMPORAL, "123456"));

        // Assert
        assertEquals("La cuenta de usuario se encuentra inactiva", ex.getMessage());
    }

    @Test
    @DisplayName("Rechaza la verificación si el usuario no tiene 2FA activado")
    void userWithout2faEnabled() {
        // Arrange
        validTemporaryTokenFor(user(true, "SECRETO", false));

        // Act
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> service.ejecutar(TOKEN_TEMPORAL, "123456"));

        // Assert
        assertEquals("El usuario no tiene 2FA configurado", ex.getMessage());
        verifyNoInteractions(totpPort);
    }

    @Test
    @DisplayName("Rechaza la verificación si el 2FA está activo pero no hay secreto")
    void userWithoutSecret() {
        // Arrange
        validTemporaryTokenFor(user(true, null, true));

        // Act
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> service.ejecutar(TOKEN_TEMPORAL, "123456"));

        // Assert
        assertEquals("El usuario no tiene 2FA configurado", ex.getMessage());
        verifyNoInteractions(totpPort);
    }

    @Test
    @DisplayName("Rechaza un código TOTP incorrecto sin emitir token")
    void wrongCode() {
        // Arrange
        validTemporaryTokenFor(user(true, "SECRETO", true));
        when(totpPort.validarCodigo("SECRETO", "000000")).thenReturn(false);

        // Act
        CredencialesInvalidasException ex = assertThrows(CredencialesInvalidasException.class,
                () -> service.ejecutar(TOKEN_TEMPORAL, "000000"));

        // Assert
        assertEquals("Código 2FA incorrecto o expirado", ex.getMessage());
        verify(jwtPort, never()).generarToken(any());
    }
}
