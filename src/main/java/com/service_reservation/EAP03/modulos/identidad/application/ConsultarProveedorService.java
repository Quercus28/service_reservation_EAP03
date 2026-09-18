package com.service_reservation.EAP03.modulos.identidad.application;

import com.service_reservation.EAP03.modulos.identidad.domain.ports.in.ConsultarProveedorUseCase;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.out.UsuarioRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ConsultarProveedorService implements ConsultarProveedorUseCase {

    private final UsuarioRepositoryPort usuarioRepository;

    public ConsultarProveedorService(UsuarioRepositoryPort usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Integer> buscarIdProveedorPorUsuario(Long idUsuario) {
        return usuarioRepository.buscarIdProveedorPorUsuario(idUsuario);
    }
}
