package com.service_reservation.EAP03.modulos.servicios_catalogo.infrastructure.adapter.in.web;

import com.service_reservation.EAP03.config.GlobalExceptionHandler;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.in.ConsultarProveedorUseCase;
import com.service_reservation.EAP03.modulos.identidad.infrastructure.inbound.security.UsuarioAutenticadoPrincipal;
import com.service_reservation.EAP03.modulos.servicios_catalogo.application.dto.CrearServicioRequest;
import com.service_reservation.EAP03.modulos.servicios_catalogo.application.dto.ServicioResponse;
import com.service_reservation.EAP03.modulos.servicios_catalogo.application.usecase.CrearServicioUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ServicioControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CrearServicioUseCase crearServicioUseCase;

    @Mock
    private ConsultarProveedorUseCase consultarProveedorUseCase;

    @BeforeEach
    void setUp() {
        ServicioController controller = new ServicioController(
                crearServicioUseCase, null, null, null, null, consultarProveedorUseCase);
        this.mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new ServicioExceptionHandler(), new GlobalExceptionHandler(null))
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/servicios debe retornar 201 Created cuando la petición es válida")
    void debeCrearServicioRetornando201() throws Exception {
        ServicioResponse response = new ServicioResponse(1, 5, "Cambio de Aceite", 45, OffsetDateTime.now());

        // El usuario 10 tiene el perfil de proveedor 5
        UsuarioAutenticadoPrincipal principal =
                new UsuarioAutenticadoPrincipal(10L, "proveedor@test.com", List.of("PROVEEDOR"));
        UsernamePasswordAuthenticationToken autenticacion = new UsernamePasswordAuthenticationToken(
                principal, null, List.of(new SimpleGrantedAuthority("ROLE_PROVEEDOR")));

        when(consultarProveedorUseCase.buscarIdProveedorPorUsuario(10L)).thenReturn(Optional.of(5));
        when(crearServicioUseCase.ejecutar(any(CrearServicioRequest.class))).thenReturn(response);

        // El idProveedor del body (99) se ignora: manda el del proveedor autenticado (5)
        String jsonValido = """
                {
                    "idProveedor": 99,
                    "nombre": "Cambio de Aceite",
                    "duracionMinutos": 45
                }
                """;

        mockMvc.perform(post("/api/v1/servicios")
                .principal(autenticacion)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonValido))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/servicios/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.idProveedor").value(5))
                .andExpect(jsonPath("$.nombre").value("Cambio de Aceite"))
                .andExpect(jsonPath("$.duracionMinutos").value(45));

        verify(crearServicioUseCase).ejecutar(argThat(req -> Integer.valueOf(5).equals(req.idProveedor())));
    }

    @Test
    @DisplayName("POST /api/v1/servicios debe retornar 400 Bad Request cuando faltan campos o son inválidos")
    void debeRetornar400ConCamposInvalidos() throws Exception {
        String jsonInvalido = """
                {
                    "idProveedor": null,
                    "nombre": "",
                    "duracionMinutos": 0
                }
                """;

        mockMvc.perform(post("/api/v1/servicios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonInvalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDACION_ENTRADA"))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.details.idProveedor").exists())
                .andExpect(jsonPath("$.details.nombre").exists())
                .andExpect(jsonPath("$.details.duracionMinutos").exists());
    }
}
