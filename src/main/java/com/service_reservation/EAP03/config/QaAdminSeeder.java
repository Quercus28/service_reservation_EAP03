package com.service_reservation.EAP03.config;

import com.service_reservation.EAP03.modulos.identidad.application.EmailUtil;
import com.service_reservation.EAP03.modulos.identidad.domain.model.Usuario;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.out.PasswordEncoderPort;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.out.UsuarioRepositoryPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Crea un administrador para las pruebas de QA (aprobar proveedores desde la colección de Postman).
 * Solo existe con el perfil "qa": producción arranca sin perfil y nunca lo ejecuta.
 * La contraseña sale de la variable de entorno QA_ADMIN_PASSWORD; no se guarda en el repositorio.
 */
@Slf4j
@Component
@Profile("qa")
public class QaAdminSeeder implements ApplicationRunner {

    private final UsuarioRepositoryPort usuarioRepository;
    private final PasswordEncoderPort passwordEncoder;
    private final String email;
    private final String password;

    public QaAdminSeeder(UsuarioRepositoryPort usuarioRepository,
                         PasswordEncoderPort passwordEncoder,
                         @Value("${qa.admin.email}") String email,
                         @Value("${qa.admin.password:}") String password) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.email = EmailUtil.normalizarEmail(email);
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (password == null || password.isBlank()) {
            throw new IllegalStateException(
                    "El perfil 'qa' necesita la variable de entorno QA_ADMIN_PASSWORD con la contraseña del admin de QA");
        }
        if (usuarioRepository.existePorEmail(email)) {
            log.info("Admin de QA ya existe: {}", email);
            return;
        }
        usuarioRepository.guardarUsuarioConRol(new Usuario(email, passwordEncoder.codificar(password)), "ROLE_ADMIN");
        log.info("Admin de QA creado: {}", email);
    }
}
