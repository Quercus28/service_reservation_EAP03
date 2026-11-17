package com.service_reservation.EAP03.modulos.reservas.infrastructure.inbound;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.net.URI;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Id de la reserva que falta o queda vacío en la ruta, con la aplicación completa (seguridad incluida).
@SpringBootTest
class ReservaIdRutaSecurityTest {

    @Autowired private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
    }

    @Test
    @DisplayName("PATCH /reservas/estado sin sesión responde 401, igual que el endpoint real")
    void missingIdWithoutSessionReturns401() throws Exception {
        mockMvc.perform(patch("/api/v1/reservas/estado")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"estado\":\"CONFIRMADO\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("NO_AUTENTICADO"));
    }

    @Test
    @DisplayName("PATCH /reservas/estado con sesión responde 400 DATOS_INVALIDOS: falta el identificador")
    void missingIdWithSessionReturns400() throws Exception {
        mockMvc.perform(patch("/api/v1/reservas/estado").with(user("proveedor").roles("PROVEEDOR"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"estado\":\"CONFIRMADO\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("DATOS_INVALIDOS"))
                .andExpect(jsonPath("$.message").value("El identificador de la reserva es obligatorio"))
                .andExpect(jsonPath("$.traceId").exists());
    }

    @Test
    @DisplayName("PATCH /reservas/%20/estado con sesión responde 400 FORMATO_PARAMETRO_INVALIDO con el formato estándar")
    void blankIdOnChangeStateReturns400() throws Exception {
        mockMvc.perform(patch(URI.create("/api/v1/reservas/%20/estado")).with(user("proveedor").roles("PROVEEDOR"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"estado\":\"CONFIRMADO\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("FORMATO_PARAMETRO_INVALIDO"))
                .andExpect(jsonPath("$.message").value("El parámetro 'id' tiene un formato o valor inválido"))
                .andExpect(jsonPath("$.traceId").exists());
    }

    @Test
    @DisplayName("POST /reservas/%20/cancelacion con sesión responde 400 FORMATO_PARAMETRO_INVALIDO con el formato estándar")
    void blankIdOnCancelReturns400() throws Exception {
        mockMvc.perform(post(URI.create("/api/v1/reservas/%20/cancelacion")).with(user("cliente").roles("CLIENTE")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("FORMATO_PARAMETRO_INVALIDO"))
                .andExpect(jsonPath("$.message").value("El parámetro 'id' tiene un formato o valor inválido"))
                .andExpect(jsonPath("$.traceId").exists());
    }
}
