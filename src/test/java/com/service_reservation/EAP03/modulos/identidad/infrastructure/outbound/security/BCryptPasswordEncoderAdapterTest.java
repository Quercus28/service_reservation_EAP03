package com.service_reservation.EAP03.modulos.identidad.infrastructure.outbound.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

class BCryptPasswordEncoderAdapterTest {

    private final BCryptPasswordEncoderAdapter adapter = new BCryptPasswordEncoderAdapter(new BCryptPasswordEncoder(4));

    @Test
    @DisplayName("Codifica la contraseña sin dejarla en texto plano")
    void encodesPassword() {
        // Act
        String hash = adapter.codificar("Clave123");

        // Assert
        assertNotEquals("Clave123", hash);
        assertTrue(hash.startsWith("$2"));
    }

    @Test
    @DisplayName("Reconoce la contraseña correcta y rechaza una incorrecta")
    void checksMatch() {
        // Arrange
        String hash = adapter.codificar("Clave123");

        // Act & Assert
        assertTrue(adapter.coincide("Clave123", hash));
        assertFalse(adapter.coincide("Clave124", hash));
    }
}
