package com.service_reservation.EAP03.config;

import com.service_reservation.EAP03.modulos.identidad.domain.model.Usuario;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.out.PasswordEncoderPort;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.out.UsuarioRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

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

    @Test
    @DisplayName("Falla si la contraseña del admin de QA es nula")
    void failsWithNullPassword() {
        // Arrange
        UsuarioRepositoryPort usuarioRepository = mock(UsuarioRepositoryPort.class);
        QaAdminSeeder seeder = new QaAdminSeeder(usuarioRepository, mock(PasswordEncoderPort.class),
                "qa.admin@qa.local", null);

        // Act
        assertThrows(IllegalStateException.class, () -> seeder.run(new DefaultApplicationArguments()));

        // Assert
        verifyNoInteractions(usuarioRepository);
    }

    @Test
    @DisplayName("No vuelve a crear el admin de QA si ya existe")
    void doesNotDuplicateExistingQaAdmin() {
        // Arrange
        UsuarioRepositoryPort usuarioRepository = mock(UsuarioRepositoryPort.class);
        PasswordEncoderPort passwordEncoder = mock(PasswordEncoderPort.class);
        when(usuarioRepository.existePorEmail("qa.admin@qa.local")).thenReturn(true);
        QaAdminSeeder seeder = new QaAdminSeeder(usuarioRepository, passwordEncoder, "QA.Admin@qa.local", "QaAdmin123");

        // Act
        seeder.run(new DefaultApplicationArguments());

        // Assert
        verify(usuarioRepository, never()).guardarUsuarioConRol(any(), any());
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    @DisplayName("Crea el admin de QA con el correo normalizado y la contraseña codificada")
    void createsQaAdminWithNormalizedEmail() {
        // Arrange
        UsuarioRepositoryPort usuarioRepository = mock(UsuarioRepositoryPort.class);
        PasswordEncoderPort passwordEncoder = mock(PasswordEncoderPort.class);
        when(passwordEncoder.codificar("QaAdmin123")).thenReturn("hash");
        QaAdminSeeder seeder = new QaAdminSeeder(usuarioRepository, passwordEncoder, " QA.Admin@qa.local ", "QaAdmin123");

        // Act
        seeder.run(new DefaultApplicationArguments());

        // Assert
        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).guardarUsuarioConRol(captor.capture(), eq("ROLE_ADMIN"));
        assertEquals("qa.admin@qa.local", captor.getValue().getEmail());
        assertEquals("hash", captor.getValue().getPasswordHash());
    }
}
