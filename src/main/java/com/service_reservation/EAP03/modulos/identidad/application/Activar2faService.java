package com.service_reservation.EAP03.modulos.identidad.application;

import com.service_reservation.EAP03.modulos.identidad.application.exception.ReglaNegocioException;
import com.service_reservation.EAP03.modulos.identidad.domain.exception.CredencialesInvalidasException;
import com.service_reservation.EAP03.modulos.identidad.domain.model.TokenRespuesta;
import com.service_reservation.EAP03.modulos.identidad.domain.model.Usuario;
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
    public void ejecutar(Long idUsuario, String codigoTotp) {
        Usuario usuario = buscar(idUsuario);
        validarCodigo(usuario, codigoTotp);
        usuarioRepository.activar2fa(idUsuario);
    }

    @Override
    @Transactional
    public TokenRespuesta ejecutarEnConfiguracionInicial(Long idUsuario, String codigoTotp) {
        Usuario usuario = buscar(idUsuario);
        // Con el token de configuración no se puede tocar un 2FA que ya está activo.
        if (usuario.is2faEnabled()) {
            throw new ReglaNegocioException("La autenticación de dos factores ya está activa para esta cuenta");
        }
        // El token de configuración solo se entrega a administradores; se revisa de nuevo por si
        // el rol cambió mientras el token seguía vigente.
        if (!usuario.esAdmin() || !usuario.isEnabled()) {
            throw new CredencialesInvalidasException("Token de configuración 2FA no válido para esta cuenta");
        }
        validarCodigo(usuario, codigoTotp);
        usuarioRepository.activar2fa(idUsuario);
        return TokenRespuesta.exitoso(jwtPort.generarToken(usuario), "Bearer", jwtPort.getExpiracionSegundos());
    }

    private Usuario buscar(Long idUsuario) {
        return usuarioRepository.buscarPorId(idUsuario)
                .orElseThrow(() -> new ReglaNegocioException("Usuario no encontrado"));
    }

    private void validarCodigo(Usuario usuario, String codigoTotp) {
        if (usuario.getSecret2fa() == null || usuario.getSecret2fa().isBlank()) {
            throw new ReglaNegocioException("No se ha iniciado la configuración de 2FA para este usuario");
        }
        if (!totpPort.validarCodigo(usuario.getSecret2fa(), codigoTotp)) {
            throw new CredencialesInvalidasException("Código 2FA incorrecto o expirado");
        }
    }
}
