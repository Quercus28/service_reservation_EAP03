package com.service_reservation.EAP03.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

// BUG-08 cerrado "Por diseño": el catálogo exige iniciar sesión. Estos tests evitan que se
// abra por error en el futuro.
@SpringBootTest
class CatalogoRequiereSesionSecurityTest {

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
    @DisplayName("TC-SC-21: el listado del catálogo sin token responde 401")
    void listRequiresLogin() throws Exception {
        // Arrange: petición sin cabecera Authorization

        // Act
        int status = mockMvc.perform(get("/api/v1/servicios")).andReturn().getResponse().getStatus();

        // Assert
        assertEquals(401, status);
    }

    @Test
    @DisplayName("El detalle de un servicio sin token responde 401")
    void detailRequiresLogin() throws Exception {
        // Arrange: petición sin cabecera Authorization

        // Act
        int status = mockMvc.perform(get("/api/v1/servicios/1")).andReturn().getResponse().getStatus();

        // Assert
        assertEquals(401, status);
    }

    @Test
    @DisplayName("Con sesión iniciada, un CLIENTE sí puede consultar el catálogo")
    void authenticatedClientCanList() throws Exception {
        // Arrange: usuario autenticado con rol CLIENTE

        // Act
        int status = mockMvc.perform(get("/api/v1/servicios").with(user("cliente").roles("CLIENTE")))
                .andReturn().getResponse().getStatus();

        // Assert
        assertEquals(200, status);
    }
}
