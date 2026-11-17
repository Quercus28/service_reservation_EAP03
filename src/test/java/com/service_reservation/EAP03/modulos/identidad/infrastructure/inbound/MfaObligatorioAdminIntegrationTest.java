package com.service_reservation.EAP03.modulos.identidad.infrastructure.inbound;

import com.jayway.jsonpath.JsonPath;
import com.service_reservation.EAP03.modulos.identidad.domain.model.Usuario;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.out.JwtPort;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.out.PasswordEncoderPort;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.out.UsuarioRepositoryPort;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.HashingAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

// Bug 141: un ADMIN sin 2FA no puede recibir el JWT definitivo solo con la contraseña.
// Recorre el flujo real (filtro JWT, seguridad, controladores, base H2 y TOTP de verdad).
@SpringBootTest
class MfaObligatorioAdminIntegrationTest {

    private static final String CLAVE = "ClaveSegura123";

    @Autowired private WebApplicationContext webApplicationContext;
    @Autowired private UsuarioRepositoryPort usuarioRepository;
    @Autowired private PasswordEncoderPort passwordEncoder;
    @Autowired private JwtPort jwtPort;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    // --- Ayudas ---

    private Usuario crearUsuario(String rol) {
        String email = rol.toLowerCase().replace("role_", "") + "-" + UUID.randomUUID() + "@test.com";
        return usuarioRepository.guardarUsuarioConRol(new Usuario(email, passwordEncoder.codificar(CLAVE)), rol);
    }

    private Usuario adminCon2faActivo(String secreto) {
        Usuario admin = crearUsuario("ROLE_ADMIN");
        usuarioRepository.guardarSecret2fa(admin.getId(), secreto);
        usuarioRepository.activar2fa(admin.getId());
        return usuarioRepository.buscarPorId(admin.getId()).orElseThrow();
    }

    private String loginJson(Usuario usuario) throws Exception {
        MvcResult r = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + usuario.getEmail() + "\",\"password\":\"" + CLAVE + "\"}"))
                .andReturn();
        assertEquals(200, r.getResponse().getStatus());
        return r.getResponse().getContentAsString();
    }

    private static String codigoActual(String secreto) throws Exception {
        long periodo = Math.floorDiv(Instant.now().getEpochSecond(), 30);
        return new DefaultCodeGenerator(HashingAlgorithm.SHA1, 6).generate(secreto, periodo);
    }

    private MvcResult activar(String bearer, String codigo) throws Exception {
        return mockMvc.perform(post("/api/v1/auth/2fa/activar")
                        .header("Authorization", "Bearer " + bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"" + codigo + "\"}"))
                .andReturn();
    }

    private int qr(String bearer) throws Exception {
        return mockMvc.perform(get("/api/v1/auth/2fa/qr").header("Authorization", "Bearer " + bearer))
                .andReturn().getResponse().getStatus();
    }

    // --- Pruebas ---

    @Test
    @DisplayName("Admin sin 2FA: el login no entrega JWT, solo el token de configuración con etapa CONFIGURAR")
    void adminWithout2faGetsNoJwt() throws Exception {
        String json = loginJson(crearUsuario("ROLE_ADMIN"));

        assertTrue((Boolean) JsonPath.read(json, "$.requiere2fa"));
        assertNull(JsonPath.read(json, "$.token"));
        assertEquals("CONFIGURAR", JsonPath.read(json, "$.etapa2fa"));
        assertNotNull(JsonPath.read(json, "$.tokenTemporal"));
    }

    @Test
    @DisplayName("Flujo completo: login → QR → activar → JWT definitivo; el siguiente login ya pide el código")
    void fullSetupFlow() throws Exception {
        Usuario admin = crearUsuario("ROLE_ADMIN");
        String tokenConfiguracion = JsonPath.read(loginJson(admin), "$.tokenTemporal");

        // QR con el token de configuración
        MvcResult qr = mockMvc.perform(get("/api/v1/auth/2fa/qr").header("Authorization", "Bearer " + tokenConfiguracion))
                .andReturn();
        assertEquals(200, qr.getResponse().getStatus());
        String secreto = qr.getResponse().getHeader("X-2FA-Secret");
        assertNotNull(secreto);

        // Activar con el primer código: entrega el JWT definitivo
        MvcResult activado = activar(tokenConfiguracion, codigoActual(secreto));
        assertEquals(200, activado.getResponse().getStatus());
        String json = activado.getResponse().getContentAsString();
        String jwt = JsonPath.read(json, "$.token");
        assertNotNull(jwt);
        assertEquals("Bearer", JsonPath.read(json, "$.tipo"));
        assertFalse((Boolean) JsonPath.read(json, "$.requiere2fa"));
        assertNull(jwtPort.extraerEtapa(jwt));
        assertTrue(jwtPort.extraerRoles(jwt).contains("ROLE_ADMIN"));
        assertTrue(usuarioRepository.buscarPorId(admin.getId()).orElseThrow().is2faEnabled());

        // El JWT definitivo sirve como sesión normal
        assertEquals(200, mockMvc.perform(get("/api/v1/servicios").header("Authorization", "Bearer " + jwt))
                .andReturn().getResponse().getStatus());

        // El mismo token de configuración ya no sirve: el 2FA quedó activo
        assertEquals(409, qr(tokenConfiguracion));

        // El siguiente login ya pide el código (etapa VERIFICAR)
        assertEquals("VERIFICAR", JsonPath.read(loginJson(admin), "$.etapa2fa"));
    }

    @Test
    @DisplayName("Admin CON 2FA activo: un token de configuración en /qr y /activar es rechazado y su secreto no cambia")
    void adminWith2faRejectsSetupToken() throws Exception {
        Usuario admin = adminCon2faActivo("JBSWY3DPEHPK3PXP");
        String tokenConfiguracion = jwtPort.generarTokenConfiguracion2fa(admin);

        assertEquals(409, qr(tokenConfiguracion));
        MvcResult activado = activar(tokenConfiguracion, codigoActual("JBSWY3DPEHPK3PXP"));
        assertEquals(409, activado.getResponse().getStatus());
        assertFalse(activado.getResponse().getContentAsString().contains("\"token\""));

        Usuario despues = usuarioRepository.buscarPorId(admin.getId()).orElseThrow();
        assertEquals("JBSWY3DPEHPK3PXP", despues.getSecret2fa());
        assertTrue(despues.is2faEnabled());
    }

    @Test
    @DisplayName("Con el JWT normal, GET /qr responde 409 si el 2FA ya está activo y no cambia el secreto")
    void normalJwtQrBlockedWhenActive() throws Exception {
        Usuario admin = adminCon2faActivo("JBSWY3DPEHPK3PXP");

        assertEquals(409, qr(jwtPort.generarToken(admin)));
        assertEquals("JBSWY3DPEHPK3PXP", usuarioRepository.buscarPorId(admin.getId()).orElseThrow().getSecret2fa());
    }

    @Test
    @DisplayName("Con el JWT normal y el 2FA inactivo, GET /qr sigue funcionando como hoy")
    void normalJwtQrWorksWhenInactive() throws Exception {
        Usuario cliente = crearUsuario("ROLE_CLIENTE");

        assertEquals(200, qr(jwtPort.generarToken(cliente)));
    }

    @Test
    @DisplayName("El token de configuración no sirve en otros endpoints ni en /2fa/verificar-login")
    void setupTokenUselessElsewhere() throws Exception {
        String tokenConfiguracion = JsonPath.read(loginJson(crearUsuario("ROLE_ADMIN")), "$.tokenTemporal");

        assertEquals(401, mockMvc.perform(get("/api/v1/servicios").header("Authorization", "Bearer " + tokenConfiguracion))
                .andReturn().getResponse().getStatus());
        assertEquals(401, mockMvc.perform(post("/api/v1/auth/2fa/verificar-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tokenTemporal\":\"" + tokenConfiguracion + "\",\"codigo\":\"123456\"}"))
                .andReturn().getResponse().getStatus());
    }

    @Test
    @DisplayName("El token temporal de verificación no sirve en /qr ni en /activar")
    void verificationTokenUselessOnSetupRoutes() throws Exception {
        Usuario admin = adminCon2faActivo("JBSWY3DPEHPK3PXP");
        String tokenVerificacion = JsonPath.read(loginJson(admin), "$.tokenTemporal");

        assertEquals(401, qr(tokenVerificacion));
        assertEquals(401, activar(tokenVerificacion, "123456").getResponse().getStatus());
    }

    @Test
    @DisplayName("POST /activar sigue cerrado para un cliente con JWT normal")
    void activarStillClosedForClient() throws Exception {
        Usuario cliente = crearUsuario("ROLE_CLIENTE");

        assertEquals(403, activar(jwtPort.generarToken(cliente), "123456").getResponse().getStatus());
    }

    @ParameterizedTest
    @ValueSource(strings = {"ROLE_CLIENTE", "ROLE_PROVEEDOR"})
    @DisplayName("Cliente y proveedor sin 2FA siguen entrando con el JWT, sin etapa 2FA")
    void clientAndProviderUnchanged(String rol) throws Exception {
        String json = loginJson(crearUsuario(rol));

        assertFalse((Boolean) JsonPath.read(json, "$.requiere2fa"));
        assertNotNull(JsonPath.read(json, "$.token"));
        assertNull(JsonPath.read(json, "$.tokenTemporal"));
        assertNull(JsonPath.read(json, "$.etapa2fa"));
    }
}
