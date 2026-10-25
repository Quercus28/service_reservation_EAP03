package com.service_reservation.EAP03.modulos.recursos.infrastructure.outbound.adapter;

import com.service_reservation.EAP03.modulos.recursos.domain.model.DiaSemana;
import com.service_reservation.EAP03.modulos.recursos.domain.model.HorarioRecurso;
import com.service_reservation.EAP03.modulos.recursos.infrastructure.outbound.persistence.HorarioRecursoMapper;
import com.service_reservation.EAP03.modulos.recursos.infrastructure.outbound.persistence.entity.HorarioRecursoJpaEntity;
import com.service_reservation.EAP03.modulos.recursos.infrastructure.outbound.persistence.repository.HorarioRecursoSpringDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HorarioRecursoRepositoryAdapterTest {

    @Mock private HorarioRecursoSpringDataRepository repositorio;

    private HorarioRecursoRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new HorarioRecursoRepositoryAdapter(repositorio, new HorarioRecursoMapper());
    }

    private static HorarioRecursoJpaEntity entity(Integer id, DiaSemana dia, int horaInicio) {
        HorarioRecursoJpaEntity entidad = new HorarioRecursoJpaEntity();
        entidad.setId(id);
        entidad.setIdRecurso(1);
        entidad.setDiaSemana(dia);
        entidad.setHoraInicio(LocalTime.of(horaInicio, 0));
        entidad.setHoraFin(LocalTime.of(horaInicio + 2, 0));
        return entidad;
    }

    @Test
    @DisplayName("Guardar convierte el horario a entidad y devuelve el horario guardado")
    void savesSchedule() {
        // Arrange
        when(repositorio.save(any())).thenReturn(entity(5, DiaSemana.LUNES, 8));

        // Act
        HorarioRecurso guardado = adapter.guardar(HorarioRecurso.nuevo(1, DiaSemana.LUNES, LocalTime.of(8, 0), LocalTime.of(10, 0)));

        // Assert
        ArgumentCaptor<HorarioRecursoJpaEntity> captor = ArgumentCaptor.forClass(HorarioRecursoJpaEntity.class);
        verify(repositorio).save(captor.capture());
        assertNull(captor.getValue().getId());
        assertEquals(1, captor.getValue().getIdRecurso());
        assertEquals(DiaSemana.LUNES, captor.getValue().getDiaSemana());
        assertEquals(LocalTime.of(8, 0), captor.getValue().getHoraInicio());
        assertEquals(LocalTime.of(10, 0), captor.getValue().getHoraFin());
        assertEquals(5, guardado.getId());
    }

    @Test
    @DisplayName("Busca un horario por id")
    void findsById() {
        when(repositorio.findById(5)).thenReturn(Optional.of(entity(5, DiaSemana.LUNES, 8)));

        assertEquals(DiaSemana.LUNES, adapter.buscarPorId(5).orElseThrow().getDiaSemana());
    }

    @Test
    @DisplayName("Ordena los horarios de un recurso por día de la semana y hora de inicio")
    void sortsSchedulesOfResourceByWeekOrder() {
        // Arrange
        when(repositorio.findByIdRecurso(1)).thenReturn(List.of(
                entity(1, DiaSemana.DOMINGO, 8),
                entity(2, DiaSemana.LUNES, 14),
                entity(3, DiaSemana.JUEVES, 8),
                entity(4, DiaSemana.LUNES, 8)));

        // Act
        List<HorarioRecurso> horarios = adapter.buscarPorRecurso(1);

        // Assert
        assertEquals(List.of(4, 2, 3, 1), horarios.stream().map(HorarioRecurso::getId).toList());
    }

    @Test
    @DisplayName("Ordena los horarios de varios recursos por día de la semana")
    void sortsSchedulesOfSeveralResources() {
        when(repositorio.findByIdRecursoIn(List.of(1, 2))).thenReturn(List.of(
                entity(1, DiaSemana.MARTES, 8),
                entity(2, DiaSemana.LUNES, 8)));

        assertEquals(List.of(2, 1), adapter.buscarPorRecursos(List.of(1, 2)).stream().map(HorarioRecurso::getId).toList());
    }

    @Test
    @DisplayName("Sin ids de recursos devuelve una lista vacía sin consultar la base de datos")
    void emptyIdsReturnEmptyList() {
        assertTrue(adapter.buscarPorRecursos(null).isEmpty());
        assertTrue(adapter.buscarPorRecursos(List.of()).isEmpty());
        verifyNoInteractions(repositorio);
    }

    @Test
    @DisplayName("Eliminar delega en el repositorio")
    void deletesSchedule() {
        adapter.eliminar(5);

        verify(repositorio).deleteById(5);
    }
}
