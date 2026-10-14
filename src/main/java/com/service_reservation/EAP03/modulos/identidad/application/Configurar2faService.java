package com.service_reservation.EAP03.modulos.identidad.application;

import com.service_reservation.EAP03.modulos.identidad.application.exception.ReglaNegocioException;
import com.service_reservation.EAP03.modulos.identidad.domain.model.ResultadoConfiguracion2fa;
import com.service_reservation.EAP03.modulos.identidad.domain.model.Usuario;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.in.Configurar2faUseCase;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.out.TotpPort;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.out.UsuarioRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class Configurar2faService implements Configurar2faUseCase {

    private final UsuarioRepositoryPort usuarioRepository;
    private final TotpPort totpPort;

    public Configurar2faService(UsuarioRepositoryPort usuarioRepository, TotpPort totpPort) {
        this.usuarioRepository = usuarioRepository;
        this.totpPort = totpPort;
    }

    @Override
    @Transactional
    public ResultadoConfiguracion2fa ejecutar(Long idUsuario) {
        Usuario usuario = usuarioRepository.buscarPorId(idUsuario)
                .orElseThrow(() -> new ReglaNegocioException("Usuario no encontrado"));

        String secreto = totpPort.generarSecreto();
        usuarioRepository.guardarSecret2fa(idUsuario, secreto);
        byte[] imagenQr = totpPort.generarQrPng(usuario.getEmail(), secreto);

        return new ResultadoConfiguracion2fa(imagenQr, secreto);
    }
}
