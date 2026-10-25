package com.service_reservation.EAP03.modulos.recursos.infrastructure.inbound;

import com.service_reservation.EAP03.modulos.recursos.domain.exception.DatosInvalidosException;
import com.service_reservation.EAP03.modulos.recursos.domain.model.BloqueoRecurso;
import com.service_reservation.EAP03.modulos.recursos.domain.model.DiaSemana;
import com.service_reservation.EAP03.modulos.recursos.domain.model.HorarioRecurso;
import com.service_reservation.EAP03.modulos.recursos.domain.model.Recurso;
import com.service_reservation.EAP03.modulos.recursos.domain.model.RecursoConHorarios;
import com.service_reservation.EAP03.modulos.recursos.domain.model.RegistrarBloqueoComando;
import com.service_reservation.EAP03.modulos.recursos.domain.model.RegistrarHorarioComando;
import com.service_reservation.EAP03.modulos.recursos.domain.model.ResultadoPaginado;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.in.ConsultarDisponibilidadProveedorUseCase;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.in.EliminarBloqueoRecursoUseCase;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.in.EliminarHorarioRecursoUseCase;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.in.ListarBloqueosRecursoUseCase;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.in.ListarHorariosRecursoUseCase;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.in.RegistrarBloqueoRecursoUseCase;
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
    private static final LocalDateTime FIN = INICIO.plusHours(2);

    @Mock private RegistrarHorarioRecursoUseCase registrarHorarioRecursoUseCase;
    @Mock private ListarHorariosRecursoUseCase listarHorariosRecursoUseCase;
    @Mock private EliminarHorarioRecursoUseCase eliminarHorarioRecursoUseCase;
    @Mock private RegistrarBloqueoRecursoUseCase registrarBloqueoRecursoUseCase;
    @Mock private ListarBloqueosRecursoUseCase listarBloqueosRecursoUseCase;
    @Mock private EliminarBloqueoRecursoUseCase eliminarBloqueoRecursoUseCase;
    @Mock private ConsultarDisponibilidadProveedorUseCase consultarDisponibilidadProveedorUseCase;
    @Mock private RecursoSecurity recursoSecurity;
    @Mock private Authentication authentication;

    @InjectMocks private DisponibilidadRecursoController controller;

    private static HorarioRecurso monday() {
        return HorarioRecurso.reconstruir(5, 1, DiaSemana.LUNES, LocalTime.of(8, 0), LocalTime.of(12, 30));
    }

    private static BloqueoRecurso block() {
        return BloqueoRecurso.reconstruir(8, 1, INICIO, FIN, "Mantenimiento", INICIO.minusDays(1));
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
    @DisplayName("Registrar bloqueo devuelve el bloqueo guardado")
    void registersBlock() {
        // Arrange
        when(registrarBloqueoRecursoUseCase.ejecutar(new RegistrarBloqueoComando(1, INICIO, FIN, "Mantenimiento")))
                .thenReturn(block());

        // Act
        BloqueoRecursoResponseDTO response = controller.registrarBloqueo(1,
                new RegistrarBloqueoRequestDTO(INICIO, FIN, "Mantenimiento"));

        // Assert
        assertEquals(new BloqueoRecursoResponseDTO(8, 1, INICIO, FIN, "Mantenimiento", INICIO.minusDays(1)), response);
    }

    @Test
    @DisplayName("Listar bloqueos devuelve la página de bloqueos del recurso")
    void listsBlocks() {
        // Arrange
        when(listarBloqueosRecursoUseCase.ejecutar(1, 0, 20)).thenReturn(new ResultadoPaginado<>(List.of(block()), 0, 20, 1L));

        // Act
        PaginaResponseDTO<BloqueoRecursoResponseDTO> pagina = controller.listarBloqueos(1, 0, 20);

        // Assert
        assertEquals(8, pagina.contenido().get(0).id());
        assertEquals(1L, pagina.totalElementos());
        assertEquals(1, pagina.totalPaginas());
    }

    @Test
    @DisplayName("Eliminar bloqueo delega en el caso de uso")
    void deletesBlock() {
        controller.eliminarBloqueo(1, 8);

        verify(eliminarBloqueoRecursoUseCase).ejecutar(1, 8);
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
