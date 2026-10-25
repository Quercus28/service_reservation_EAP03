package com.service_reservation.EAP03.modulos.recursos.application;

import com.service_reservation.EAP03.modulos.recursos.domain.exception.DatosInvalidosException;
import com.service_reservation.EAP03.modulos.recursos.domain.model.DiaSemana;
import com.service_reservation.EAP03.modulos.recursos.domain.model.HorarioRecurso;
import com.service_reservation.EAP03.modulos.recursos.domain.model.Recurso;
import com.service_reservation.EAP03.modulos.recursos.domain.model.RecursoConHorarios;
import com.service_reservation.EAP03.modulos.recursos.domain.model.ResultadoPaginado;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.out.HorarioRecursoRepositoryPort;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.out.RecursoRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsultarDisponibilidadProveedorServiceTest {

    @Mock private RecursoRepositoryPort recursoRepositoryPort;
    @Mock private HorarioRecursoRepositoryPort horarioRecursoRepositoryPort;

    @InjectMocks private ConsultarDisponibilidadProveedorService service;

    private static Recurso resource(int id) {
        return Recurso.reconstruir(id, 7, "Recurso " + id, BigDecimal.TEN, 3, LocalDateTime.of(2026, 9, 1, 8, 0), null);
    }

    @Test
    @DisplayName("Agrupa los horarios de cada recurso de la página con una sola consulta")
    void groupsSchedulesByResource() {
        // Arrange
        when(recursoRepositoryPort.buscarActivosPorProveedor(7, 1, 2))
                .thenReturn(new ResultadoPaginado<>(List.of(resource(1), resource(2)), 1, 2, 5L));
        HorarioRecurso lunes = HorarioRecurso.reconstruir(10, 1, DiaSemana.LUNES, LocalTime.of(8, 0), LocalTime.of(12, 0));
        when(horarioRecursoRepositoryPort.buscarPorRecursos(List.of(1, 2))).thenReturn(List.of(lunes));

        // Act
        ResultadoPaginado<RecursoConHorarios> resultado = service.ejecutar(7, 1, 2);

        // Assert
        assertEquals(List.of(lunes), resultado.getContenido().get(0).horarios());
        assertTrue(resultado.getContenido().get(1).horarios().isEmpty());
        assertEquals(1, resultado.getPagina());
        assertEquals(2, resultado.getTamano());
        assertEquals(5L, resultado.getTotalElementos());
    }

    @Test
    @DisplayName("Con una página vacía no consulta horarios")
    void emptyPageDoesNotQuerySchedules() {
        when(recursoRepositoryPort.buscarActivosPorProveedor(7, 0, 20)).thenReturn(new ResultadoPaginado<>(List.of(), 0, 20, 0L));

        assertTrue(service.ejecutar(7, 0, 20).getContenido().isEmpty());
        verifyNoInteractions(horarioRecursoRepositoryPort);
    }

    @Test
    @DisplayName("Rechaza una página negativa o un tamaño menor que uno")
    void rejectsInvalidPaging() {
        assertThrows(DatosInvalidosException.class, () -> service.ejecutar(7, -1, 20));
        assertThrows(DatosInvalidosException.class, () -> service.ejecutar(7, 0, 0));
        verifyNoInteractions(recursoRepositoryPort, horarioRecursoRepositoryPort);
    }
}
