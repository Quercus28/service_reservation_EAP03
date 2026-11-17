package com.service_reservation.EAP03.modulos.reservas.infrastructure.inbound;

import com.service_reservation.EAP03.modulos.admin.infrastructure.adapter.out.persistence.entity.RegistroAuditoriaEntity;
import com.service_reservation.EAP03.modulos.admin.infrastructure.adapter.out.persistence.repository.RegistroAuditoriaJpaRepository;
import com.service_reservation.EAP03.modulos.identidad.domain.model.Usuario;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.out.JwtPort;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.out.PasswordEncoderPort;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.out.UsuarioRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

// HU19: un cliente que pide el historial de otro recibe 403 y el intento queda guardado en
// admin_auditoria (con la aplicación completa: sesión real, controlador, manejador y base H2).
@SpringBootTest
class HistorialAjenoAuditoriaIntegrationTest {

    @Autowired private WebApplicationContext webApplicationContext;
    @Autowired private UsuarioRepositoryPort usuarioRepository;
    @Autowired private PasswordEncoderPort passwordEncoder;
    @Autowired private JwtPort jwtPort;
    @Autowired private RegistroAuditoriaJpaRepository auditoriaRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
    }

    private Usuario crearCliente() {
        String email = "cliente-" + UUID.randomUUID() + "@test.com";
        Usuario creado = usuarioRepository.guardarUsuarioConRol(new Usuario(email, passwordEncoder.codificar("Clave123")), "ROLE_CLIENTE");
        usuarioRepository.guardarPerfilCliente(creado.getId(), "Cliente Prueba", "3000000000", UUID.randomUUID().toString().substring(0, 10));
        return usuarioRepository.buscarPorId(creado.getId()).orElseThrow();
    }

    private List<RegistroAuditoriaEntity> auditoriaDe(Long idUsuario) {
        return auditoriaRepository.findAll().stream()
                .filter(r -> idUsuario.equals(r.getIdActor()) && "HISTORIAL_AJENO_DENEGADO".equals(r.getAccion()))
                .toList();
    }

    @Test
    @DisplayName("Pedir el historial de otro cliente responde 403 y queda guardado en la auditoría sin datos personales")
    void otherClientsHistoryIsSavedInAudit() throws Exception {
        Usuario cliente = crearCliente();
        Long idClientePropio = usuarioRepository.buscarIdClientePorUsuario(cliente.getId()).orElseThrow();
        long idClienteAjeno = idClientePropio + 1000;

        int status = mockMvc.perform(get("/api/v1/reservas/mias")
                        .param("idCliente", String.valueOf(idClienteAjeno))
                        .header("Authorization", "Bearer " + jwtPort.generarToken(cliente)))
                .andReturn().getResponse().getStatus();

        assertEquals(403, status);
        List<RegistroAuditoriaEntity> registros = auditoriaDe(cliente.getId());
        assertEquals(1, registros.size());
        RegistroAuditoriaEntity registro = registros.get(0);
        assertEquals("DENEGADO", registro.getResultado());
        assertEquals("GET /api/v1/reservas/mias", registro.getEndpoint());
        assertEquals("motivo=historial de otro cliente", registro.getDetalle());
        assertNull(registro.getIdUsuarioAfectado());
        assertFalse(registro.getEndpoint().contains(cliente.getEmail()));
        assertFalse(registro.getDetalle().contains("@"));
    }

    @Test
    @DisplayName("Pedir el historial propio no deja registro en la auditoría")
    void ownHistoryIsNotAudited() throws Exception {
        Usuario cliente = crearCliente();

        int status = mockMvc.perform(get("/api/v1/reservas/mias")
                        .header("Authorization", "Bearer " + jwtPort.generarToken(cliente)))
                .andReturn().getResponse().getStatus();

        assertEquals(200, status);
        assertTrue(auditoriaDe(cliente.getId()).isEmpty());
    }
}
