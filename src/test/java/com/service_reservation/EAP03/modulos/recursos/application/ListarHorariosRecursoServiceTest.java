package com.service_reservation.EAP03.modulos.recursos.application;

import com.service_reservation.EAP03.modulos.recursos.domain.exception.RecursoNoEncontradoException;
import com.service_reservation.EAP03.modulos.recursos.domain.model.DiaSemana;
import com.service_reservation.EAP03.modulos.recursos.domain.model.HorarioRecurso;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.out.HorarioRecursoRepositoryPort;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.out.RecursoRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListarHorariosRecursoServiceTest {

    @Mock private RecursoRepositoryPort recursoRepositoryPort;
    @Mock private HorarioRecursoRepositoryPort horarioRecursoRepositoryPort;

    @InjectMocks private ListarHorariosRecursoService service;

    @Test
    @DisplayName("Devuelve los horarios del recurso")
    void returnsSchedules() {
        List<HorarioRecurso> horarios = List.of(HorarioRecurso.reconstruir(2, 1, DiaSemana.LUNES, LocalTime.of(8, 0), LocalTime.of(12, 0)));
        when(recursoRepositoryPort.existePorId(1)).thenReturn(true);
        when(horarioRecursoRepositoryPort.buscarPorRecurso(1)).thenReturn(horarios);

        assertEquals(horarios, service.ejecutar(1));
    }

    @Test
    @DisplayName("Falla si el recurso no existe")
    void unknownResource() {
        when(recursoRepositoryPort.existePorId(1)).thenReturn(false);

        assertThrows(RecursoNoEncontradoException.class, () -> service.ejecutar(1));
        verifyNoInteractions(horarioRecursoRepositoryPort);
    }
}
