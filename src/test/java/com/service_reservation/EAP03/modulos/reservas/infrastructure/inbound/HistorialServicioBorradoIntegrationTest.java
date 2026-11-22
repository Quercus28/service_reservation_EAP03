package com.service_reservation.EAP03.modulos.reservas.infrastructure.inbound;

import com.service_reservation.EAP03.modulos.identidad.domain.model.Usuario;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.out.JwtPort;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.out.PasswordEncoderPort;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.out.UsuarioRepositoryPort;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// HU21: una cita de un servicio que ya se borró no tumba el historial (antes respondía 500 por
// UnexpectedRollbackException). Con la aplicación completa: sesión real, controlador y base H2.
// La prueba no usa @Transactional porque el fallo solo aparece cuando la transacción se confirma
// de verdad; por eso borra al final lo que insertó, para no afectar a otras pruebas.
@SpringBootTest
class HistorialServicioBorradoIntegrationTest {

    @Autowired private WebApplicationContext webApplicationContext;
    @Autowired private UsuarioRepositoryPort usuarioRepository;
    @Autowired private PasswordEncoderPort passwordEncoder;
    @Autowired private JwtPort jwtPort;
    @Autowired private JdbcTemplate jdbcTemplate;

    private static final int ID_PROVEEDOR = 900;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
    }

    @AfterEach
    void limpiar() {
        jdbcTemplate.update("DELETE FROM servicio_prestado WHERE id_proveedor = ?", ID_PROVEEDOR);
        jdbcTemplate.update("DELETE FROM servicio WHERE id_proveedor = ?", ID_PROVEEDOR);
    }

    @Test
    @DisplayName("Un cliente con una cita de un servicio borrado y otra normal recibe 200 con las dos")
    void deletedServiceStillAppearsInHistory() throws Exception {
        String email = "cliente-" + UUID.randomUUID() + "@test.com";
        Usuario creado = usuarioRepository.guardarUsuarioConRol(new Usuario(email, passwordEncoder.codificar("Clave123")), "ROLE_CLIENTE");
        usuarioRepository.guardarPerfilCliente(creado.getId(), "Cliente Prueba", "3000000000", UUID.randomUUID().toString().substring(0, 10));
        Usuario cliente = usuarioRepository.buscarPorId(creado.getId()).orElseThrow();
        Long idCliente = usuarioRepository.buscarIdClientePorUsuario(cliente.getId()).orElseThrow();

        jdbcTemplate.update("INSERT INTO servicio (id_proveedor, duracion_minutos, nombre) VALUES (?, 60, 'Masaje')", ID_PROVEEDOR);
        Integer idServicioVivo = jdbcTemplate.queryForObject("SELECT MAX(id) FROM servicio", Integer.class);
        jdbcTemplate.update("INSERT INTO servicio (id_proveedor, duracion_minutos, nombre) VALUES (?, 30, 'Corte')", ID_PROVEEDOR);
        Integer idServicioBorrado = jdbcTemplate.queryForObject("SELECT MAX(id) FROM servicio", Integer.class);

        insertarReserva(idCliente, idServicioVivo, "2026-10-02 10:00:00", "2026-10-02 11:00:00", "RESERVADO");
        insertarReserva(idCliente, idServicioBorrado, "2026-10-01 15:00:00", "2026-10-01 15:30:00", "COMPLETADO");
        jdbcTemplate.update("DELETE FROM servicio WHERE id = ?", idServicioBorrado);

        mockMvc.perform(get("/api/v1/reservas/mias")
                        .header("Authorization", "Bearer " + jwtPort.generarToken(cliente)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido", hasSize(2)))
                .andExpect(jsonPath("$.contenido[0].idServicio").value(idServicioVivo))
                .andExpect(jsonPath("$.contenido[0].nombreServicio").value("Masaje"))
                .andExpect(jsonPath("$.contenido[1].idServicio").value(idServicioBorrado))
                .andExpect(jsonPath("$.contenido[1].nombreServicio").value("Servicio no disponible"))
                .andExpect(jsonPath("$.contenido[1].fechaInicio").value("2026-10-01T15:00:00"))
                .andExpect(jsonPath("$.contenido[1].hora").value("15:00"))
                .andExpect(jsonPath("$.contenido[1].estado").value("COMPLETADO"));
    }

    private void insertarReserva(Long idCliente, Integer idServicio, String inicio, String fin, String estado) {
        jdbcTemplate.update("INSERT INTO servicio_prestado (id_cliente, id_servicio, id_proveedor, fecha_inicio, fecha_fin, estado) "
                + "VALUES (?, ?, ?, ?, ?, ?)", idCliente, idServicio, ID_PROVEEDOR, inicio, fin, estado);
    }
}
