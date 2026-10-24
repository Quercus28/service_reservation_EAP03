package com.service_reservation.EAP03.modulos.identidad.application;

import com.service_reservation.EAP03.modulos.identidad.application.exception.ReglaNegocioException;
import com.service_reservation.EAP03.modulos.identidad.domain.exception.CredencialesInvalidasException;
import com.service_reservation.EAP03.modulos.identidad.domain.model.Usuario;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.out.TotpPort;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.out.UsuarioRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class Activar2faServiceTest {

    @Mock private UsuarioRepositoryPort usuarioRepository;
    @Mock private TotpPort totpPort;

    @InjectMocks private Activar2faService service;

    private static Usuario userWithSecret(String secreto) {
        return new Usuario(1L, "admin@test.com", "hash", true, Set.of("ROLE_ADMIN"), secreto, false);
    }

    @Test
    @DisplayName("Activa el 2FA cuando el código TOTP es válido")
    void activatesWithValidCode() {
        // Arrange
        when(usuarioRepository.buscarPorId(1L)).thenReturn(Optional.of(userWithSecret("SECRETO")));
        when(totpPort.validarCodigo("SECRETO", "123456")).thenReturn(true);

        // Act
        service.ejecutar(1L, "123456");

        // Assert
        verify(usuarioRepository).activar2fa(1L);
    }

    @Test
    @DisplayName("Rechaza la activación si el usuario no existe")
    void unknownUser() {
        // Arrange
        when(usuarioRepository.buscarPorId(1L)).thenReturn(Optional.empty());

        // Act
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () -> service.ejecutar(1L, "123456"));

        // Assert
        assertEquals("Usuario no encontrado", ex.getMessage());
        verifyNoInteractions(totpPort);
    }

    @ParameterizedTest(name = "secreto = [{0}]")
    @NullSource
    @ValueSource(strings = {"", "   "})
    @DisplayName("Rechaza la activación si no se ha iniciado la configuración de 2FA")
    void setupNotStarted(String secreto) {
        // Arrange
        when(usuarioRepository.buscarPorId(1L)).thenReturn(Optional.of(userWithSecret(secreto)));

        // Act
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () -> service.ejecutar(1L, "123456"));

        // Assert
        assertEquals("No se ha iniciado la configuración de 2FA para este usuario", ex.getMessage());
        verifyNoInteractions(totpPort);
        verify(usuarioRepository, never()).activar2fa(any());
    }

    @Test
    @DisplayName("Rechaza un código TOTP incorrecto sin activar el 2FA")
    void wrongCode() {
        // Arrange
        when(usuarioRepository.buscarPorId(1L)).thenReturn(Optional.of(userWithSecret("SECRETO")));
        when(totpPort.validarCodigo("SECRETO", "000000")).thenReturn(false);

        // Act
        CredencialesInvalidasException ex = assertThrows(CredencialesInvalidasException.class,
                () -> service.ejecutar(1L, "000000"));

        // Assert
        assertEquals("Código 2FA incorrecto o expirado", ex.getMessage());
        verify(usuarioRepository, never()).activar2fa(any());
    }
}
