package com.service_reservation.EAP03.modulos.identidad.application;

import com.service_reservation.EAP03.modulos.identidad.application.exception.ReglaNegocioException;
import com.service_reservation.EAP03.modulos.identidad.domain.exception.CredencialesInvalidasException;
import com.service_reservation.EAP03.modulos.identidad.domain.model.TokenRespuesta;
import com.service_reservation.EAP03.modulos.identidad.domain.model.Usuario;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.in.VerificarLogin2faUseCase;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.out.JwtPort;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.out.TotpPort;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.out.UsuarioRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VerificarLogin2faService implements VerificarLogin2faUseCase {

    private final UsuarioRepositoryPort usuarioRepository;
    private final TotpPort totpPort;
    private final JwtPort jwtPort;

    public VerificarLogin2faService(
            UsuarioRepositoryPort usuarioRepository,
            TotpPort totpPort,
            JwtPort jwtPort
    ) {
        this.usuarioRepository = usuarioRepository;
        this.totpPort = totpPort;
        this.jwtPort = jwtPort;
    }

    @Override
    @Transactional(readOnly = true)
    public TokenRespuesta ejecutar(String tokenTemporal, String codigoTotp) {
        if (!jwtPort.esTokenTemporal2faValido(tokenTemporal)) {
            throw new CredencialesInvalidasException("Token temporal 2FA inválido o expirado");
        }

        String email = jwtPort.extraerEmail(tokenTemporal);
        Usuario usuario = usuarioRepository.buscarPorEmail(email)
                .orElseThrow(() -> new CredencialesInvalidasException("Usuario no encontrado"));

        if (!usuario.isEnabled()) {
            throw new ReglaNegocioException("La cuenta de usuario se encuentra inactiva");
        }

        if (!usuario.is2faEnabled() || usuario.getSecret2fa() == null) {
            throw new ReglaNegocioException("El usuario no tiene 2FA configurado");
        }

        boolean esValido = totpPort.validarCodigo(usuario.getSecret2fa(), codigoTotp);
        if (!esValido) {
            throw new CredencialesInvalidasException("Código 2FA incorrecto o expirado");
        }

        String tokenDefinitivo = jwtPort.generarToken(usuario);
        return TokenRespuesta.exitoso(tokenDefinitivo, "Bearer", jwtPort.getExpiracionSegundos());
    }
}
