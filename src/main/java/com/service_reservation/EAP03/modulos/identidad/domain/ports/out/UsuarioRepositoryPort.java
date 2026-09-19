package com.service_reservation.EAP03.modulos.identidad.domain.ports.out;

import com.service_reservation.EAP03.modulos.identidad.domain.model.Usuario;
import java.util.Optional;

public interface UsuarioRepositoryPort {
    boolean existePorEmail(String email);
    Optional<Usuario> buscarPorEmail(String email);
    Usuario guardarUsuarioConRol(Usuario usuario, String nombreRol);
    void guardarPerfilCliente(Long idUsuario, String nombre, String telefono, String documento);
    void guardarPerfilProveedor(Long idUsuario, String razonSocial, String telefono, String nitRut);
    Optional<Long> buscarIdProveedorPorUsuario(Long idUsuario);
}