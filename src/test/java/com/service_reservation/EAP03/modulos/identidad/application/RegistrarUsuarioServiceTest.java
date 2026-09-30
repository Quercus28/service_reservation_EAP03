package com.service_reservation.EAP03.modulos.identidad.application;

import com.service_reservation.EAP03.modulos.identidad.application.exception.ReglaNegocioException;
import com.service_reservation.EAP03.modulos.identidad.domain.model.Usuario;
import com.service_reservation.EAP03.modulos.identidad.domain.model.UsuarioNuevoComando;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.out.PasswordEncoderPort;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.out.UsuarioRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrarUsuarioServiceTest {

    @Mock private UsuarioRepositoryPort usuarioRepository;
    @Mock private PasswordEncoderPort passwordEncoder;

    @InjectMocks private RegistrarUsuarioService service;

    private UsuarioNuevoComando comando(String rol) {
        return new UsuarioNuevoComando("pendienteback@gmail.com", "pendienteback", rol,
                "Nombre Apellido", "3001234567", "CC123456");
    }

    @Test
    @DisplayName("TC-01: Registro exitoso como Cliente crea el usuario con ROLE_CLIENTE y guarda su perfil de cliente")
    void registraClienteExitoso() {
        when(usuarioRepository.existePorEmail("pendienteback@gmail.com")).thenReturn(false);
        when(passwordEncoder.codificar("pendienteback")).thenReturn("hash-simulado");
        Usuario usuarioGuardado = new Usuario(1L, "pendienteback@gmail.com", "hash-simulado");
        when(usuarioRepository.guardarUsuarioConRol(any(Usuario.class), eq("ROLE_CLIENTE")))
                .thenReturn(usuarioGuardado);

        service.ejecutar(comando("CLIENTE"));

        verify(usuarioRepository).guardarUsuarioConRol(any(Usuario.class), eq("ROLE_CLIENTE"));
        verify(usuarioRepository).guardarPerfilCliente(1L, "Nombre Apellido", "3001234567", "CC123456");
        verify(usuarioRepository, never()).guardarPerfilProveedor(any(), any(), any(), any());
    }

    @Test
    @DisplayName("TC-02: Registro exitoso como Proveedor crea el usuario con ROLE_PROVEEDOR y guarda su perfil de proveedor")
    void registraProveedorExitoso() {
        when(usuarioRepository.existePorEmail("pendienteback@gmail.com")).thenReturn(false);
        when(passwordEncoder.codificar("pendienteback")).thenReturn("hash-simulado");
        Usuario usuarioGuardado = new Usuario(2L, "pendienteback@gmail.com", "hash-simulado");
        when(usuarioRepository.guardarUsuarioConRol(any(Usuario.class), eq("ROLE_PROVEEDOR")))
                .thenReturn(usuarioGuardado);

        service.ejecutar(comando("PROVEEDOR"));

        verify(usuarioRepository).guardarUsuarioConRol(any(Usuario.class), eq("ROLE_PROVEEDOR"));
        verify(usuarioRepository).guardarPerfilProveedor(2L, "Nombre Apellido", "3001234567", "CC123456");
        verify(usuarioRepository, never()).guardarPerfilCliente(any(), any(), any(), any());
    }

    @Test
    @DisplayName("TC-03: Rechaza el registro cuando el correo ya esta en uso, sin guardar nada")
    void rechazaCorreoExistente() {
        when(usuarioRepository.existePorEmail("pendienteback@gmail.com")).thenReturn(true);

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> service.ejecutar(comando("CLIENTE")));

        assertEquals("El correo ya está en uso", ex.getMessage());
        verify(usuarioRepository, never()).guardarUsuarioConRol(any(), any());
        verifyNoInteractions(passwordEncoder);
    }
}
