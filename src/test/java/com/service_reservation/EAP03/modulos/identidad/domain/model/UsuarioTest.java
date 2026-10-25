package com.service_reservation.EAP03.modulos.identidad.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioTest {

    @Test
    @DisplayName("Un usuario nuevo queda habilitado, sin id, sin roles y sin 2FA")
    void newUserHasDefaults() {
        // Act
        Usuario usuario = new Usuario("ana@test.com", "hash");

        // Assert
        assertNull(usuario.getId());
        assertEquals("ana@test.com", usuario.getEmail());
        assertEquals("hash", usuario.getPasswordHash());
        assertTrue(usuario.isEnabled());
        assertTrue(usuario.getRoles().isEmpty());
        assertNull(usuario.getSecret2fa());
        assertFalse(usuario.is2faEnabled());
    }

    @Test
    @DisplayName("El constructor con id conserva el id y los valores por defecto")
    void userWithIdKeepsDefaults() {
        // Act
        Usuario usuario = new Usuario(4L, "ana@test.com", "hash");

        // Assert
        assertEquals(4L, usuario.getId());
        assertTrue(usuario.isEnabled());
        assertTrue(usuario.getRoles().isEmpty());
    }

    @Test
    @DisplayName("El constructor con estado y roles hace una copia de los roles")
    void copiesRoles() {
        // Arrange
        Set<String> roles = new HashSet<>(Set.of("ROLE_CLIENTE"));

        // Act
        Usuario usuario = new Usuario(4L, "ana@test.com", "hash", false, roles);
        roles.add("ROLE_ADMIN");

        // Assert
        assertFalse(usuario.isEnabled());
        assertEquals(Set.of("ROLE_CLIENTE"), usuario.getRoles());
    }

    @Test
    @DisplayName("Roles nulos se convierten en un conjunto vacío")
    void nullRolesBecomeEmptySet() {
        // Act
        Usuario usuario = new Usuario(4L, "ana@test.com", "hash", true, null);

        // Assert
        assertTrue(usuario.getRoles().isEmpty());
    }

    @Test
    @DisplayName("Los roles expuestos no se pueden modificar")
    void rolesAreUnmodifiable() {
        // Arrange
        Usuario usuario = new Usuario(4L, "ana@test.com", "hash", true, Set.of("ROLE_CLIENTE"));
        Set<String> roles = usuario.getRoles();

        // Act & Assert
        assertThrows(UnsupportedOperationException.class, () -> roles.add("ROLE_ADMIN"));
    }

    @Test
    @DisplayName("El constructor completo conserva los datos de 2FA")
    void fullConstructorKeeps2faData() {
        // Act
        Usuario usuario = new Usuario(4L, "ana@test.com", "hash", true, Set.of("ROLE_ADMIN"), "SECRETO", true);

        // Assert
        assertEquals(4L, usuario.getId());
        assertEquals(Set.of("ROLE_ADMIN"), usuario.getRoles());
        assertEquals("SECRETO", usuario.getSecret2fa());
        assertTrue(usuario.is2faEnabled());
    }
}
