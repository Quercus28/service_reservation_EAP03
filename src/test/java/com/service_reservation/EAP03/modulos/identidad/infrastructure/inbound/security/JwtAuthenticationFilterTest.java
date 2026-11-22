package com.service_reservation.EAP03.modulos.identidad.infrastructure.inbound.security;

import com.service_reservation.EAP03.modulos.identidad.domain.ports.out.JwtPort;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
        when(jwtPort.extraerEtapa(TOKEN)).thenReturn(null);
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
        assertFalse(principal.isConfiguracion2fa());
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
    @DisplayName("Un token temporal de verificación 2FA no sirve para autenticarse, ni siquiera en /2fa/qr")
    void temporaryTokenDoesNotAuthenticate() throws Exception {
        // Arrange
        request = new MockHttpServletRequest("GET", "/api/v1/auth/2fa/qr");
        request.addHeader("Authorization", "Bearer " + TOKEN);
        when(jwtPort.validarToken(TOKEN)).thenReturn(true);
        when(jwtPort.extraerEtapa(TOKEN)).thenReturn(JwtPort.ETAPA_VERIFICAR_2FA);

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
        verify(jwtPort, never()).extraerEmail(anyString());
    }

    @Test
    @DisplayName("Un token de una etapa desconocida tampoco sirve como sesión")
    void unknownStageDoesNotAuthenticate() throws Exception {
        // Arrange
        request.addHeader("Authorization", "Bearer " + TOKEN);
        when(jwtPort.validarToken(TOKEN)).thenReturn(true);
        when(jwtPort.extraerEtapa(TOKEN)).thenReturn("OTRA_ETAPA");

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(jwtPort, never()).extraerRoles(anyString());
    }

    @ParameterizedTest(name = "{0} {1}")
    @CsvSource({"GET,/api/v1/auth/2fa/qr", "POST,/api/v1/auth/2fa/activar"})
    @DisplayName("El token de configuración autentica en /2fa/qr y /2fa/activar solo con el permiso especial y sin roles")
    void setupTokenAuthenticatesOnSetupRoutes(String metodo, String ruta) throws Exception {
        // Arrange
        request = new MockHttpServletRequest(metodo, ruta);
        request.addHeader("Authorization", "Bearer " + TOKEN);
        when(jwtPort.validarToken(TOKEN)).thenReturn(true);
        when(jwtPort.extraerEtapa(TOKEN)).thenReturn(JwtPort.ETAPA_CONFIGURAR_2FA);
        when(jwtPort.extraerUsuarioId(TOKEN)).thenReturn(1L);
        when(jwtPort.extraerEmail(TOKEN)).thenReturn("admin@test.com");

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        UsuarioAutenticadoPrincipal principal = assertInstanceOf(UsuarioAutenticadoPrincipal.class, auth.getPrincipal());
        assertTrue(principal.isConfiguracion2fa());
        assertEquals(1L, principal.getIdUsuario());
        assertTrue(principal.getRoles().isEmpty());
        assertEquals(Set.of(JwtAuthenticationFilter.PERMISO_CONFIGURAR_2FA), auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).collect(Collectors.toSet()));
        verify(jwtPort, never()).extraerRoles(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @ParameterizedTest(name = "{0} {1}")
    @CsvSource({
            "GET,/api/v1/servicios",
            "POST,/api/v1/auth/2fa/qr",
            "GET,/api/v1/auth/2fa/activar",
            "POST,/api/v1/auth/2fa/verificar-login",
            "GET,/api/v1/auth/2fa/qr/otra",
            "GET,/api/admin/permisos"
    })
    @DisplayName("El token de configuración no sirve en ninguna otra ruta ni con otro método")
    void setupTokenIgnoredElsewhere(String metodo, String ruta) throws Exception {
        // Arrange
        request = new MockHttpServletRequest(metodo, ruta);
        request.addHeader("Authorization", "Bearer " + TOKEN);
        when(jwtPort.validarToken(TOKEN)).thenReturn(true);
        when(jwtPort.extraerEtapa(TOKEN)).thenReturn(JwtPort.ETAPA_CONFIGURAR_2FA);

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Respeta el contexto de la aplicación al reconocer las rutas de configuración")
    void setupTokenWithContextPath() throws Exception {
        // Arrange
        request = new MockHttpServletRequest("GET", "/app/api/v1/auth/2fa/qr");
        request.setContextPath("/app");
        request.addHeader("Authorization", "Bearer " + TOKEN);
        when(jwtPort.validarToken(TOKEN)).thenReturn(true);
        when(jwtPort.extraerEtapa(TOKEN)).thenReturn(JwtPort.ETAPA_CONFIGURAR_2FA);
        when(jwtPort.extraerUsuarioId(TOKEN)).thenReturn(1L);
        when(jwtPort.extraerEmail(TOKEN)).thenReturn("admin@test.com");

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("Si falla la lectura del token de configuración limpia el contexto y continúa la cadena")
    void setupTokenReadErrorClearsContext() throws Exception {
        // Arrange
        request = new MockHttpServletRequest("GET", "/api/v1/auth/2fa/qr");
        request.addHeader("Authorization", "Bearer " + TOKEN);
        when(jwtPort.validarToken(TOKEN)).thenReturn(true);
        when(jwtPort.extraerEtapa(TOKEN)).thenReturn(JwtPort.ETAPA_CONFIGURAR_2FA);
        when(jwtPort.extraerUsuarioId(TOKEN)).thenThrow(new IllegalStateException("token corrupto"));

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Si falla la lectura del token limpia el contexto y continúa la cadena")
    void tokenReadErrorClearsContext() throws Exception {
        // Arrange
        request.addHeader("Authorization", "Bearer " + TOKEN);
        when(jwtPort.validarToken(TOKEN)).thenReturn(true);
        when(jwtPort.extraerEtapa(TOKEN)).thenReturn(null);
        when(jwtPort.extraerEmail(TOKEN)).thenThrow(new IllegalStateException("token corrupto"));

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @ParameterizedTest(name = "{0} -> excluida={1}")
    @CsvSource({
            "/swagger-ui.html, true",
            "/swagger-ui/index.html, true",
            "/v3/api-docs, true",
            "/v3/api-docs/swagger-config, true",
            "/v3/api-docs.yaml, true",
            "/api/v1/reservas, false"
    })
    @DisplayName("Las rutas de la documentación de Swagger no pasan por el filtro JWT; el resto sí")
    void swaggerPathsAreNotFiltered(String ruta, boolean excluida) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", ruta);
        request.setServletPath(ruta);

        assertEquals(excluida, filter.shouldNotFilter(request));
    }
}
