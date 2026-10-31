package com.service_reservation.EAP03.config;

import com.service_reservation.EAP03.modulos.identidad.domain.ports.out.JwtPort;
import com.service_reservation.EAP03.modulos.identidad.infrastructure.inbound.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@SpringJUnitWebConfig(SwaggerSecurityConfigTest.TestConfig.class)
@TestPropertySource(properties = {
        "swagger.security.enabled=true",
        "swagger.security.username=docs",
        "swagger.security.password=DocumentationTestPassword123!"
})
class SwaggerSecurityConfigTest {

    @Configuration
    @EnableWebMvc
    @Import(SecurityConfig.class)
    static class TestConfig {
        @Bean
        JwtAuthenticationFilter jwtAuthenticationFilter() {
            return new JwtAuthenticationFilter(mock(JwtPort.class));
        }
    }

    @Autowired
    private AuthenticationProvider swaggerAuthenticationProvider;

    @Autowired
    private List<SecurityFilterChain> filterChains;

    @Test
    @DisplayName("La cadena Swagger requiere Basic Auth y mantiene CSRF habilitado")
    void swaggerChainRequiresBasicAuthenticationAndKeepsCsrfEnabled() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/swagger-ui/index.html");
        request.setServletPath("/swagger-ui/index.html");

        SecurityFilterChain swaggerChain = filterChains.stream()
                .filter(chain -> chain.matches(request))
                .findFirst()
                .orElseThrow();

        assertTrue(swaggerChain.getFilters().stream().anyMatch(BasicAuthenticationFilter.class::isInstance));
        assertTrue(swaggerChain.getFilters().stream().anyMatch(CsrfFilter.class::isInstance));
    }

    @Test
    @DisplayName("El proveedor de Swagger autentica las credenciales configuradas")
    void swaggerProviderAuthenticatesConfiguredCredentials() {
        Authentication result = swaggerAuthenticationProvider.authenticate(
                new UsernamePasswordAuthenticationToken("docs", "DocumentationTestPassword123!"));

        assertNotNull(result);
        assertTrue(result.isAuthenticated());
        assertEquals("docs", result.getName());
        assertTrue(result.getAuthorities().stream().anyMatch(authority -> authority.getAuthority().equals("ROLE_DOCS")));
    }

    @Test
    @DisplayName("El proveedor de Swagger rechaza una contraseña incorrecta")
    void swaggerProviderRejectsIncorrectPassword() {
        assertThrows(BadCredentialsException.class, () -> swaggerAuthenticationProvider.authenticate(
                new UsernamePasswordAuthenticationToken("docs", "incorrect-password")));
    }
}
