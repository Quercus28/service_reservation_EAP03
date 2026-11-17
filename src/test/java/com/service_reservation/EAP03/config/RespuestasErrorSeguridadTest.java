package com.service_reservation.EAP03.config;

import com.service_reservation.EAP03.modulos.admin.domain.model.IntentoAccesoDenegado;
import com.service_reservation.EAP03.modulos.admin.domain.ports.in.RegistrarAccesoDenegadoUseCase;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.out.JwtPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.databind.json.JsonMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

// Va contra un servidor real para que los filtros corran en su orden de verdad: si TraceIdFilter
// corriera después de la seguridad, el traceId de la respuesta no sería el enviado en la cabecera.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RespuestasErrorSeguridadTest {

    private static final String TRACE_ID = "3f2b8c1e-9a4d-4e7b-8f10-5c6d7e8f9a0b";
    private static final String TOKEN_CLIENTE = "token-cliente";
    private static final String MENSAJE_403 = "No tiene privilegios para realizar esta operación";

    private final HttpClient cliente = HttpClient.newHttpClient();

    @Value("${local.server.port}")
    private int puerto;

    @MockitoBean private JwtPort jwtPort;
    @MockitoBean private RegistrarAccesoDenegadoUseCase auditoria;

    @BeforeEach
    void usuarioCliente() {
        when(jwtPort.validarToken(TOKEN_CLIENTE)).thenReturn(true);
        when(jwtPort.esTokenTemporal2faValido(TOKEN_CLIENTE)).thenReturn(false);
        when(jwtPort.extraerEmail(TOKEN_CLIENTE)).thenReturn("cliente@test.com");
        when(jwtPort.extraerUsuarioId(TOKEN_CLIENTE)).thenReturn(7L);
        when(jwtPort.extraerRoles(TOKEN_CLIENTE)).thenReturn(List.of("CLIENTE"));
    }

    private HttpResponse<String> pedir(String ruta, String token) throws Exception {
        HttpRequest.Builder peticion = HttpRequest.newBuilder(URI.create("http://localhost:" + puerto + ruta))
                .header(TraceIdFilter.CABECERA, TRACE_ID);
        if (token != null) {
            peticion.header("Authorization", "Bearer " + token);
        }
        return cliente.send(peticion.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    // Compara el cuerpo completo: si sobrara timestamp, path o cualquier detalle, la prueba falla.
    private static void assertFormatoEstandar(HttpResponse<String> respuesta, int estado, String errorCode, String mensaje) {
        assertEquals(estado, respuesta.statusCode());
        assertTrue(respuesta.headers().firstValue("Content-Type").orElseThrow().startsWith("application/json"));
        assertEquals(Map.of("errorCode", errorCode, "message", mensaje, "traceId", TRACE_ID),
                JsonMapper.shared().readValue(respuesta.body(), Map.class));
    }

    @Test
    @DisplayName("Sin token responde 401 con el formato estándar")
    void noTokenReturnsStandard401() throws Exception {
        assertFormatoEstandar(pedir("/api/v1/servicios", null), 401, "NO_AUTENTICADO",
                "Se requiere autenticación para acceder a este recurso.");
    }

    @Test
    @DisplayName("Con el rol equivocado responde 403 con el formato estándar y sin auditar fuera de /api/admin")
    void wrongRoleReturnsStandard403() throws Exception {
        assertFormatoEstandar(pedir("/api/v1/agendas/5", TOKEN_CLIENTE), 403, "ACCESO_NO_AUTORIZADO", MENSAJE_403);

        verify(auditoria, never()).registrar(any());
    }

    @Test
    @DisplayName("Un 403 en /api/admin responde con el mismo formato y queda en auditoría")
    void adminDeniedIsStandardAndAudited() throws Exception {
        assertFormatoEstandar(pedir("/api/admin/permisos/revocaciones", TOKEN_CLIENTE), 403,
                "ACCESO_NO_AUTORIZADO", MENSAJE_403);

        ArgumentCaptor<IntentoAccesoDenegado> intento = ArgumentCaptor.forClass(IntentoAccesoDenegado.class);
        verify(auditoria).registrar(intento.capture());
        assertEquals(7L, intento.getValue().idUsuario());
        assertEquals("GET", intento.getValue().metodo());
        assertEquals("/api/admin/permisos/revocaciones", intento.getValue().endpoint());
    }

    @Test
    @DisplayName("Una URL con ';' que rechaza el firewall responde 400 con el formato estándar")
    void firewallRejectionReturnsStandard400() throws Exception {
        assertFormatoEstandar(pedir("/api/v1/servicios/1%3bDROP%20TABLE%20servicio/disponibilidad", null),
                400, "PETICION_RECHAZADA", "La petición no es válida");
    }
}
