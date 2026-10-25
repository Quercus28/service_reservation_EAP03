package com.service_reservation.EAP03.modulos.agendas_horarios.infrastructure.outbound.adapter;

import com.service_reservation.EAP03.modulos.agendas_horarios.domain.model.Agenda;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.model.DiaSemana;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.model.Horario;
import com.service_reservation.EAP03.modulos.agendas_horarios.infrastructure.outbound.persistence.AgendaMapper;
import com.service_reservation.EAP03.modulos.agendas_horarios.infrastructure.outbound.persistence.entity.AgendaJpaEntity;
import com.service_reservation.EAP03.modulos.agendas_horarios.infrastructure.outbound.persistence.entity.HorarioJpaEntity;
import com.service_reservation.EAP03.modulos.agendas_horarios.infrastructure.outbound.persistence.repository.AgendaSpringDataRepository;
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
class PersistenciaAgendasHorariosAdapterTest {

    private static final LocalTime OCHO = LocalTime.of(8, 0);
    private static final LocalTime DOCE = LocalTime.of(12, 0);

    @Mock private AgendaSpringDataRepository repository;

    private PersistenciaAgendasHorariosAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new PersistenciaAgendasHorariosAdapter(repository, new AgendaMapper());
    }

    private static AgendaJpaEntity entity() {
        AgendaJpaEntity agenda = new AgendaJpaEntity();
        agenda.setId(5L);
        agenda.setIdProveedor(7);
        agenda.setIdServicio(10);
        agenda.setActiva(true);
        HorarioJpaEntity horario = new HorarioJpaEntity();
        horario.setId(1L);
        horario.setAgenda(agenda);
        horario.setDiaSemana(DiaSemana.LUNES);
        horario.setHoraInicio(OCHO);
        horario.setHoraFin(DOCE);
        agenda.setHorarios(List.of(horario));
        return agenda;
    }

    @Test
    @DisplayName("Guardar convierte la agenda y sus horarios a entidades y devuelve la agenda guardada")
    void savesAgenda() {
        // Arrange
        when(repository.save(any())).thenReturn(entity());
        Agenda agenda = new Agenda(null, 7, 10, false, List.of(new Horario(DiaSemana.LUNES, OCHO, DOCE)));

        // Act
        Agenda guardada = adapter.guardar(agenda);

        // Assert
        ArgumentCaptor<AgendaJpaEntity> captor = ArgumentCaptor.forClass(AgendaJpaEntity.class);
        verify(repository).save(captor.capture());
        AgendaJpaEntity enviada = captor.getValue();
        assertNull(enviada.getId());
        assertEquals(7, enviada.getIdProveedor());
        assertEquals(10, enviada.getIdServicio());
        assertFalse(enviada.isActiva());
        HorarioJpaEntity horario = enviada.getHorarios().get(0);
        assertNull(horario.getId());
        assertSame(enviada, horario.getAgenda());
        assertEquals(DiaSemana.LUNES, horario.getDiaSemana());
        assertEquals(OCHO, horario.getHoraInicio());
        assertEquals(DOCE, horario.getHoraFin());
        assertEquals(5L, guardada.getId());
        assertEquals(List.of(new Horario(1L, DiaSemana.LUNES, OCHO, DOCE)), guardada.getHorarios());
    }

    @Test
    @DisplayName("Una entidad nueva queda activa y sin horarios")
    void newEntityDefaults() {
        AgendaJpaEntity agenda = new AgendaJpaEntity();

        assertTrue(agenda.isActiva());
        assertTrue(agenda.getHorarios().isEmpty());
    }

    @Test
    @DisplayName("Busca la agenda activa del proveedor por id")
    void findsAgendaOfProvider() {
        when(repository.findByIdAndIdProveedorAndActivaTrue(5L, 7)).thenReturn(Optional.of(entity()));
        when(repository.findByIdAndIdProveedorAndActivaTrue(6L, 7)).thenReturn(Optional.empty());

        assertEquals(10, adapter.buscarPorIdYProveedor(5L, 7).orElseThrow().getServicioId());
        assertTrue(adapter.buscarPorIdYProveedor(6L, 7).isEmpty());
    }

    @Test
    @DisplayName("Busca la agenda activa de un servicio")
    void findsActiveAgendaOfService() {
        when(repository.findByIdServicioAndActivaTrue(10)).thenReturn(Optional.of(entity()));

        assertEquals(5L, adapter.buscarAgendaActivaPorServicio(10).orElseThrow().getId());
    }

    @Test
    @DisplayName("Consulta si el servicio ya tiene una agenda activa")
    void checksActiveAgenda() {
        when(repository.existsByIdServicioAndActivaTrue(10)).thenReturn(true);

        assertTrue(adapter.existeAgendaActivaPorServicio(10));
    }
}
