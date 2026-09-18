package com.service_reservation.EAP03.modulos.identidad.application;

import com.service_reservation.EAP03.modulos.identidad.application.exception.ReglaNegocioException;
import com.service_reservation.EAP03.modulos.identidad.domain.model.Usuario;
import com.service_reservation.EAP03.modulos.identidad.domain.model.UsuarioNuevoComando;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.in.RegistrarUsuarioUseCase;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.out.PasswordEncoderPort;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.out.UsuarioRepositoryPort;

import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrarUsuarioService implements RegistrarUsuarioUseCase {

    private final UsuarioRepositoryPort usuarioRepository;
    private final PasswordEncoderPort passwordEncoder;

    public RegistrarUsuarioService(UsuarioRepositoryPort usuarioRepository, PasswordEncoderPort passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void ejecutar(UsuarioNuevoComando comando) {
        if (usuarioRepository.existePorEmail(comando.email())) {
            throw new ReglaNegocioException("El correo ya está en uso");
        }

        String rolNormalizado = RolNombreUtil.normalizarRol(comando.rol());
        String passwordHash = passwordEncoder.codificar(comando.password());
        Usuario nuevoUsuario = new Usuario(comando.email(), passwordHash);

        Usuario usuarioGuardado = usuarioRepository.guardarUsuarioConRol(nuevoUsuario, rolNormalizado);

        if (Set.of("ROLE_CLIENTE", "CLIENTE").contains(rolNormalizado)) {
            guardarPerfilCliente(
                usuarioGuardado.getId(),
                comando.nombreIdentificacion(),
                comando.telefono(),
                comando.documentoIdentidad()
            );
        } else if (Set.of("ROLE_PROVEEDOR", "PROVEEDOR").contains(rolNormalizado)) {
            guardarPerfilProveedor(
                usuarioGuardado.getId(),
                comando.nombreIdentificacion(),
                comando.telefono(),
                comando.documentoIdentidad()
            );
        }
    }

    @Override
    @Transactional
    public void guardarPerfilCliente(Long usuarioId, String nombreIdentificacion, String telefono, String documentoIdentidad) {
        usuarioRepository.guardarPerfilCliente(usuarioId, nombreIdentificacion, telefono, documentoIdentidad);
    }

    @Override
    @Transactional
    public void guardarPerfilProveedor(Long usuarioId, String nombreIdentificacion, String telefono, String documentoIdentidad) {
        usuarioRepository.guardarPerfilProveedor(usuarioId, nombreIdentificacion, telefono, documentoIdentidad);
    }
}