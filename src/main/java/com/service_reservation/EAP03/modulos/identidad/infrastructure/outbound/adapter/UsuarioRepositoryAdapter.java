package com.service_reservation.EAP03.modulos.identidad.infrastructure.outbound.adapter;

import com.service_reservation.EAP03.modulos.identidad.domain.model.Usuario;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.out.UsuarioRepositoryPort;
import com.service_reservation.EAP03.modulos.identidad.infrastructure.outbound.persistence.entity.*;
import com.service_reservation.EAP03.modulos.identidad.infrastructure.outbound.persistence.repository.*;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UsuarioRepositoryAdapter implements UsuarioRepositoryPort {

    private final UsuarioSpringDataRepository usuarioJpaRepo;
    private final RolSpringDataRepository rolJpaRepo;
    private final ClienteSpringDataRepository clienteJpaRepo;
    private final ProveedorSpringDataRepository proveedorJpaRepo;

    public UsuarioRepositoryAdapter(UsuarioSpringDataRepository usuarioJpaRepo, 
                                    RolSpringDataRepository rolJpaRepo, 
                                    ClienteSpringDataRepository clienteJpaRepo, 
                                    ProveedorSpringDataRepository proveedorJpaRepo) {
        this.usuarioJpaRepo = usuarioJpaRepo;
        this.rolJpaRepo = rolJpaRepo;
        this.clienteJpaRepo = clienteJpaRepo;
        this.proveedorJpaRepo = proveedorJpaRepo;
    }

    @Override
    public boolean existePorEmail(String email) {
        return usuarioJpaRepo.existsByEmail(email);
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioJpaRepo.findByEmail(email)
                .map(this::mapearAUsuario);
    }

    @Override
    public Optional<Usuario> buscarPorId(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return usuarioJpaRepo.findById(id)
                .map(this::mapearAUsuario);
    }

    private Usuario mapearAUsuario(UsuarioJpaEntity entity) {
        Set<String> roles = entity.getRoles().stream()
                .filter(Objects::nonNull)
                .map(rol -> normalizarNombreRol(rol.getNombre()))
                .collect(Collectors.toSet());
        return new Usuario(
                entity.getId(),
                entity.getEmail(),
                entity.getPasswordHash(),
                entity.isEnabled(),
                roles,
                entity.getSecret2fa(),
                entity.is2faEnabled()
        );
    }

    @Override
    public Usuario guardarUsuarioConRol(Usuario usuario, String nombreRol) {
        UsuarioJpaEntity usuarioEntity = new UsuarioJpaEntity();
        usuarioEntity.setEmail(usuario.getEmail());
        usuarioEntity.setPasswordHash(usuario.getPasswordHash());
        usuarioEntity.setEnabled(true);
        usuarioEntity.setCreatedAt(LocalDateTime.now());

        final String rolBuscado = normalizarNombreRol(nombreRol);

        RolJpaEntity rolEntity = rolJpaRepo.findByNombre(rolBuscado)
            .orElseThrow(() -> new RuntimeException("Rol no encontrado en BD: " + rolBuscado));

        usuarioEntity.addRol(rolEntity);

        UsuarioJpaEntity guardado = usuarioJpaRepo.save(usuarioEntity);
        return new Usuario(guardado.getId(), guardado.getEmail(), guardado.getPasswordHash());
    }

    @Override
    public Usuario guardarAdministrador(Usuario usuario) {
        UsuarioJpaEntity usuarioEntity = new UsuarioJpaEntity();
        usuarioEntity.setEmail(usuario.getEmail());
        usuarioEntity.setPasswordHash(usuario.getPasswordHash());
        usuarioEntity.setEnabled(true);
        usuarioEntity.set2faEnabled(true);
        usuarioEntity.setCreatedAt(LocalDateTime.now());

        RolJpaEntity rolAdmin = rolJpaRepo.findByNombre("ROLE_ADMIN")
            .orElseThrow(() -> new RuntimeException("Rol no encontrado en BD: ROLE_ADMIN"));
        usuarioEntity.addRol(rolAdmin);

        UsuarioJpaEntity guardado = usuarioJpaRepo.save(usuarioEntity);
        return new Usuario(guardado.getId(), guardado.getEmail(), guardado.getPasswordHash());
    }

    private String normalizarNombreRol(String nombreRol) {
        if (nombreRol == null) {
            return null;
        }

        String rol = nombreRol.trim();
        if (rol.startsWith("ROLE_")) {
            return rol;
        }

        return "ROLE_" + rol;
    }

    @Override
    public void guardarPerfilCliente(Long idUsuario, String nombre, String telefono, String documento) {
        ClienteJpaEntity cliente = new ClienteJpaEntity();
        cliente.setIdUsuario(idUsuario);
        cliente.setNombre(nombre);
        cliente.setTelefono(telefono);
        cliente.setDocumento(documento);
        clienteJpaRepo.save(cliente);
    }

    @Override
    public Optional<Integer> buscarIdProveedorPorUsuario(Long idUsuario) {
        if (idUsuario == null) {
            return Optional.empty();
        }
        return proveedorJpaRepo.findByIdUsuario(idUsuario)
                .map(proveedor -> proveedor.getId().intValue());
    }

    @Override
    public void guardarPerfilProveedor(Long idUsuario, String razonSocial, String telefono, String nitRut) {
        ProveedorJpaEntity proveedor = new ProveedorJpaEntity();
        proveedor.setIdUsuario(idUsuario);
        proveedor.setRazonSocial(razonSocial);
        proveedor.setTelefono(telefono);
        proveedor.setNitRut(nitRut);
        proveedor.setCreatedAt(LocalDateTime.now());
        proveedorJpaRepo.save(proveedor);
    }

    @Override
    public void guardarSecret2fa(Long usuarioId, String secret2fa) {
        if (usuarioId != null) {
            usuarioJpaRepo.findById(usuarioId).ifPresent(entity -> {
                entity.setSecret2fa(secret2fa);
                usuarioJpaRepo.save(entity);
            });
        }
    }

    @Override
    public void activar2fa(Long usuarioId) {
        if (usuarioId != null) {
            usuarioJpaRepo.findById(usuarioId).ifPresent(entity -> {
                entity.set2faEnabled(true);
                usuarioJpaRepo.save(entity);
            });
        }
    }

    @Override
    public void desactivar2fa(Long usuarioId) {
        if (usuarioId != null) {
            usuarioJpaRepo.findById(usuarioId).ifPresent(entity -> {
                entity.set2faEnabled(false);
                entity.setSecret2fa(null);
                usuarioJpaRepo.save(entity);
            });
        }
    }
}