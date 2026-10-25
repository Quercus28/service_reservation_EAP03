package com.service_reservation.EAP03.modulos.identidad.application;

import com.service_reservation.EAP03.modulos.identidad.application.exception.ReglaNegocioException;
import com.service_reservation.EAP03.modulos.identidad.domain.events.UsuarioRegistradoEvent;
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
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class RegistrarUsuarioServiceTest {

    @Mock private UsuarioRepositoryPort usuarioRepository;
    @Mock private PasswordEncoderPort passwordEncoder;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks private RegistrarUsuarioService service;

    private UsuarioNuevoComando command(String rol) {
        return new UsuarioNuevoComando("pendienteback@gmail.com", "pendienteback", rol,
                "Nombre Apellido", "3001234567", "CC123456");
    }

    @Test
    @DisplayName("TC-01: Registro exitoso como Cliente crea el usuario con ROLE_CLIENTE y guarda su perfil de cliente")
    void registersClient() {
        when(usuarioRepository.existePorEmail("pendienteback@gmail.com")).thenReturn(false);
        when(passwordEncoder.codificar("pendienteback")).thenReturn("hash-simulado");
        Usuario usuarioGuardado = new Usuario(1L, "pendienteback@gmail.com", "hash-simulado");
        when(usuarioRepository.guardarUsuarioConRol(any(Usuario.class), eq("ROLE_CLIENTE")))
                .thenReturn(usuarioGuardado);

        service.ejecutar(command("CLIENTE"));

        verify(usuarioRepository).guardarUsuarioConRol(any(Usuario.class), eq("ROLE_CLIENTE"));
        verify(usuarioRepository).guardarPerfilCliente(1L, "Nombre Apellido", "3001234567", "CC123456");
        verify(usuarioRepository, never()).guardarPerfilProveedor(any(), any(), any(), any());
    }

    @Test
    @DisplayName("TC-02: Registro exitoso como Proveedor crea el usuario con ROLE_PROVEEDOR y guarda su perfil de proveedor")
    void registersProvider() {
        when(usuarioRepository.existePorEmail("pendienteback@gmail.com")).thenReturn(false);
        when(passwordEncoder.codificar("pendienteback")).thenReturn("hash-simulado");
        Usuario usuarioGuardado = new Usuario(2L, "pendienteback@gmail.com", "hash-simulado");
        when(usuarioRepository.guardarUsuarioConRol(any(Usuario.class), eq("ROLE_PROVEEDOR")))
                .thenReturn(usuarioGuardado);

        service.ejecutar(command("PROVEEDOR"));

        verify(usuarioRepository).guardarUsuarioConRol(any(Usuario.class), eq("ROLE_PROVEEDOR"));
        verify(usuarioRepository).guardarPerfilProveedor(2L, "Nombre Apellido", "3001234567", "CC123456");
        verify(usuarioRepository, never()).guardarPerfilCliente(any(), any(), any(), any());
    }

    @Test
    @DisplayName("TC-03: Rechaza el registro cuando el correo ya esta en uso, sin guardar nada")
    void rejectsExistingEmail() {
        when(usuarioRepository.existePorEmail("pendienteback@gmail.com")).thenReturn(true);

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> service.ejecutar(command("CLIENTE")));

        assertEquals("El correo ya está en uso", ex.getMessage());
        verify(usuarioRepository, never()).guardarUsuarioConRol(any(), any());
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    @DisplayName("TC-14: Guarda y compara el correo en minúsculas y sin espacios")
    void normalizesEmailOnRegistration() {
        // Arrange
        UsuarioNuevoComando comando = new UsuarioNuevoComando("  Test@X.com ", "pendienteback", "CLIENTE",
                "Nombre Apellido", "3001234567", "CC123456");
        when(usuarioRepository.existePorEmail("test@x.com")).thenReturn(false);
        when(passwordEncoder.codificar("pendienteback")).thenReturn("hash-simulado");
        when(usuarioRepository.guardarUsuarioConRol(any(Usuario.class), eq("ROLE_CLIENTE")))
                .thenReturn(new Usuario(1L, "test@x.com", "hash-simulado"));

        // Act
        service.ejecutar(comando);

        // Assert
        verify(usuarioRepository).existePorEmail("test@x.com");
        verify(usuarioRepository).guardarUsuarioConRol(
                argThat(usuario -> usuario.getEmail().equals("test@x.com")), eq("ROLE_CLIENTE"));
    }

    @Test
    @DisplayName("TC-14: Rechaza la variante con mayúsculas de un correo ya registrado")
    void rejectsUppercaseVariant() {
        // Arrange
        UsuarioNuevoComando comando = new UsuarioNuevoComando("PendienteBack@Gmail.com", "pendienteback", "CLIENTE",
                "Nombre Apellido", "3001234567", "CC123456");
        when(usuarioRepository.existePorEmail("pendienteback@gmail.com")).thenReturn(true);

        // Act
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () -> service.ejecutar(comando));

        // Assert
        assertEquals("El correo ya está en uso", ex.getMessage());
        verify(usuarioRepository, never()).guardarUsuarioConRol(any(), any());
    }

    @Test
    @DisplayName("Registro con un rol sin perfil asociado no guarda perfil de cliente ni de proveedor")
    void registersRoleWithoutProfile() {
        // Arrange
        when(usuarioRepository.existePorEmail("pendienteback@gmail.com")).thenReturn(false);
        when(passwordEncoder.codificar("pendienteback")).thenReturn("hash-simulado");
        when(usuarioRepository.guardarUsuarioConRol(any(Usuario.class), eq("ROLE_ADMIN")))
                .thenReturn(new Usuario(3L, "pendienteback@gmail.com", "hash-simulado"));

        // Act
        service.ejecutar(command("ADMIN"));

        // Assert
        verify(usuarioRepository, never()).guardarPerfilCliente(any(), any(), any(), any());
        verify(usuarioRepository, never()).guardarPerfilProveedor(any(), any(), any(), any());
        verify(eventPublisher).publishEvent(new UsuarioRegistradoEvent(3L, "ROLE_ADMIN"));
    }

    @Test
    @DisplayName("Guarda el perfil de cliente de un usuario ya registrado")
    void savesClientProfile() {
        service.guardarPerfilCliente(1L, "Nombre Apellido", "3001234567", "CC123456");

        verify(usuarioRepository).guardarPerfilCliente(1L, "Nombre Apellido", "3001234567", "CC123456");
    }

    @Test
    @DisplayName("Guarda el perfil de proveedor de un usuario ya registrado")
    void savesProviderProfile() {
        service.guardarPerfilProveedor(2L, "Empresa SAS", "3001234567", "900123-1");

        verify(usuarioRepository).guardarPerfilProveedor(2L, "Empresa SAS", "3001234567", "900123-1");
    }
}
