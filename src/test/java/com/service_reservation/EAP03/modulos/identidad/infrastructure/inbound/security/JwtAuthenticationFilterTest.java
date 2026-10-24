package com.service_reservation.EAP03.modulos.identidad.infrastructure.inbound.security;

import com.service_reservation.EAP03.modulos.identidad.domain.ports.out.JwtPort;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    private static final String TOKEN = "token-jwt";

    @Mock private JwtPort jwtPort;
    @Mock private FilterChain filterChain;

    private JwtAuthenticationFilter filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(jwtPort);
        request = new MockHttpServletRequest("GET", "/api/v1/servicios");
        response = new MockHttpServletResponse();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void resetContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Con un token válido autentica al usuario con sus roles prefijados con ROLE_")
    void validTokenAuthenticates() throws Exception {
        // Arrange
        request.addHeader("Authorization", "Bearer " + TOKEN);
        when(jwtPort.validarToken(TOKEN)).thenReturn(true);
        when(jwtPort.esTokenTemporal2faValido(TOKEN)).thenReturn(false);
        when(jwtPort.extraerEmail(TOKEN)).thenReturn("ana@test.com");
        when(jwtPort.extraerUsuarioId(TOKEN)).thenReturn(1L);
        when(jwtPort.extraerRoles(TOKEN)).thenReturn(List.of("ROLE_ADMIN", "CLIENTE"));

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        UsuarioAutenticadoPrincipal principal = assertInstanceOf(UsuarioAutenticadoPrincipal.class, auth.getPrincipal());
        assertEquals(1L, principal.getIdUsuario());
        assertEquals("ana@test.com", principal.getEmail());
        Set<String> authorities = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
        assertEquals(Set.of("ROLE_ADMIN", "ROLE_CLIENTE"), authorities);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Sin encabezado Authorization continúa la cadena sin autenticar")
    void missingHeader() throws Exception {
        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtPort);
    }

    @Test
    @DisplayName("Con un encabezado que no es Bearer continúa la cadena sin autenticar")
    void nonBearerHeader() throws Exception {
        // Arrange
        request.addHeader("Authorization", "Basic dXN1YXJpbzpjbGF2ZQ==");

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtPort);
    }

    @Test
    @DisplayName("Con un token inválido continúa la cadena sin autenticar")
    void invalidToken() throws Exception {
        // Arrange
        request.addHeader("Authorization", "Bearer " + TOKEN);
        when(jwtPort.validarToken(TOKEN)).thenReturn(false);

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
        verify(jwtPort, never()).extraerEmail(anyString());
    }

    @Test
    @DisplayName("Un token temporal de 2FA no sirve para autenticarse")
    void temporaryTokenDoesNotAuthenticate() throws Exception {
        // Arrange
        request.addHeader("Authorization", "Bearer " + TOKEN);
        when(jwtPort.validarToken(TOKEN)).thenReturn(true);
        when(jwtPort.esTokenTemporal2faValido(TOKEN)).thenReturn(true);

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
        verify(jwtPort, never()).extraerEmail(anyString());
    }

    @Test
    @DisplayName("Si falla la lectura del token limpia el contexto y continúa la cadena")
    void tokenReadErrorClearsContext() throws Exception {
        // Arrange
        request.addHeader("Authorization", "Bearer " + TOKEN);
        when(jwtPort.validarToken(TOKEN)).thenReturn(true);
        when(jwtPort.esTokenTemporal2faValido(TOKEN)).thenReturn(false);
        when(jwtPort.extraerEmail(TOKEN)).thenThrow(new IllegalStateException("token corrupto"));

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }
}
