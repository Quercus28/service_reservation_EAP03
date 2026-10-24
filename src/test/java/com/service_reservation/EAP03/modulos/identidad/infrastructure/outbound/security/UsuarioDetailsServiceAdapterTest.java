package com.service_reservation.EAP03.modulos.identidad.infrastructure.outbound.security;

import com.service_reservation.EAP03.modulos.identidad.infrastructure.outbound.persistence.entity.RolJpaEntity;
import com.service_reservation.EAP03.modulos.identidad.infrastructure.outbound.persistence.entity.UsuarioJpaEntity;
import com.service_reservation.EAP03.modulos.identidad.infrastructure.outbound.persistence.repository.UsuarioSpringDataRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioDetailsServiceAdapterTest {

    @Mock private UsuarioSpringDataRepository usuarioRepository;

    @InjectMocks private UsuarioDetailsServiceAdapter adapter;

    private static UsuarioJpaEntity user(boolean habilitado) {
        RolJpaEntity rol = new RolJpaEntity();
        rol.setNombre("ROLE_CLIENTE");
        UsuarioJpaEntity entity = new UsuarioJpaEntity();
        entity.setEmail("ana@test.com");
        entity.setPasswordHash("hash");
        entity.setEnabled(habilitado);
        entity.addRol(rol);
        return entity;
    }

    @Test
    @DisplayName("Carga un usuario habilitado con sus roles como autoridades")
    void loadsEnabledUser() {
        // Arrange
        when(usuarioRepository.findByEmail("ana@test.com")).thenReturn(Optional.of(user(true)));

        // Act
        UserDetails detalles = adapter.loadUserByUsername("ana@test.com");

        // Assert
        assertEquals("ana@test.com", detalles.getUsername());
        assertEquals("hash", detalles.getPassword());
        assertTrue(detalles.isEnabled());
        Set<String> autoridades = detalles.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
        assertEquals(Set.of("ROLE_CLIENTE"), autoridades);
    }

    @Test
    @DisplayName("Carga un usuario inhabilitado marcándolo como deshabilitado")
    void loadsDisabledUser() {
        // Arrange
        when(usuarioRepository.findByEmail("ana@test.com")).thenReturn(Optional.of(user(false)));

        // Act
        UserDetails detalles = adapter.loadUserByUsername("ana@test.com");

        // Assert
        assertFalse(detalles.isEnabled());
    }

    @Test
    @DisplayName("Lanza UsernameNotFoundException cuando el usuario no existe")
    void unknownUser() {
        // Arrange
        when(usuarioRepository.findByEmail("x@test.com")).thenReturn(Optional.empty());

        // Act
        UsernameNotFoundException ex = assertThrows(UsernameNotFoundException.class,
                () -> adapter.loadUserByUsername("x@test.com"));

        // Assert
        assertEquals("Usuario no encontrado", ex.getMessage());
    }
}
