package com.service_reservation.EAP03.modulos.servicios_catalogo.infrastructure.adapter.in.web;

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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ServicioControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CrearServicioUseCase crearServicioUseCase;

    @BeforeEach
    void setUp() {
        ServicioController controller = new ServicioController(crearServicioUseCase);
        this.mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new ServicioExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/servicios debe retornar 201 Created cuando la petición es válida")
    void debeCrearServicioRetornando201() throws Exception {
        ServicioResponse response = new ServicioResponse(1, 5, "Cambio de Aceite", 45, OffsetDateTime.now());

        when(crearServicioUseCase.ejecutar(any(CrearServicioRequest.class))).thenReturn(response);

        String jsonValido = """
                {
                    "idProveedor": 5,
                    "nombre": "Cambio de Aceite",
                    "duracionMinutos": 45
                }
                """;

        mockMvc.perform(post("/api/v1/servicios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonValido))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/servicios/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.idProveedor").value(5))
                .andExpect(jsonPath("$.nombre").value("Cambio de Aceite"))
                .andExpect(jsonPath("$.duracionMinutos").value(45));
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
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validación Fallida"))
                .andExpect(jsonPath("$.errores.idProveedor").exists())
                .andExpect(jsonPath("$.errores.nombre").exists())
                .andExpect(jsonPath("$.errores.duracionMinutos").exists());
    }
}
