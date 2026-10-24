package com.service_reservation.EAP03.config;

import com.service_reservation.EAP03.modulos.identidad.domain.model.Usuario;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.out.PasswordEncoderPort;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.out.UsuarioRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class QaAdminSeederTest {

    @Nested
    @SpringBootTest
    @ActiveProfiles("qa")
    // Base H2 propia: así el admin de QA no queda en la base que comparten los demás tests.
    @TestPropertySource(properties = {"qa.admin.email=QA.Admin@qa.local", "qa.admin.password=QaAdmin123",
            "spring.datasource.url=jdbc:h2:mem:eap03-qa-seeder;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE"})
    @DisplayName("Con el perfil qa")
    class ConPerfilQa {

        @Autowired private UsuarioRepositoryPort usuarioRepository;
        @Autowired private PasswordEncoderPort passwordEncoder;

        @Test
        @DisplayName("Crea al arrancar un admin activo, con el correo en minúsculas y la contraseña configurada")
        void createsQaAdminOnStartup() {
            // Arrange: el seeder ya corrió al levantar el contexto

            // Act
            Optional<Usuario> admin = usuarioRepository.buscarPorEmail("qa.admin@qa.local");

            // Assert
            assertTrue(admin.isPresent());
            assertEquals("qa.admin@qa.local", admin.get().getEmail());
            assertTrue(admin.get().isEnabled());
            assertTrue(admin.get().getRoles().contains("ROLE_ADMIN"));
            assertTrue(passwordEncoder.coincide("QaAdmin123", admin.get().getPasswordHash()));
        }
    }

    @Nested
    @SpringBootTest
    @DisplayName("Sin el perfil qa (como en producción)")
    class SinPerfilQa {

        @Autowired private ApplicationContext context;
        @Autowired private UsuarioRepositoryPort usuarioRepository;

        @Test
        @DisplayName("No registra el seeder ni crea el admin de QA")
        void doesNotCreateQaAdmin() {
            // Arrange: contexto con la configuración por defecto

            // Act
            int seeders = context.getBeansOfType(QaAdminSeeder.class).size();
            boolean existe = usuarioRepository.existePorEmail("qa.admin@qa.local");

            // Assert
            assertEquals(0, seeders);
            assertFalse(existe);
        }
    }

    @Test
    @DisplayName("Falla con un mensaje claro si falta QA_ADMIN_PASSWORD")
    void failsWithoutPassword() {
        // Arrange
        QaAdminSeeder seeder = new QaAdminSeeder(mock(UsuarioRepositoryPort.class), mock(PasswordEncoderPort.class),
                "qa.admin@qa.local", "");

        // Act
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> seeder.run(new DefaultApplicationArguments()));

        // Assert
        assertTrue(ex.getMessage().contains("QA_ADMIN_PASSWORD"));
    }
}
