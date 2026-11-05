package com.service_reservation.EAP03.modulos.identidad.application;

import com.service_reservation.EAP03.modulos.identidad.application.exception.ReglaNegocioException;
import com.service_reservation.EAP03.modulos.identidad.domain.exception.CredencialesInvalidasException;
import com.service_reservation.EAP03.modulos.identidad.domain.model.Usuario;
import com.service_reservation.EAP03.modulos.identidad.domain.model.TokenRespuesta;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.in.Activar2faUseCase;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.out.JwtPort;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.out.TotpPort;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.out.UsuarioRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class Activar2faService implements Activar2faUseCase {

    private final UsuarioRepositoryPort usuarioRepository;
    private final TotpPort totpPort;
    private final JwtPort jwtPort;

    public Activar2faService(UsuarioRepositoryPort usuarioRepository, TotpPort totpPort, JwtPort jwtPort) {
        this.usuarioRepository = usuarioRepository;
        this.totpPort = totpPort;
        this.jwtPort = jwtPort;
    }

    @Override
    @Transactional
    public TokenRespuesta ejecutar(Long idUsuario, String codigoTotp) {
        Usuario usuario = usuarioRepository.buscarPorId(idUsuario)
                .orElseThrow(() -> new ReglaNegocioException("Usuario no encontrado"));

        if (usuario.getSecret2fa() == null || usuario.getSecret2fa().isBlank()) {
            throw new ReglaNegocioException("No se ha iniciado la configuración de 2FA para este usuario");
        }

        boolean esValido = totpPort.validarCodigo(usuario.getSecret2fa(), codigoTotp);
        if (!esValido) {
            throw new CredencialesInvalidasException("Código 2FA incorrecto o expirado");
        }

        usuarioRepository.activar2fa(idUsuario);
        String token = jwtPort.generarToken(usuario);
        return TokenRespuesta.exitoso(token, "Bearer", jwtPort.getExpiracionSegundos());
    }
}
