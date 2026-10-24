package com.service_reservation.EAP03.modulos.identidad.infrastructure.outbound.security;

import com.service_reservation.EAP03.modulos.identidad.domain.model.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class JwtAdapterTest {

    private static final String SECRETO = "clave-de-pruebas-de-al-menos-32-bytes-para-hs256";

    private final JwtAdapter jwtAdapter = new JwtAdapter(SECRETO, 3600);

    private static Usuario user(Long id) {
        return new Usuario(id, "ana@test.com", "hash", true, Set.of("ROLE_CLIENTE"));
    }

    @Test
    @DisplayName("Genera un token válido con correo, id y roles del usuario")
    void generatesTokenWithUserData() {
        // Act
        String token = jwtAdapter.generarToken(user(7L));

        // Assert
        assertTrue(jwtAdapter.validarToken(token));
        assertEquals("ana@test.com", jwtAdapter.extraerEmail(token));
        assertEquals(7L, jwtAdapter.extraerUsuarioId(token));
        assertEquals(List.of("ROLE_CLIENTE"), jwtAdapter.extraerRoles(token));
        assertFalse(jwtAdapter.esTokenTemporal2faValido(token));
    }

    @Test
    @DisplayName("Devuelve la expiración configurada en segundos")
    void configuredExpiration() {
        assertEquals(3600, jwtAdapter.getExpiracionSegundos());
    }

    @Test
    @DisplayName("Un usuario sin id produce un token sin id")
    void tokenWithoutId() {
        // Act
        String token = jwtAdapter.generarToken(user(null));

        // Assert
        assertNull(jwtAdapter.extraerUsuarioId(token));
    }

    @Test
    @DisplayName("El token temporal de 2FA se reconoce como tal y no tiene roles")
    void temporary2faToken() {
        // Act
        String token = jwtAdapter.generarTokenTemporal2fa(user(7L));

        // Assert
        assertTrue(jwtAdapter.validarToken(token));
        assertTrue(jwtAdapter.esTokenTemporal2faValido(token));
        assertEquals("ana@test.com", jwtAdapter.extraerEmail(token));
        assertEquals(7L, jwtAdapter.extraerUsuarioId(token));
        assertTrue(jwtAdapter.extraerRoles(token).isEmpty());
    }

    @Test
    @DisplayName("Rechaza un token mal formado")
    void malformedToken() {
        assertFalse(jwtAdapter.validarToken("no-es-un-jwt"));
        assertFalse(jwtAdapter.esTokenTemporal2faValido("no-es-un-jwt"));
    }

    @Test
    @DisplayName("Rechaza un token nulo")
    void nullToken() {
        assertFalse(jwtAdapter.validarToken(null));
        assertFalse(jwtAdapter.esTokenTemporal2faValido(null));
    }

    @Test
    @DisplayName("Rechaza un token firmado con otra clave")
    void tokenFromAnotherKey() {
        // Arrange
        JwtAdapter otroEmisor = new JwtAdapter("otra-clave-distinta-de-al-menos-32-bytes-hs256", 3600);
        String token = otroEmisor.generarToken(user(7L));

        // Act & Assert
        assertFalse(jwtAdapter.validarToken(token));
    }

    @Test
    @DisplayName("Rechaza un token expirado")
    void expiredToken() {
        // Arrange
        JwtAdapter emisorExpirado = new JwtAdapter(SECRETO, -60);
        String token = emisorExpirado.generarToken(user(7L));

        // Act & Assert
        assertFalse(jwtAdapter.validarToken(token));
    }
}
