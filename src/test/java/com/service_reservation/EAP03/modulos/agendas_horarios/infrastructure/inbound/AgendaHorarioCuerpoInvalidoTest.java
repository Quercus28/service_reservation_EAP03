package com.service_reservation.EAP03.modulos.agendas_horarios.infrastructure.inbound;

import com.service_reservation.EAP03.config.GlobalExceptionHandler;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.ports.in.IDisponibilidad;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.ports.in.IGestionAgendas;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.ports.in.IGestionHorarios;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.in.ConsultarProveedorUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AgendaHorarioCuerpoInvalidoTest {

    @Mock private IGestionAgendas gestionAgendas;
    @Mock private IGestionHorarios gestionHorarios;
    @Mock private IDisponibilidad disponibilidad;
    @Mock private ConsultarProveedorUseCase consultarProveedor;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new AgendaController(gestionAgendas, gestionHorarios, disponibilidad, consultarProveedor))
                .setControllerAdvice(new GlobalExceptionHandler(null))
                .build();
    }

    private ResultActions crearHorario(String cuerpo) throws Exception {
        return mockMvc.perform(post("/api/v1/agendas/5/horarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(cuerpo));
    }

    @Test
    @DisplayName("Un diaSemana que no existe responde VALIDACION_ENTRADA nombrando el campo y los valores permitidos")
    void invalidDayReportsFieldAndAllowedValues() throws Exception {
        crearHorario("""
                {"diaSemana": "FUNDAY", "horaInicio": "08:00", "horaFin": "12:00"}
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDACION_ENTRADA"))
                .andExpect(jsonPath("$.message").value("Error en los datos enviados"))
                .andExpect(jsonPath("$.details.diaSemana").value("El valor enviado no es válido. "
                        + "Valores permitidos: LUNES, MARTES, MIERCOLES, JUEVES, VIERNES, SABADO, DOMINGO"))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(content().string(not(containsString("FUNDAY"))));

        verifyNoInteractions(gestionHorarios);
    }

    @Test
    @DisplayName("Un JSON roto sigue respondiendo CUERPO_NO_VALIDO")
    void brokenJsonKeepsBodyError() throws Exception {
        crearHorario("{\"diaSemana\": \"LUNES\",")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("CUERPO_NO_VALIDO"))
                .andExpect(jsonPath("$.message").value("El cuerpo de la petición no es válido"))
                .andExpect(jsonPath("$.details").doesNotExist());

        verifyNoInteractions(gestionHorarios);
    }
}
