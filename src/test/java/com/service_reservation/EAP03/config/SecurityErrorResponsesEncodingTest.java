package com.service_reservation.EAP03.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
class SecurityErrorResponsesEncodingTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("El 401 sin token se envía en UTF-8 y conserva la tilde de 'autenticación'")
    void unauthorizedResponseIsUtf8() throws Exception {
        // Arrange: petición a una ruta protegida sin cabecera Authorization

        // Act
        MvcResult result = mockMvc.perform(get("/api/v1/servicios")).andReturn();

        // Assert
        assertEquals(401, result.getResponse().getStatus());
        assertTrue(result.getResponse().getContentType().toUpperCase().contains("CHARSET=UTF-8"));
        assertTrue(result.getResponse().getContentAsString(StandardCharsets.UTF_8).contains("autenticación"));
    }

    @Test
    @DisplayName("El 403 por rol se envía en UTF-8 y conserva la tilde de 'acción'")
    void forbiddenResponseIsUtf8() throws Exception {
        // Arrange: un CLIENTE en una ruta que exige el rol PROVEEDOR

        // Act
        MvcResult result = mockMvc.perform(get("/api/v1/agendas").with(user("cliente").roles("CLIENTE")))
                .andReturn();

        // Assert
        assertEquals(403, result.getResponse().getStatus());
        assertTrue(result.getResponse().getContentType().toUpperCase().contains("CHARSET=UTF-8"));
        assertTrue(result.getResponse().getContentAsString(StandardCharsets.UTF_8).contains("acción"));
    }
}
