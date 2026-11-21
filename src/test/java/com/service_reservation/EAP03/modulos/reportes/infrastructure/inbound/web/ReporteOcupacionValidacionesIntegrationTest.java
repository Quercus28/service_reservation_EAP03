package com.service_reservation.EAP03.modulos.reportes.infrastructure.inbound.web;

import com.service_reservation.EAP03.modulos.admin.domain.ports.in.VerificarPermisoOperativoUseCase;
import com.service_reservation.EAP03.modulos.admin.infrastructure.adapter.out.persistence.entity.RegistroAuditoriaEntity;
import com.service_reservation.EAP03.modulos.admin.infrastructure.adapter.out.persistence.repository.RegistroAuditoriaJpaRepository;
import com.service_reservation.EAP03.modulos.identidad.domain.model.Usuario;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.out.JwtPort;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.out.PasswordEncoderPort;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.out.UsuarioRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Reporte de ocupación con la aplicación completa (sesión real, controlador, manejadores y base H2):
// validaciones de los parámetros y auditoría del intento de ver el reporte de otro proveedor.
// El permiso operativo se simula activo para no depender del flujo de aprobación de proveedores.
@SpringBootTest
class ReporteOcupacionValidacionesIntegrationTest {

    private static final String URL = "/api/reportes/ocupacion";

    @Autowired private WebApplicationContext webApplicationContext;
    @Autowired private UsuarioRepositoryPort usuarioRepository;
    @Autowired private PasswordEncoderPort passwordEncoder;
    @Autowired private JwtPort jwtPort;
    @Autowired private RegistroAuditoriaJpaRepository auditoriaRepository;
    @MockitoBean private VerificarPermisoOperativoUseCase verificarPermisoOperativo;

    private MockMvc mockMvc;
    private Usuario proveedor;
    private String tokenProveedor;

    @BeforeEach
    void setUp() {
        when(verificarPermisoOperativo.tienePermisoOperativo(any(), any())).thenReturn(true);
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();

        Usuario creado = usuarioRepository.guardarUsuarioConRol(
                new Usuario("proveedor-" + UUID.randomUUID() + "@test.com", passwordEncoder.codificar("Clave123")), "ROLE_PROVEEDOR");
        usuarioRepository.guardarPerfilProveedor(creado.getId(), "Proveedor Prueba", "3000000000",
                UUID.randomUUID().toString().substring(0, 10));
        proveedor = usuarioRepository.buscarPorId(creado.getId()).orElseThrow();
        tokenProveedor = "Bearer " + jwtPort.generarToken(proveedor);
    }

    private ResultActions pedir(MockHttpServletRequestBuilder peticion) throws Exception {
        return mockMvc.perform(peticion.header("Authorization", tokenProveedor));
    }

    private static MockHttpServletRequestBuilder periodoValido() {
        return get(URL).param("startDate", "2026-11-02").param("endDate", "2026-11-08");
    }

    private static void formatoEstandar(ResultActions resultado, String errorCode, String mensaje) throws Exception {
        resultado.andExpect(jsonPath("$.errorCode").value(errorCode))
                .andExpect(jsonPath("$.message").value(mensaje))
                .andExpect(jsonPath("$.traceId", notNullValue()));
    }

    private List<RegistroAuditoriaEntity> auditoriaDe(Long idUsuario) {
        return auditoriaRepository.findAll().stream()
                .filter(r -> idUsuario.equals(r.getIdActor()) && "REPORTE_AJENO_DENEGADO".equals(r.getAccion()))
                .toList();
    }

    @Test
    @DisplayName("Con un período válido y sin citas responde 200 con 0% y no deja auditoría")
    void periodoValidoSinCitas() throws Exception {
        pedir(periodoValido())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCitasGlobal").value(0))
                .andExpect(jsonPath("$.porcentajeOcupacionGlobal").value(0.0));
        assertTrue(auditoriaDe(proveedor.getId()).isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"proveedorId", "idProveedor", "PROVEEDORID", "IdProveedor"})
    @DisplayName("Si la petición nombra a un proveedor responde 403 y queda una fila en admin_auditoria sin datos personales")
    void reporteAjenoQuedaEnAuditoria(String parametro) throws Exception {
        formatoEstandar(pedir(periodoValido().param(parametro, "999")).andExpect(status().isForbidden()),
                "ACCESO_NO_AUTORIZADO", "No tiene privilegios para realizar esta operación");

        List<RegistroAuditoriaEntity> registros = auditoriaDe(proveedor.getId());
        assertEquals(1, registros.size());
        RegistroAuditoriaEntity registro = registros.get(0);
        assertEquals("DENEGADO", registro.getResultado());
        assertEquals("GET /api/reportes/ocupacion", registro.getEndpoint());
        assertEquals("motivo=reporte de otro proveedor", registro.getDetalle());
        assertNull(registro.getIdUsuarioAfectado());
        assertFalse(registro.getEndpoint().contains("999"));
        assertFalse(registro.getDetalle().contains("@"));
    }

    @Test
    @DisplayName("Falta endDate: 400 VALIDACION_ENTRADA nombrando el campo")
    void faltaEndDate() throws Exception {
        formatoEstandar(pedir(get(URL).param("startDate", "2026-11-02")).andExpect(status().isBadRequest()),
                "VALIDACION_ENTRADA", "El parámetro 'endDate' es obligatorio");
    }

    @Test
    @DisplayName("startDate vacío: 400 VALIDACION_ENTRADA nombrando el campo")
    void startDateVacio() throws Exception {
        formatoEstandar(pedir(get(URL).param("startDate", "").param("endDate", "2026-11-08")).andExpect(status().isBadRequest()),
                "VALIDACION_ENTRADA", "El parámetro 'startDate' es obligatorio");
    }

    @Test
    @DisplayName("Un parámetro desconocido: 400 CONSULTA_INVALIDA")
    void parametroDesconocido() throws Exception {
        formatoEstandar(pedir(periodoValido().param("x", "<script>alert(1)</script>")).andExpect(status().isBadRequest()),
                "CONSULTA_INVALIDA", "La solicitud de consulta no es válida.");
    }

    @Test
    @DisplayName("Un parámetro repetido: 400 CONSULTA_INVALIDA")
    void parametroRepetido() throws Exception {
        formatoEstandar(pedir(get(URL).param("startDate", "2026-11-02", "2026-01-01").param("endDate", "2026-11-08"))
                        .andExpect(status().isBadRequest()),
                "CONSULTA_INVALIDA", "La solicitud de consulta no es válida.");
    }

    @Test
    @DisplayName("Un período de más de 366 días: 400 PERIODO_INVALIDO con un mensaje que lo dice")
    void periodoMuyLargo() throws Exception {
        formatoEstandar(pedir(get(URL).param("startDate", "2026-01-01").param("endDate", "2027-01-02"))
                        .andExpect(status().isBadRequest()),
                "PERIODO_INVALIDO", "El período no puede ser mayor a 366 días");
    }

    @Test
    @DisplayName("Sigue igual: fin antes que inicio responde 400 PERIODO_INVALIDO")
    void finAntesQueInicio() throws Exception {
        formatoEstandar(pedir(get(URL).param("startDate", "2026-11-08").param("endDate", "2026-11-02"))
                        .andExpect(status().isBadRequest()),
                "PERIODO_INVALIDO", "La fecha de fin no puede ser anterior a la fecha de inicio");
    }

    @Test
    @DisplayName("Sigue igual: una fecha mal escrita responde 400 FORMATO_PARAMETRO_INVALIDO")
    void fechaMalEscrita() throws Exception {
        formatoEstandar(pedir(get(URL).param("startDate", "2026-11-02").param("endDate", "2026-13-45"))
                        .andExpect(status().isBadRequest()),
                "FORMATO_PARAMETRO_INVALIDO", "El parámetro 'endDate' tiene un formato o valor inválido");
    }

    @Test
    @DisplayName("Sigue igual: sin token responde 401")
    void sinToken() throws Exception {
        mockMvc.perform(periodoValido()).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Sigue igual: un cliente recibe 403 ACCESO_NO_AUTORIZADO")
    void clienteRecibe403() throws Exception {
        Usuario cliente = usuarioRepository.guardarUsuarioConRol(
                new Usuario("cliente-" + UUID.randomUUID() + "@test.com", passwordEncoder.codificar("Clave123")), "ROLE_CLIENTE");
        cliente = usuarioRepository.buscarPorId(cliente.getId()).orElseThrow();

        formatoEstandar(mockMvc.perform(periodoValido().header("Authorization", "Bearer " + jwtPort.generarToken(cliente)))
                        .andExpect(status().isForbidden()),
                "ACCESO_NO_AUTORIZADO", "No tiene privilegios para realizar esta operación");
    }
}
