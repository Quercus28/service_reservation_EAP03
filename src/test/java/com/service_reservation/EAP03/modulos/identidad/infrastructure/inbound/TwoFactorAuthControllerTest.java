package com.service_reservation.EAP03.modulos.identidad.infrastructure.inbound;

import com.service_reservation.EAP03.modulos.identidad.domain.model.ResultadoConfiguracion2fa;
import com.service_reservation.EAP03.modulos.identidad.domain.model.TokenRespuesta;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.in.Activar2faUseCase;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.in.Configurar2faUseCase;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.in.VerificarLogin2faUseCase;
import com.service_reservation.EAP03.modulos.identidad.infrastructure.inbound.security.UsuarioAutenticadoPrincipal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TwoFactorAuthControllerTest {

    @Mock private Configurar2faUseCase configurar2faUseCase;
    @Mock private Activar2faUseCase activar2faUseCase;
    @Mock private VerificarLogin2faUseCase verificarLogin2faUseCase;

    @InjectMocks private TwoFactorAuthController controller;

    private static UsuarioAutenticadoPrincipal admin() {
        return new UsuarioAutenticadoPrincipal(1L, "admin@test.com", List.of("ADMIN"));
    }

    @Test
    @DisplayName("GET /qr devuelve la imagen PNG y expone el secreto en un encabezado")
    void returnsQrImage() {
        // Arrange
        byte[] qr = {1, 2, 3};
        when(configurar2faUseCase.ejecutar(1L)).thenReturn(new ResultadoConfiguracion2fa(qr, "SECRETO"));

        // Act
        ResponseEntity<byte[]> response = controller.obtenerQr(admin());

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertEquals(MediaType.IMAGE_PNG, response.getHeaders().getContentType());
        assertEquals("SECRETO", response.getHeaders().getFirst("X-2FA-Secret"));
        assertEquals("X-2FA-Secret", response.getHeaders().getFirst(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS));
        assertArrayEquals(qr, response.getBody());
    }

    @Test
    @DisplayName("GET /qr responde 401 sin usuario autenticado")
    void qrWithoutAuthentication() {
        // Act
        ResponseEntity<byte[]> response = controller.obtenerQr(null);

        // Assert
        assertEquals(401, response.getStatusCode().value());
        verifyNoInteractions(configurar2faUseCase);
    }

    @Test
    @DisplayName("POST /activar activa el 2FA del usuario autenticado")
    void enables2fa() {
        // Act
        ResponseEntity<Map<String, String>> response = controller.activar2fa(admin(), new Activar2faRequestDTO("123456"));

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertEquals(Map.of("message", "Autenticación de dos factores activada exitosamente"), response.getBody());
        verify(activar2faUseCase).ejecutar(1L, "123456");
    }

    @Test
    @DisplayName("POST /activar responde 401 sin usuario autenticado")
    void enable2faWithoutAuthentication() {
        // Act
        ResponseEntity<Map<String, String>> response = controller.activar2fa(null, new Activar2faRequestDTO("123456"));

        // Assert
        assertEquals(401, response.getStatusCode().value());
        verifyNoInteractions(activar2faUseCase);
    }

    @Test
    @DisplayName("POST /verificar-login devuelve el JWT definitivo")
    void verifiesLogin() {
        // Arrange
        when(verificarLogin2faUseCase.ejecutar("temporal", "123456"))
                .thenReturn(TokenRespuesta.exitoso("jwt", "Bearer", 3600));

        // Act
        ResponseEntity<LoginResponseDTO> response =
                controller.verificarLogin(new Verificar2faLoginRequestDTO("temporal", "123456"));

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertEquals(new LoginResponseDTO("jwt", "Bearer", 3600, false, null), response.getBody());
    }
}
