package com.service_reservation.EAP03.modulos.identidad.application;

import com.service_reservation.EAP03.modulos.identidad.application.exception.ReglaNegocioException;
import com.service_reservation.EAP03.modulos.identidad.domain.exception.CredencialesInvalidasException;
import com.service_reservation.EAP03.modulos.identidad.domain.model.IniciarSesionComando;
import com.service_reservation.EAP03.modulos.identidad.domain.model.TokenRespuesta;
import com.service_reservation.EAP03.modulos.identidad.domain.model.Usuario;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.in.IniciarSesionUseCase;
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

    public IniciarSesionService(
            UsuarioRepositoryPort usuarioRepository,
            PasswordEncoderPort passwordEncoder,
            JwtPort jwtPort
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtPort = jwtPort;
    }

    @Override
    @Transactional(readOnly = true)
    public TokenRespuesta ejecutar(IniciarSesionComando comando) {
        String email = comando.email() != null ? comando.email().trim().toLowerCase() : "";

        // 1. Consulta de usuario y mitigación de enumeración
        Optional<Usuario> usuarioOptional = usuarioRepository.buscarPorEmail(email);

        if (usuarioOptional.isEmpty()) {
            throw new CredencialesInvalidasException("Credenciales incorrectas");
        }

        Usuario usuario = usuarioOptional.get();

        // 2. Verificación de estado de la cuenta
        if (!usuario.isEnabled()) {
            throw new ReglaNegocioException("La cuenta de usuario se encuentra inactiva");
        }

        // 3. Validación de contraseña con BCrypt
        if (!passwordEncoder.coincide(comando.password(), usuario.getPasswordHash())) {
            throw new CredencialesInvalidasException("Credenciales incorrectas");
        }

        // 4. Generación del token JWT
        String token = jwtPort.generarToken(usuario);

        return new TokenRespuesta(token, "Bearer", jwtPort.getExpiracionSegundos());
    }
}
