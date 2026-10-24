package com.service_reservation.EAP03.modulos.identidad.application;

import com.service_reservation.EAP03.modulos.identidad.application.exception.ReglaNegocioException;
import com.service_reservation.EAP03.modulos.identidad.domain.model.ResultadoConfiguracion2fa;
import com.service_reservation.EAP03.modulos.identidad.domain.model.Usuario;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.out.TotpPort;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.out.UsuarioRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class Configurar2faServiceTest {

    @Mock private UsuarioRepositoryPort usuarioRepository;
    @Mock private TotpPort totpPort;

    @InjectMocks private Configurar2faService service;

    @Test
    @DisplayName("Genera y guarda un secreto nuevo y devuelve su código QR")
    void generatesSecretAndQr() {
        // Arrange
        byte[] qr = {1, 2, 3};
        when(usuarioRepository.buscarPorId(1L)).thenReturn(Optional.of(new Usuario(1L, "admin@test.com", "hash")));
        when(totpPort.generarSecreto()).thenReturn("SECRETO");
        when(totpPort.generarQrPng("admin@test.com", "SECRETO")).thenReturn(qr);

        // Act
        ResultadoConfiguracion2fa resultado = service.ejecutar(1L);

        // Assert
        assertEquals("SECRETO", resultado.secreto());
        assertArrayEquals(qr, resultado.imagenQr());
        verify(usuarioRepository).guardarSecret2fa(1L, "SECRETO");
    }

    @Test
    @DisplayName("Rechaza la configuración si el usuario no existe")
    void unknownUser() {
        // Arrange
        when(usuarioRepository.buscarPorId(1L)).thenReturn(Optional.empty());

        // Act
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () -> service.ejecutar(1L));

        // Assert
        assertEquals("Usuario no encontrado", ex.getMessage());
        verifyNoInteractions(totpPort);
    }
}
