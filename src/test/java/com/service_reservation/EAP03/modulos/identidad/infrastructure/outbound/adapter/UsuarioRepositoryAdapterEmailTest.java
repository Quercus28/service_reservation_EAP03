package com.service_reservation.EAP03.modulos.identidad.infrastructure.outbound.adapter;

import com.service_reservation.EAP03.modulos.identidad.domain.model.Usuario;
import com.service_reservation.EAP03.modulos.identidad.infrastructure.outbound.persistence.entity.UsuarioJpaEntity;
import com.service_reservation.EAP03.modulos.identidad.infrastructure.outbound.persistence.repository.UsuarioSpringDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class UsuarioRepositoryAdapterEmailTest {

    @Autowired
    private UsuarioRepositoryAdapter adapter;

    @Autowired
    private UsuarioSpringDataRepository usuarioJpaRepo;

    private Long idCuentaAntigua;

    @BeforeEach
    void setUp() {
        // Cuenta guardada antes del fix, con mayúsculas en el correo (H-18).
        UsuarioJpaEntity entity = new UsuarioJpaEntity();
        entity.setEmail("Qa.ProvA@Test.com");
        entity.setPasswordHash("hash");
        entity.setEnabled(true);
        entity.setCreatedAt(LocalDateTime.now());
        idCuentaAntigua = usuarioJpaRepo.save(entity).getId();
    }

    @Test
    @DisplayName("Detecta como duplicada la variante en minúsculas de una cuenta con mayúsculas")
    void existsIgnoresCase() {
        // Arrange
        String correoEnMinusculas = "qa.prova@test.com";

        // Act
        boolean existe = adapter.existePorEmail(correoEnMinusculas);

        // Assert
        assertTrue(existe);
    }

    @Test
    @DisplayName("Encuentra para el login una cuenta antigua guardada con mayúsculas (H-18)")
    void findIgnoresCase() {
        // Arrange
        String correoDelLogin = "qa.prova@test.com";

        // Act
        Optional<Usuario> usuario = adapter.buscarPorEmail(correoDelLogin);

        // Assert
        assertTrue(usuario.isPresent());
        assertEquals(idCuentaAntigua, usuario.get().getId());
    }

    @Test
    @DisplayName("No encuentra un correo distinto")
    void doesNotMatchOtherEmail() {
        // Arrange
        String otroCorreo = "qa.provb@test.com";

        // Act
        boolean existe = adapter.existePorEmail(otroCorreo);

        // Assert
        assertFalse(existe);
    }
}
