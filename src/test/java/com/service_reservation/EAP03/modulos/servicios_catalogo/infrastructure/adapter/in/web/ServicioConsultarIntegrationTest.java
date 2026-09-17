package com.service_reservation.EAP03.modulos.servicios_catalogo.infrastructure.adapter.in.web;

import com.service_reservation.EAP03.modulos.servicios_catalogo.domain.model.Servicio;
import com.service_reservation.EAP03.modulos.servicios_catalogo.domain.repository.ServicioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class ServicioConsultarIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private ServicioRepository servicioRepository;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("GET /api/v1/servicios sin parámetros debe retornar 200 OK y la lista paginada sin errores de tipo")
    void debeConsultarServiciosSinParametrosRetornando200() throws Exception {
        servicioRepository.guardar(Servicio.crearNuevo(1, "Mantenimiento Preventivo", 60));

        mockMvc.perform(get("/api/v1/servicios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido").isArray())
                .andExpect(jsonPath("$.contenido[0].nombre").value("Mantenimiento Preventivo"))
                .andExpect(jsonPath("$.totalElementos").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/servicios con filtro por nombre debe retornar 200 OK")
    void debeConsultarServiciosConFiltroNombre() throws Exception {
        servicioRepository.guardar(Servicio.crearNuevo(1, "Limpieza Profunda", 30));

        mockMvc.perform(get("/api/v1/servicios").param("nombre", "Limpieza"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido[0].nombre").value("Limpieza Profunda"));
    }
}
