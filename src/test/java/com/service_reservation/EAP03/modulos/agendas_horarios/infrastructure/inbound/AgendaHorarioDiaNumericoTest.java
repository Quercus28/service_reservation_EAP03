package com.service_reservation.EAP03.modulos.agendas_horarios.infrastructure.inbound;

import com.service_reservation.EAP03.config.GlobalExceptionHandler;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.model.Agenda;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.model.DiaSemana;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.model.Horario;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.ports.in.IDisponibilidad;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.ports.in.IGestionAgendas;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.ports.in.IGestionHorarios;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.in.ConsultarProveedorUseCase;
import com.service_reservation.EAP03.modulos.identidad.infrastructure.inbound.security.UsuarioAutenticadoPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Usa el JsonMapper real de la aplicación: el de un MockMvc independiente no trae su configuración.
@SpringBootTest
class AgendaHorarioDiaNumericoTest {

    private static final String DIAS_PERMITIDOS = "El valor enviado no es válido. "
            + "Valores permitidos: LUNES, MARTES, MIERCOLES, JUEVES, VIERNES, SABADO, DOMINGO";

    @Autowired private JsonMapper jsonMapper;

    private final IGestionHorarios gestionHorarios = mock(IGestionHorarios.class);
    private final ConsultarProveedorUseCase consultarProveedor = mock(ConsultarProveedorUseCase.class);

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        AgendaController controller = new AgendaController(
                mock(IGestionAgendas.class), gestionHorarios, mock(IDisponibilidad.class), consultarProveedor);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler(null))
                .setMessageConverters(new JacksonJsonHttpMessageConverter(jsonMapper))
                .build();

        Horario lunes = new Horario(1L, DiaSemana.LUNES, LocalTime.of(8, 0), LocalTime.of(12, 0));
        when(consultarProveedor.buscarIdProveedorPorUsuario(20L)).thenReturn(Optional.of(7));
        when(gestionHorarios.agregarHorario(anyLong(), anyInt(), any()))
                .thenReturn(new Agenda(5L, 7, 10, true, List.of(lunes)));
    }

    private ResultActions crearHorarioConDia(String dia) throws Exception {
        UsernamePasswordAuthenticationToken proveedor = new UsernamePasswordAuthenticationToken(
                new UsuarioAutenticadoPrincipal(20L, "prov@test.com", List.of("PROVEEDOR")), null, List.of());
        return mockMvc.perform(post("/api/v1/agendas/5/horarios")
                .principal(proveedor)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"diaSemana\": " + dia + ", \"horaInicio\": \"08:00\", \"horaFin\": \"12:00\"}"));
    }

    private void assertDiaRechazado(ResultActions respuesta) throws Exception {
        respuesta.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDACION_ENTRADA"))
                .andExpect(jsonPath("$.details.diaSemana").value(DIAS_PERMITIDOS));
        verifyNoInteractions(gestionHorarios);
    }

    @Test
    @DisplayName("diaSemana 0 se rechaza y no crea el horario")
    void dayZeroIsRejected() throws Exception {
        assertDiaRechazado(crearHorarioConDia("0"));
    }

    @Test
    @DisplayName("diaSemana 9 se rechaza y no crea el horario")
    void dayNineIsRejected() throws Exception {
        assertDiaRechazado(crearHorarioConDia("9"));
    }

    @Test
    @DisplayName("diaSemana \"0\", un número escrito como texto, se rechaza y no crea el horario")
    void dayZeroAsTextIsRejected() throws Exception {
        assertDiaRechazado(crearHorarioConDia("\"0\""));
    }

    @Test
    @DisplayName("diaSemana con un nombre válido sigue creando el horario")
    void validDayNameIsAccepted() throws Exception {
        crearHorarioConDia("\"LUNES\"").andExpect(status().isCreated());
    }
}
