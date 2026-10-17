package com.service_reservation.EAP03.modulos.identidad.application;

import com.service_reservation.EAP03.modulos.identidad.domain.ports.in.ConsultarClienteUseCase;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.out.UsuarioRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ConsultarClienteService implements ConsultarClienteUseCase {

    private final UsuarioRepositoryPort usuarioRepository;

    public ConsultarClienteService(UsuarioRepositoryPort usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Long> buscarIdClientePorUsuario(Long idUsuario) {
        return usuarioRepository.buscarIdClientePorUsuario(idUsuario);
    }
}
