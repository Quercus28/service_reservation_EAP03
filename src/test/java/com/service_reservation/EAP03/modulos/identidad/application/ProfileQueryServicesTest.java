package com.service_reservation.EAP03.modulos.identidad.application;

import com.service_reservation.EAP03.modulos.identidad.domain.ports.out.UsuarioRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileQueryServicesTest {

    @Mock private UsuarioRepositoryPort usuarioRepository;

    @Test
    @DisplayName("Consultar cliente devuelve el id del perfil de cliente del usuario")
    void findsClientId() {
        // Arrange
        when(usuarioRepository.buscarIdClientePorUsuario(10L)).thenReturn(Optional.of(3L));
        when(usuarioRepository.buscarIdClientePorUsuario(11L)).thenReturn(Optional.empty());
        ConsultarClienteService service = new ConsultarClienteService(usuarioRepository);

        // Act & Assert
        assertEquals(Optional.of(3L), service.buscarIdClientePorUsuario(10L));
        assertTrue(service.buscarIdClientePorUsuario(11L).isEmpty());
    }

    @Test
    @DisplayName("Consultar proveedor devuelve el id del perfil de proveedor del usuario")
    void findsProviderId() {
        // Arrange
        when(usuarioRepository.buscarIdProveedorPorUsuario(10L)).thenReturn(Optional.of(5));
        when(usuarioRepository.buscarIdProveedorPorUsuario(11L)).thenReturn(Optional.empty());
        ConsultarProveedorService service = new ConsultarProveedorService(usuarioRepository);

        // Act & Assert
        assertEquals(Optional.of(5), service.buscarIdProveedorPorUsuario(10L));
        assertTrue(service.buscarIdProveedorPorUsuario(11L).isEmpty());
    }
}
