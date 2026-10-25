package com.service_reservation.EAP03.modulos.recursos.application;

import com.service_reservation.EAP03.modulos.recursos.domain.exception.HorarioNoEncontradoException;
import com.service_reservation.EAP03.modulos.recursos.domain.model.DiaSemana;
import com.service_reservation.EAP03.modulos.recursos.domain.model.HorarioRecurso;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.out.HorarioRecursoRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EliminarHorarioRecursoServiceTest {

    @Mock private HorarioRecursoRepositoryPort horarioRecursoRepositoryPort;

    @InjectMocks private EliminarHorarioRecursoService service;

    private static HorarioRecurso scheduleOf(int idRecurso) {
        return HorarioRecurso.reconstruir(5, idRecurso, DiaSemana.LUNES, LocalTime.of(8, 0), LocalTime.of(12, 0));
    }

    @Test
    @DisplayName("Elimina el horario cuando pertenece al recurso")
    void deletesOwnSchedule() {
        when(horarioRecursoRepositoryPort.buscarPorId(5)).thenReturn(Optional.of(scheduleOf(1)));

        service.ejecutar(1, 5);

        verify(horarioRecursoRepositoryPort).eliminar(5);
    }

    @Test
    @DisplayName("Un horario de otro recurso se trata como inexistente")
    void scheduleOfAnotherResourceIsNotFound() {
        when(horarioRecursoRepositoryPort.buscarPorId(5)).thenReturn(Optional.of(scheduleOf(2)));

        assertThrows(HorarioNoEncontradoException.class, () -> service.ejecutar(1, 5));
        verify(horarioRecursoRepositoryPort, never()).eliminar(any());
    }

    @Test
    @DisplayName("Falla si el horario no existe")
    void unknownSchedule() {
        when(horarioRecursoRepositoryPort.buscarPorId(5)).thenReturn(Optional.empty());

        assertThrows(HorarioNoEncontradoException.class, () -> service.ejecutar(1, 5));
    }
}
