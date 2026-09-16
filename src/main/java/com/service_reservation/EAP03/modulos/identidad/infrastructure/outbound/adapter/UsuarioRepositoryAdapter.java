package com.service_reservation.EAP03.modulos.identidad.infrastructure.outbound.adapter;

import com.service_reservation.EAP03.modulos.identidad.domain.model.Usuario;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.out.UsuarioRepositoryPort;
import com.service_reservation.EAP03.modulos.identidad.infrastructure.outbound.persistence.entity.*;
import com.service_reservation.EAP03.modulos.identidad.infrastructure.outbound.persistence.repository.*;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
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
                .map(entity -> {
                    Set<String> roles = entity.getRoles().stream()
                            .map(RolJpaEntity::getNombre)
                            .collect(Collectors.toSet());
                    return new Usuario(
                            entity.getId(),
                            entity.getEmail(),
                            entity.getPasswordHash(),
                            entity.isEnabled(),
                            roles
                    );
                });
    }

    @Override
    public Usuario guardarUsuarioConRol(Usuario usuario, String nombreRol) {
        UsuarioJpaEntity usuarioEntity = new UsuarioJpaEntity();
        usuarioEntity.setEmail(usuario.getEmail());
        usuarioEntity.setPasswordHash(usuario.getPasswordHash());
        usuarioEntity.setEnabled(true);
        usuarioEntity.setCreatedAt(LocalDateTime.now());

        RolJpaEntity rolEntity = rolJpaRepo.findByNombre(nombreRol)
            .orElseThrow(() -> new RuntimeException("Rol no encontrado en BD"));
        
        usuarioEntity.addRol(rolEntity); 
        
        UsuarioJpaEntity guardado = usuarioJpaRepo.save(usuarioEntity);
        return new Usuario(guardado.getId(), guardado.getEmail(), guardado.getPasswordHash());
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
    public void guardarPerfilProveedor(Long idUsuario, String razonSocial, String telefono, String nitRut) {
        ProveedorJpaEntity proveedor = new ProveedorJpaEntity();
        proveedor.setIdUsuario(idUsuario);
        proveedor.setRazonSocial(razonSocial);
        proveedor.setTelefono(telefono);
        proveedor.setNitRut(nitRut);
        proveedor.setCreatedAt(LocalDateTime.now());
        proveedorJpaRepo.save(proveedor);
    }
}