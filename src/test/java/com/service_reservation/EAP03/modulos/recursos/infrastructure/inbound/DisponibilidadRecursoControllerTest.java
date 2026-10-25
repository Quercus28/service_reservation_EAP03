package com.service_reservation.EAP03.modulos.recursos.infrastructure.inbound;

import com.service_reservation.EAP03.modulos.recursos.domain.exception.DatosInvalidosException;
import com.service_reservation.EAP03.modulos.recursos.domain.model.DiaSemana;
import com.service_reservation.EAP03.modulos.recursos.domain.model.HorarioRecurso;
import com.service_reservation.EAP03.modulos.recursos.domain.model.Recurso;
import com.service_reservation.EAP03.modulos.recursos.domain.model.RecursoConHorarios;
import com.service_reservation.EAP03.modulos.recursos.domain.model.RegistrarHorarioComando;
import com.service_reservation.EAP03.modulos.recursos.domain.model.ResultadoPaginado;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.in.ConsultarDisponibilidadProveedorUseCase;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.in.EliminarHorarioRecursoUseCase;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.in.ListarHorariosRecursoUseCase;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.in.RegistrarHorarioRecursoUseCase;
import com.service_reservation.EAP03.modulos.recursos.infrastructure.config.RecursoSecurity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DisponibilidadRecursoControllerTest {

    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 11, 1, 8, 0);

    @Mock private RegistrarHorarioRecursoUseCase registrarHorarioRecursoUseCase;
    @Mock private ListarHorariosRecursoUseCase listarHorariosRecursoUseCase;
    @Mock private EliminarHorarioRecursoUseCase eliminarHorarioRecursoUseCase;
    @Mock private ConsultarDisponibilidadProveedorUseCase consultarDisponibilidadProveedorUseCase;
    @Mock private RecursoSecurity recursoSecurity;
    @Mock private Authentication authentication;

    @InjectMocks private DisponibilidadRecursoController controller;

    private static HorarioRecurso monday() {
        return HorarioRecurso.reconstruir(5, 1, DiaSemana.LUNES, LocalTime.of(8, 0), LocalTime.of(12, 30));
    }

    @Test
    @DisplayName("Registrar horario convierte el día y las horas y devuelve la franja con formato HH:mm")
    void registersSchedule() {
        // Arrange
        when(registrarHorarioRecursoUseCase.ejecutar(
                new RegistrarHorarioComando(1, DiaSemana.LUNES, LocalTime.of(8, 0), LocalTime.of(12, 30))))
                .thenReturn(monday());

        // Act
        HorarioRecursoResponseDTO response = controller.registrarHorario(1,
                new RegistrarHorarioRequestDTO("LUNES", "08:00", "12:30"));

        // Assert
        assertEquals(new HorarioRecursoResponseDTO(5, 1, DiaSemana.LUNES, "08:00", "12:30"), response);
    }

    @Test
    @DisplayName("Listar horarios devuelve las franjas del recurso")
    void listsSchedules() {
        when(listarHorariosRecursoUseCase.ejecutar(1)).thenReturn(List.of(monday()));

        assertEquals(List.of(new HorarioRecursoResponseDTO(5, 1, DiaSemana.LUNES, "08:00", "12:30")), controller.listarHorarios(1));
    }

    @Test
    @DisplayName("Eliminar horario delega en el caso de uso")
    void deletesSchedule() {
        controller.eliminarHorario(1, 5);

        verify(eliminarHorarioRecursoUseCase).ejecutar(1, 5);
    }

    @Test
    @DisplayName("La disponibilidad del proveedor lista sus recursos con sus horarios")
    void listsProviderAvailability() {
        // Arrange
        Recurso recurso = Recurso.reconstruir(1, 7, "Camilla", BigDecimal.TEN, 3, INICIO, null);
        when(recursoSecurity.idProveedorDe(authentication)).thenReturn(Optional.of(7));
        when(consultarDisponibilidadProveedorUseCase.ejecutar(7, 0, 20))
                .thenReturn(new ResultadoPaginado<>(List.of(new RecursoConHorarios(recurso, List.of(monday()))), 0, 20, 1L));

        // Act
        PaginaResponseDTO<RecursoDisponibilidadResponseDTO> pagina = controller.consultarDisponibilidadProveedor(0, 20, authentication);

        // Assert
        assertEquals(new RecursoDisponibilidadResponseDTO(1, "Camilla", 3,
                List.of(new HorarioRecursoResponseDTO(5, 1, DiaSemana.LUNES, "08:00", "12:30"))), pagina.contenido().get(0));
        assertEquals(1, pagina.totalPaginas());
    }

    @Test
    @DisplayName("La disponibilidad del proveedor falla si el usuario no tiene perfil de proveedor")
    void providerAvailabilityFailsWithoutProfile() {
        when(recursoSecurity.idProveedorDe(authentication)).thenReturn(Optional.empty());

        assertThrows(DatosInvalidosException.class, () -> controller.consultarDisponibilidadProveedor(0, 20, authentication));
        verifyNoInteractions(consultarDisponibilidadProveedorUseCase);
    }
}
