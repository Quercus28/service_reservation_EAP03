package com.service_reservation.EAP03.config;

import com.service_reservation.EAP03.modulos.admin.domain.model.IntentoAccesoDenegado;
import com.service_reservation.EAP03.modulos.admin.domain.ports.in.RegistrarAccesoDenegadoUseCase;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.out.JwtPort;
import com.service_reservation.EAP03.modulos.identidad.infrastructure.inbound.security.JwtAuthenticationFilter;
import com.service_reservation.EAP03.modulos.identidad.infrastructure.inbound.security.UsuarioAutenticadoPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SecurityConfigTest {

    @Configuration
    @EnableWebMvc
    @Import(SecurityConfig.class)
    static class WithoutAuditConfig {
        @Bean
        JwtAuthenticationFilter jwtAuthenticationFilter() {
            return new JwtAuthenticationFilter(mock(JwtPort.class));
        }
    }

    @Configuration
    @Import(WithoutAuditConfig.class)
    static class WithAuditConfig {
        @Bean
        RegistrarAccesoDenegadoUseCase registrarAccesoDenegadoUseCase() {
            return mock(RegistrarAccesoDenegadoUseCase.class);
        }
    }

    private static ExceptionTranslationFilter exceptionTranslationFilter(SecurityFilterChain chain) {
        return chain.getFilters().stream()
                .filter(ExceptionTranslationFilter.class::isInstance)
                .map(ExceptionTranslationFilter.class::cast)
                .findFirst()
                .orElseThrow();
    }

    private static AccessDeniedHandler accessDeniedHandler(SecurityFilterChain chain) {
        return (AccessDeniedHandler) ReflectionTestUtils.getField(exceptionTranslationFilter(chain), "accessDeniedHandler");
    }

    private static AuthenticationEntryPoint authenticationEntryPoint(SecurityFilterChain chain) {
        return (AuthenticationEntryPoint) ReflectionTestUtils.getField(exceptionTranslationFilter(chain), "authenticationEntryPoint");
    }

    @Nested
    @SpringJUnitWebConfig(WithAuditConfig.class)
    class WithAuditService {

        @Autowired private SecurityFilterChain chain;
        @Autowired private PasswordEncoder passwordEncoder;
        @Autowired private RegistrarAccesoDenegadoUseCase auditUseCase;

        private final MockHttpServletResponse response = new MockHttpServletResponse();

        @BeforeEach
        void resetAudit() {
            reset(auditUseCase);
        }

        @AfterEach
        void clearSecurityContext() {
            SecurityContextHolder.clearContext();
        }

        private IntentoAccesoDenegado deniedInAdmin() throws Exception {
            MockHttpServletRequest request = new MockHttpServletRequest("DELETE", "/api/admin/permisos/3");
            accessDeniedHandler(chain).handle(request, response, new AccessDeniedException("x"));
            ArgumentCaptor<IntentoAccesoDenegado> captor = ArgumentCaptor.forClass(IntentoAccesoDenegado.class);
            verify(auditUseCase).registrar(captor.capture());
            return captor.getValue();
        }

        @Test
        @DisplayName("El codificador de contraseñas es BCrypt")
        void passwordEncoderIsBcrypt() {
            // Act
            String hash = passwordEncoder.encode("Clave123");

            // Assert
            assertInstanceOf(BCryptPasswordEncoder.class, passwordEncoder);
            assertTrue(passwordEncoder.matches("Clave123", hash));
        }

        @Test
        @DisplayName("Sin autenticación responde 401 en JSON UTF-8 con la ruta solicitada")
        void entryPointReturns401Json() throws Exception {
            // Arrange
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/servicios");

            // Act
            authenticationEntryPoint(chain).commence(request, response, new InsufficientAuthenticationException("x"));

            // Assert
            assertEquals(401, response.getStatus());
            assertTrue(response.getContentType().startsWith("application/json"));
            assertEquals("UTF-8", response.getCharacterEncoding());
            String cuerpo = response.getContentAsString(StandardCharsets.UTF_8);
            assertTrue(cuerpo.contains("\"status\":401"));
            assertTrue(cuerpo.contains("Se requiere autenticación para acceder a este recurso."));
            assertTrue(cuerpo.contains("\"path\":\"/api/v1/servicios\""));
        }

        @Test
        @DisplayName("Un acceso denegado fuera de /api/admin responde 403 sin auditar")
        void accessDeniedOutsideAdminIsNotAudited() throws Exception {
            // Arrange
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/agendas");

            // Act
            accessDeniedHandler(chain).handle(request, response, new AccessDeniedException("x"));

            // Assert
            assertEquals(403, response.getStatus());
            assertEquals("UTF-8", response.getCharacterEncoding());
            String cuerpo = response.getContentAsString(StandardCharsets.UTF_8);
            assertTrue(cuerpo.contains("No tiene los permisos necesarios para realizar esta acción."));
            assertTrue(cuerpo.contains("\"path\":\"/api/v1/agendas\""));
            verifyNoInteractions(auditUseCase);
        }

        @Test
        @DisplayName("Un acceso denegado en /api/admin se audita con el id del usuario autenticado")
        void accessDeniedInAdminIsAuditedWithUserId() throws Exception {
            // Arrange
            UsuarioAutenticadoPrincipal principal = new UsuarioAutenticadoPrincipal(7L, "cliente@test.com", List.of("CLIENTE"));
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(principal, null, List.of()));

            // Act
            IntentoAccesoDenegado intento = deniedInAdmin();

            // Assert
            assertEquals(403, response.getStatus());
            assertEquals(7L, intento.idUsuario());
            assertEquals("DELETE", intento.metodo());
            assertEquals("/api/admin/permisos/3", intento.endpoint());
            assertNotNull(intento.fecha());
        }

        @Test
        @DisplayName("Un acceso denegado en /api/admin sin usuario autenticado se audita sin id")
        void accessDeniedInAdminWithoutAuthenticationIsAuditedWithoutUserId() throws Exception {
            assertNull(deniedInAdmin().idUsuario());
        }

        @Test
        @DisplayName("Un acceso denegado en /api/admin con otro tipo de principal se audita sin id")
        void accessDeniedInAdminWithUnknownPrincipalIsAuditedWithoutUserId() throws Exception {
            // Arrange
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken("anonimo", null, List.of()));

            // Act & Assert
            assertNull(deniedInAdmin().idUsuario());
        }
    }

    @Nested
    @SpringJUnitWebConfig(WithoutAuditConfig.class)
    class WithoutAuditService {

        @Autowired private SecurityFilterChain chain;

        @Test
        @DisplayName("Un acceso denegado en /api/admin responde 403 aunque no haya servicio de auditoría")
        void accessDeniedInAdminWithoutAuditService() throws Exception {
            // Arrange
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/admin/solicitudes");
            MockHttpServletResponse response = new MockHttpServletResponse();

            // Act
            accessDeniedHandler(chain).handle(request, response, new AccessDeniedException("x"));

            // Assert
            assertEquals(403, response.getStatus());
            assertTrue(response.getContentAsString(StandardCharsets.UTF_8).contains("\"path\":\"/api/admin/solicitudes\""));
        }
    }
}
