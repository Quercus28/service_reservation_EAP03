package com.service_reservation.EAP03.modulos.recursos.application;

import com.service_reservation.EAP03.modulos.recursos.domain.exception.HorarioSolapadoException;
import com.service_reservation.EAP03.modulos.recursos.domain.exception.RecursoNoEncontradoException;
import com.service_reservation.EAP03.modulos.recursos.domain.model.DiaSemana;
import com.service_reservation.EAP03.modulos.recursos.domain.model.HorarioRecurso;
import com.service_reservation.EAP03.modulos.recursos.domain.model.Recurso;
import com.service_reservation.EAP03.modulos.recursos.domain.model.RegistrarHorarioComando;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.out.HorarioRecursoRepositoryPort;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.out.RecursoRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrarHorarioRecursoServiceTest {

    private static final RegistrarHorarioComando COMANDO =
            new RegistrarHorarioComando(1, DiaSemana.LUNES, LocalTime.of(8, 0), LocalTime.of(12, 0));

    @Mock private RecursoRepositoryPort recursoRepositoryPort;
    @Mock private HorarioRecursoRepositoryPort horarioRecursoRepositoryPort;

    @InjectMocks private RegistrarHorarioRecursoService service;

    private void existingResource() {
        when(recursoRepositoryPort.buscarPorIdConBloqueo(1)).thenReturn(Optional.of(Recurso.nuevo(7, "Camilla", BigDecimal.TEN, 3)));
    }

    @Test
    @DisplayName("Guarda la franja cuando no se solapa con las existentes")
    void savesNonOverlappingSchedule() {
        // Arrange
        existingResource();
        when(horarioRecursoRepositoryPort.buscarPorRecurso(1)).thenReturn(List.of(
                HorarioRecurso.reconstruir(2, 1, DiaSemana.LUNES, LocalTime.of(12, 0), LocalTime.of(16, 0)),
                HorarioRecurso.reconstruir(3, 1, DiaSemana.MARTES, LocalTime.of(8, 0), LocalTime.of(12, 0))));
        when(horarioRecursoRepositoryPort.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        HorarioRecurso horario = service.ejecutar(COMANDO);

        // Assert
        assertEquals(DiaSemana.LUNES, horario.getDiaSemana());
        assertEquals(LocalTime.of(8, 0), horario.getHoraInicio());
        assertEquals(LocalTime.of(12, 0), horario.getHoraFin());
    }

    @Test
    @DisplayName("Rechaza una franja que se solapa con otra del mismo día")
    void rejectsOverlappingSchedule() {
        existingResource();
        when(horarioRecursoRepositoryPort.buscarPorRecurso(1)).thenReturn(List.of(
                HorarioRecurso.reconstruir(2, 1, DiaSemana.LUNES, LocalTime.of(11, 0), LocalTime.of(14, 0))));

        assertThrows(HorarioSolapadoException.class, () -> service.ejecutar(COMANDO));
        verify(horarioRecursoRepositoryPort, never()).guardar(any());
    }

    @Test
    @DisplayName("Falla si el recurso no existe")
    void unknownResource() {
        when(recursoRepositoryPort.buscarPorIdConBloqueo(1)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.ejecutar(COMANDO));
        verifyNoInteractions(horarioRecursoRepositoryPort);
    }
}
