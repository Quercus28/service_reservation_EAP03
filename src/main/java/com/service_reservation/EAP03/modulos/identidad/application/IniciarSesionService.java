package com.service_reservation.EAP03.modulos.identidad.application;

import com.service_reservation.EAP03.modulos.identidad.application.exception.ReglaNegocioException;
import com.service_reservation.EAP03.modulos.identidad.domain.exception.CredencialesInvalidasException;
import com.service_reservation.EAP03.modulos.identidad.domain.exception.CuentaBloqueadaException;
import com.service_reservation.EAP03.modulos.identidad.domain.model.IniciarSesionComando;
import com.service_reservation.EAP03.modulos.identidad.domain.model.TokenRespuesta;
import com.service_reservation.EAP03.modulos.identidad.domain.model.Usuario;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.in.IniciarSesionUseCase;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.out.BloqueoFuerzaBrutaPort;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.out.JwtPort;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.out.PasswordEncoderPort;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.out.UsuarioRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class IniciarSesionService implements IniciarSesionUseCase {

    private final UsuarioRepositoryPort usuarioRepository;
    private final PasswordEncoderPort passwordEncoder;
    private final JwtPort jwtPort;
    private final BloqueoFuerzaBrutaPort bloqueoFuerzaBrutaPort;

    public IniciarSesionService(
            UsuarioRepositoryPort usuarioRepository,
            PasswordEncoderPort passwordEncoder,
            JwtPort jwtPort,
            BloqueoFuerzaBrutaPort bloqueoFuerzaBrutaPort
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtPort = jwtPort;
        this.bloqueoFuerzaBrutaPort = bloqueoFuerzaBrutaPort;
    }

    @Override
    @Transactional(readOnly = true)
    public TokenRespuesta ejecutar(IniciarSesionComando comando) {
        String email = comando.email() != null ? comando.email().trim().toLowerCase() : "";

        // 1. Chequeo de prevención contra fuerza bruta (bloqueo de 15 minutos tras 5 intentos fallidos)
        if (bloqueoFuerzaBrutaPort.estaBloqueado(email)) {
            throw new CuentaBloqueadaException("La cuenta se encuentra bloqueada temporalmente por demasiados intentos fallidos. Intente de nuevo en 15 minutos.");
        }

        // 2. Consulta de usuario y mitigación de enumeración
        Optional<Usuario> usuarioOptional = usuarioRepository.buscarPorEmail(email);

        if (usuarioOptional.isEmpty()) {
            bloqueoFuerzaBrutaPort.registrarIntentoFallido(email);
            throw new CredencialesInvalidasException("Credenciales incorrectas");
        }

        Usuario usuario = usuarioOptional.get();

        // 3. Verificación de estado de la cuenta
        if (!usuario.isEnabled()) {
            throw new ReglaNegocioException("La cuenta de usuario se encuentra inactiva");
        }

        // 4. Validación de contraseña con BCrypt
        if (!passwordEncoder.coincide(comando.password(), usuario.getPasswordHash())) {
            bloqueoFuerzaBrutaPort.registrarIntentoFallido(email);
            throw new CredencialesInvalidasException("Credenciales incorrectas");
        }

        // 5. Credenciales válidas: reiniciar intentos fallidos
        bloqueoFuerzaBrutaPort.resetearIntentos(email);

        // 6. Si el usuario tiene 2FA activado, emitir token temporal
        if (usuario.is2faEnabled()) {
            String tokenTemporal = jwtPort.generarTokenTemporal2fa(usuario);
            return TokenRespuesta.requiere2fa(tokenTemporal);
        }

        // 7. Generación del token JWT normal
        String token = jwtPort.generarToken(usuario);

        return TokenRespuesta.exitoso(token, "Bearer", jwtPort.getExpiracionSegundos());
    }
}
