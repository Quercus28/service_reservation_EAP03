package com.service_reservation.EAP03.modulos.admin.domain.ports.out;

import com.service_reservation.EAP03.modulos.admin.domain.model.ProveedorPerfilDTO;

import java.util.Optional;

public interface ProveedorPerfilPort {
    Optional<ProveedorPerfilDTO> buscarPorIdUsuario(Long idUsuario);
    void actualizarPerfil(Long idUsuario, String razonSocial, String nitRut, String telefono);
}
