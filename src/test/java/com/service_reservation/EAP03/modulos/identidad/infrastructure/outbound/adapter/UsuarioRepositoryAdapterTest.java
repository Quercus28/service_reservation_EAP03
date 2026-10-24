package com.service_reservation.EAP03.modulos.identidad.infrastructure.outbound.adapter;

import com.service_reservation.EAP03.modulos.identidad.domain.model.Usuario;
import com.service_reservation.EAP03.modulos.identidad.infrastructure.outbound.persistence.entity.ClienteJpaEntity;
import com.service_reservation.EAP03.modulos.identidad.infrastructure.outbound.persistence.entity.ProveedorJpaEntity;
import com.service_reservation.EAP03.modulos.identidad.infrastructure.outbound.persistence.entity.RolJpaEntity;
import com.service_reservation.EAP03.modulos.identidad.infrastructure.outbound.persistence.entity.UsuarioJpaEntity;
import com.service_reservation.EAP03.modulos.identidad.infrastructure.outbound.persistence.entity.UsuarioRolJpaEntity;
import com.service_reservation.EAP03.modulos.identidad.infrastructure.outbound.persistence.repository.ClienteSpringDataRepository;
import com.service_reservation.EAP03.modulos.identidad.infrastructure.outbound.persistence.repository.ProveedorSpringDataRepository;
import com.service_reservation.EAP03.modulos.identidad.infrastructure.outbound.persistence.repository.RolSpringDataRepository;
import com.service_reservation.EAP03.modulos.identidad.infrastructure.outbound.persistence.repository.UsuarioRolSpringDataRepository;
import com.service_reservation.EAP03.modulos.identidad.infrastructure.outbound.persistence.repository.UsuarioSpringDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioRepositoryAdapterTest {

    @Mock private UsuarioSpringDataRepository usuarioJpaRepo;
    @Mock private RolSpringDataRepository rolJpaRepo;
    @Mock private UsuarioRolSpringDataRepository usuarioRolJpaRepo;
    @Mock private ClienteSpringDataRepository clienteJpaRepo;
    @Mock private ProveedorSpringDataRepository proveedorJpaRepo;

    private UsuarioRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new UsuarioRepositoryAdapter(usuarioJpaRepo, rolJpaRepo, usuarioRolJpaRepo, clienteJpaRepo, proveedorJpaRepo);
    }

    private static RolJpaEntity role(Long id, String nombre) {
        RolJpaEntity rol = new RolJpaEntity();
        rol.setId(id);
        rol.setNombre(nombre);
        return rol;
    }

    private static UsuarioJpaEntity userEntity(Long id, RolJpaEntity... roles) {
        UsuarioJpaEntity entity = new UsuarioJpaEntity();
        entity.setId(id);
        entity.setEmail("ana@test.com");
        entity.setPasswordHash("hash");
        entity.setEnabled(true);
        entity.setSecret2fa("SECRETO");
        entity.set2faEnabled(true);
        entity.setRoles(new HashSet<>(Arrays.asList(roles)));
        return entity;
    }

    // ---------------------------------------------------------------- Consultas de usuario

    @Test
    @DisplayName("existePorEmail delega en la búsqueda sin distinguir mayúsculas")
    void existsByEmail() {
        when(usuarioJpaRepo.existsByEmailIgnoreCase("ana@test.com")).thenReturn(true);

        assertTrue(adapter.existePorEmail("ana@test.com"));
    }

    @Test
    @DisplayName("buscarPorEmail mapea todos los datos y normaliza los nombres de rol")
    void findByEmailMapsUser() {
        // Arrange
        UsuarioJpaEntity entity = userEntity(1L, role(1L, "ROLE_ADMIN"), role(2L, " CLIENTE "), null);
        when(usuarioJpaRepo.findFirstByEmailIgnoreCaseOrderByIdAsc("ana@test.com")).thenReturn(Optional.of(entity));

        // Act
        Usuario usuario = adapter.buscarPorEmail("ana@test.com").orElseThrow();

        // Assert
        assertEquals(1L, usuario.getId());
        assertEquals("ana@test.com", usuario.getEmail());
        assertEquals("hash", usuario.getPasswordHash());
        assertTrue(usuario.isEnabled());
        assertEquals(Set.of("ROLE_ADMIN", "ROLE_CLIENTE"), usuario.getRoles());
        assertEquals("SECRETO", usuario.getSecret2fa());
        assertTrue(usuario.is2faEnabled());
    }

    @Test
    @DisplayName("buscarPorEmail devuelve vacío cuando el correo no existe")
    void findByEmailNotFound() {
        when(usuarioJpaRepo.findFirstByEmailIgnoreCaseOrderByIdAsc("x@test.com")).thenReturn(Optional.empty());

        assertTrue(adapter.buscarPorEmail("x@test.com").isEmpty());
    }

    @Test
    @DisplayName("buscarPorId mapea el usuario y conserva un rol sin nombre como nulo")
    void findByIdMapsUser() {
        // Arrange
        when(usuarioJpaRepo.findById(1L)).thenReturn(Optional.of(userEntity(1L, role(3L, null))));

        // Act
        Usuario usuario = adapter.buscarPorId(1L).orElseThrow();

        // Assert
        assertEquals(1, usuario.getRoles().size());
        assertTrue(usuario.getRoles().contains(null));
    }

    @Test
    @DisplayName("buscarPorId con id nulo devuelve vacío sin consultar la base de datos")
    void findByNullId() {
        assertTrue(adapter.buscarPorId(null).isEmpty());
        verifyNoInteractions(usuarioJpaRepo);
    }

    // ---------------------------------------------------------------- Registro

    @Test
    @DisplayName("Guarda un cliente con su rol activo desde el inicio")
    void savesClientWithActiveRole() {
        // Arrange
        when(rolJpaRepo.findByNombre("ROLE_CLIENTE")).thenReturn(Optional.of(role(2L, "ROLE_CLIENTE")));
        when(usuarioJpaRepo.save(any(UsuarioJpaEntity.class))).thenAnswer(inv -> {
            UsuarioJpaEntity entity = inv.getArgument(0);
            entity.setId(10L);
            return entity;
        });

        // Act
        Usuario guardado = adapter.guardarUsuarioConRol(new Usuario("ana@test.com", "hash"), "CLIENTE");

        // Assert
        assertEquals(10L, guardado.getId());
        assertEquals("ana@test.com", guardado.getEmail());
        assertEquals("hash", guardado.getPasswordHash());
        ArgumentCaptor<UsuarioJpaEntity> usuarioCaptor = ArgumentCaptor.forClass(UsuarioJpaEntity.class);
        verify(usuarioJpaRepo).save(usuarioCaptor.capture());
        assertTrue(usuarioCaptor.getValue().isEnabled());
        assertNotNull(usuarioCaptor.getValue().getCreatedAt());
        ArgumentCaptor<UsuarioRolJpaEntity> rolCaptor = ArgumentCaptor.forClass(UsuarioRolJpaEntity.class);
        verify(usuarioRolJpaRepo).save(rolCaptor.capture());
        assertEquals(10L, rolCaptor.getValue().getIdUsuario());
        assertEquals(2L, rolCaptor.getValue().getIdRol());
        assertTrue(rolCaptor.getValue().isActivo());
    }

    @Test
    @DisplayName("Guarda un proveedor con su rol inactivo hasta la aprobación del administrador")
    void savesProviderWithInactiveRole() {
        // Arrange
        when(rolJpaRepo.findByNombre("ROLE_PROVEEDOR")).thenReturn(Optional.of(role(3L, "ROLE_PROVEEDOR")));
        when(usuarioJpaRepo.save(any(UsuarioJpaEntity.class))).thenAnswer(inv -> {
            UsuarioJpaEntity entity = inv.getArgument(0);
            entity.setId(11L);
            return entity;
        });

        // Act
        adapter.guardarUsuarioConRol(new Usuario("prov@test.com", "hash"), "ROLE_PROVEEDOR");

        // Assert
        ArgumentCaptor<UsuarioRolJpaEntity> rolCaptor = ArgumentCaptor.forClass(UsuarioRolJpaEntity.class);
        verify(usuarioRolJpaRepo).save(rolCaptor.capture());
        assertFalse(rolCaptor.getValue().isActivo());
    }

    @Test
    @DisplayName("Falla sin guardar nada cuando el rol no existe en la base de datos")
    void unknownRole() {
        // Arrange
        when(rolJpaRepo.findByNombre("ROLE_OTRO")).thenReturn(Optional.empty());
        Usuario usuario = new Usuario("ana@test.com", "hash");

        // Act
        RuntimeException ex = assertThrows(RuntimeException.class, () -> adapter.guardarUsuarioConRol(usuario, "OTRO"));

        // Assert
        assertEquals("Rol no encontrado en BD: ROLE_OTRO", ex.getMessage());
        verify(usuarioJpaRepo, never()).save(any());
        verifyNoInteractions(usuarioRolJpaRepo);
    }

    @Test
    @DisplayName("Guarda el perfil de cliente con sus datos")
    void savesClientProfile() {
        // Act
        adapter.guardarPerfilCliente(10L, "Ana", "3001234567", "CC1");

        // Assert
        ArgumentCaptor<ClienteJpaEntity> captor = ArgumentCaptor.forClass(ClienteJpaEntity.class);
        verify(clienteJpaRepo).save(captor.capture());
        assertEquals(10L, captor.getValue().getIdUsuario());
        assertEquals("Ana", captor.getValue().getNombre());
        assertEquals("3001234567", captor.getValue().getTelefono());
        assertEquals("CC1", captor.getValue().getDocumento());
    }

    @Test
    @DisplayName("Guarda el perfil de proveedor con sus datos y la fecha de creación")
    void savesProviderProfile() {
        // Act
        adapter.guardarPerfilProveedor(11L, "Empresa SAS", "6041234567", "900123-1");

        // Assert
        ArgumentCaptor<ProveedorJpaEntity> captor = ArgumentCaptor.forClass(ProveedorJpaEntity.class);
        verify(proveedorJpaRepo).save(captor.capture());
        assertEquals(11L, captor.getValue().getIdUsuario());
        assertEquals("Empresa SAS", captor.getValue().getRazonSocial());
        assertEquals("6041234567", captor.getValue().getTelefono());
        assertEquals("900123-1", captor.getValue().getNitRut());
        assertNotNull(captor.getValue().getCreatedAt());
    }

    // ---------------------------------------------------------------- Perfiles

    @Test
    @DisplayName("buscarIdProveedorPorUsuario devuelve el id del perfil de proveedor")
    void findsProviderId() {
        // Arrange
        ProveedorJpaEntity proveedor = new ProveedorJpaEntity();
        proveedor.setId(5L);
        when(proveedorJpaRepo.findByIdUsuario(11L)).thenReturn(Optional.of(proveedor));
        when(proveedorJpaRepo.findByIdUsuario(12L)).thenReturn(Optional.empty());

        // Act & Assert
        assertEquals(Optional.of(5), adapter.buscarIdProveedorPorUsuario(11L));
        assertTrue(adapter.buscarIdProveedorPorUsuario(12L).isEmpty());
    }

    @Test
    @DisplayName("buscarIdProveedorPorUsuario con id nulo devuelve vacío sin consultar")
    void providerIdWithNullUser() {
        assertTrue(adapter.buscarIdProveedorPorUsuario(null).isEmpty());
        verifyNoInteractions(proveedorJpaRepo);
    }

    @Test
    @DisplayName("buscarIdClientePorUsuario devuelve el id del perfil de cliente")
    void findsClientId() {
        // Arrange
        ClienteJpaEntity cliente = new ClienteJpaEntity();
        cliente.setId(3L);
        when(clienteJpaRepo.findByIdUsuario(10L)).thenReturn(Optional.of(cliente));
        when(clienteJpaRepo.findByIdUsuario(12L)).thenReturn(Optional.empty());

        // Act & Assert
        assertEquals(Optional.of(3L), adapter.buscarIdClientePorUsuario(10L));
        assertTrue(adapter.buscarIdClientePorUsuario(12L).isEmpty());
    }

    @Test
    @DisplayName("buscarIdClientePorUsuario con id nulo devuelve vacío sin consultar")
    void clientIdWithNullUser() {
        assertTrue(adapter.buscarIdClientePorUsuario(null).isEmpty());
        verifyNoInteractions(clienteJpaRepo);
    }

    // ---------------------------------------------------------------- 2FA

    @Test
    @DisplayName("guardarSecret2fa guarda el secreto del usuario existente")
    void savesSecret() {
        // Arrange
        UsuarioJpaEntity entity = userEntity(1L);
        when(usuarioJpaRepo.findById(1L)).thenReturn(Optional.of(entity));

        // Act
        adapter.guardarSecret2fa(1L, "NUEVO");

        // Assert
        assertEquals("NUEVO", entity.getSecret2fa());
        verify(usuarioJpaRepo).save(entity);
    }

    @Test
    @DisplayName("activar2fa marca el 2FA como activo")
    void enables2fa() {
        // Arrange
        UsuarioJpaEntity entity = userEntity(1L);
        entity.set2faEnabled(false);
        when(usuarioJpaRepo.findById(1L)).thenReturn(Optional.of(entity));

        // Act
        adapter.activar2fa(1L);

        // Assert
        assertTrue(entity.is2faEnabled());
        verify(usuarioJpaRepo).save(entity);
    }

    @Test
    @DisplayName("desactivar2fa desactiva el 2FA y borra el secreto")
    void disables2fa() {
        // Arrange
        UsuarioJpaEntity entity = userEntity(1L);
        when(usuarioJpaRepo.findById(1L)).thenReturn(Optional.of(entity));

        // Act
        adapter.desactivar2fa(1L);

        // Assert
        assertFalse(entity.is2faEnabled());
        assertNull(entity.getSecret2fa());
        verify(usuarioJpaRepo).save(entity);
    }

    @Test
    @DisplayName("Las operaciones de 2FA no guardan nada si el usuario no existe")
    void twoFactorOperationsWithUnknownUser() {
        // Arrange
        when(usuarioJpaRepo.findById(99L)).thenReturn(Optional.empty());

        // Act
        adapter.guardarSecret2fa(99L, "NUEVO");
        adapter.activar2fa(99L);
        adapter.desactivar2fa(99L);

        // Assert
        verify(usuarioJpaRepo, never()).save(any());
    }

    @Test
    @DisplayName("Las operaciones de 2FA con id nulo no consultan la base de datos")
    void twoFactorOperationsWithNullId() {
        // Act
        adapter.guardarSecret2fa(null, "NUEVO");
        adapter.activar2fa(null);
        adapter.desactivar2fa(null);

        // Assert
        verifyNoInteractions(usuarioJpaRepo);
    }
}
