package com.service_reservation.EAP03.modulos.identidad.infrastructure.inbound.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioAutenticadoPrincipalTest {

    @Test
    @DisplayName("Conserva los datos del usuario con una copia inmutable de los roles")
    void keepsDataWithImmutableRoles() {
        // Arrange
        List<String> roles = new ArrayList<>(List.of("CLIENTE"));

        // Act
        UsuarioAutenticadoPrincipal principal = new UsuarioAutenticadoPrincipal(1L, "ana@test.com", roles);
        roles.add("ADMIN");

        // Assert
        assertEquals(1L, principal.getIdUsuario());
        assertEquals("ana@test.com", principal.getEmail());
        assertEquals(List.of("CLIENTE"), principal.getRoles());
        List<String> rolesExpuestos = principal.getRoles();
        assertThrows(UnsupportedOperationException.class, () -> rolesExpuestos.add("ADMIN"));
    }

    @Test
    @DisplayName("Roles nulos se convierten en una lista vacía")
    void nullRoles() {
        // Act
        UsuarioAutenticadoPrincipal principal = new UsuarioAutenticadoPrincipal(1L, "ana@test.com", null);

        // Assert
        assertTrue(principal.getRoles().isEmpty());
    }

    @Test
    @DisplayName("toString incluye id, correo y roles")
    void toStringIncludesData() {
        // Act
        String texto = new UsuarioAutenticadoPrincipal(1L, "ana@test.com", List.of("CLIENTE")).toString();

        // Assert
        assertEquals("UsuarioAutenticadoPrincipal{idUsuario=1, email='ana@test.com', roles=[CLIENTE]}", texto);
    }
}
