package com.service_reservation.EAP03.modulos.identidad.infrastructure.outbound.persistence.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class IdentityJpaEntitiesTest {

    @Test
    @DisplayName("UsuarioJpaEntity guarda y devuelve todos sus campos")
    void userEntity() {
        // Arrange
        UsuarioJpaEntity usuario = new UsuarioJpaEntity();
        LocalDateTime creado = LocalDateTime.now();
        RolJpaEntity rol = new RolJpaEntity();
        rol.setNombre("ROLE_CLIENTE");

        // Act
        usuario.setId(1L);
        usuario.setEmail("ana@test.com");
        usuario.setPasswordHash("hash");
        usuario.setEnabled(true);
        usuario.setCreatedAt(creado);
        usuario.setSecret2fa("SECRETO");
        usuario.set2faEnabled(true);
        usuario.addRol(rol);

        // Assert
        assertEquals(1L, usuario.getId());
        assertEquals("ana@test.com", usuario.getEmail());
        assertEquals("hash", usuario.getPasswordHash());
        assertTrue(usuario.isEnabled());
        assertEquals(creado, usuario.getCreatedAt());
        assertEquals("SECRETO", usuario.getSecret2fa());
        assertTrue(usuario.is2faEnabled());
        assertEquals(Set.of(rol), usuario.getRoles());
    }

    @Test
    @DisplayName("UsuarioJpaEntity permite reemplazar el conjunto de roles")
    void userEntityReplacesRoles() {
        // Arrange
        UsuarioJpaEntity usuario = new UsuarioJpaEntity();
        Set<RolJpaEntity> roles = new HashSet<>();

        // Act
        usuario.setRoles(roles);

        // Assert
        assertSame(roles, usuario.getRoles());
    }

    @Test
    @DisplayName("RolJpaEntity guarda y devuelve sus campos")
    void roleEntity() {
        // Arrange
        RolJpaEntity rol = new RolJpaEntity();

        // Act
        rol.setId(2L);
        rol.setNombre("ROLE_ADMIN");

        // Assert
        assertEquals(2L, rol.getId());
        assertEquals("ROLE_ADMIN", rol.getNombre());
    }

    @Test
    @DisplayName("ClienteJpaEntity guarda y devuelve todos sus campos")
    void clientEntity() {
        // Arrange
        ClienteJpaEntity cliente = new ClienteJpaEntity();

        // Act
        cliente.setId(3L);
        cliente.setIdUsuario(10L);
        cliente.setNombre("Ana");
        cliente.setTelefono("3001234567");
        cliente.setDocumento("CC1");

        // Assert
        assertEquals(3L, cliente.getId());
        assertEquals(10L, cliente.getIdUsuario());
        assertEquals("Ana", cliente.getNombre());
        assertEquals("3001234567", cliente.getTelefono());
        assertEquals("CC1", cliente.getDocumento());
    }

    @Test
    @DisplayName("ProveedorJpaEntity guarda y devuelve todos sus campos")
    void providerEntity() {
        // Arrange
        ProveedorJpaEntity proveedor = new ProveedorJpaEntity();
        LocalDateTime creado = LocalDateTime.now();

        // Act
        proveedor.setId(5L);
        proveedor.setIdUsuario(11L);
        proveedor.setRazonSocial("Empresa SAS");
        proveedor.setTelefono("6041234567");
        proveedor.setNitRut("900123-1");
        proveedor.setCreatedAt(creado);

        // Assert
        assertEquals(5L, proveedor.getId());
        assertEquals(11L, proveedor.getIdUsuario());
        assertEquals("Empresa SAS", proveedor.getRazonSocial());
        assertEquals("6041234567", proveedor.getTelefono());
        assertEquals("900123-1", proveedor.getNitRut());
        assertEquals(creado, proveedor.getCreatedAt());
    }

    @Test
    @DisplayName("UsuarioRolJpaEntity guarda usuario, rol y estado activo")
    void userRoleEntity() {
        // Act
        UsuarioRolJpaEntity usuarioRol = new UsuarioRolJpaEntity(10L, 2L, false);

        // Assert
        assertEquals(10L, usuarioRol.getIdUsuario());
        assertEquals(2L, usuarioRol.getIdRol());
        assertFalse(usuarioRol.isActivo());
    }

    @Test
    @DisplayName("UsuarioRolJpaEntity tiene un constructor vacío para JPA")
    void emptyUserRoleEntity() {
        // Act
        UsuarioRolJpaEntity usuarioRol = new UsuarioRolJpaEntity();

        // Assert
        assertNull(usuarioRol.getIdUsuario());
        assertNull(usuarioRol.getIdRol());
        assertFalse(usuarioRol.isActivo());
    }

    @Test
    @DisplayName("La clave compuesta de UsuarioRol es igual solo si coinciden usuario y rol")
    void compositeKeyEquality() {
        // Arrange
        UsuarioRolJpaEntity.Id clave = new UsuarioRolJpaEntity.Id(10L, 2L);

        // Act
        boolean igualASiMisma = clave.equals(clave);
        boolean igualANulo = clave.equals(null);
        boolean igualAOtroTipo = clave.equals("10-2");

        // Assert
        assertTrue(igualASiMisma);
        assertEquals(clave, new UsuarioRolJpaEntity.Id(10L, 2L));
        assertEquals(clave.hashCode(), new UsuarioRolJpaEntity.Id(10L, 2L).hashCode());
        assertNotEquals(clave, new UsuarioRolJpaEntity.Id(11L, 2L));
        assertNotEquals(clave, new UsuarioRolJpaEntity.Id(10L, 3L));
        assertFalse(igualANulo);
        assertFalse(igualAOtroTipo);
    }

    @Test
    @DisplayName("Dos claves compuestas vacías son iguales")
    void emptyCompositeKeys() {
        assertEquals(new UsuarioRolJpaEntity.Id(), new UsuarioRolJpaEntity.Id());
    }
}
