package com.service_reservation.EAP03.modulos.recursos.infrastructure.outbound.adapter;

import com.service_reservation.EAP03.modulos.recursos.domain.model.BloqueoRecurso;
import com.service_reservation.EAP03.modulos.recursos.domain.model.ResultadoPaginado;
import com.service_reservation.EAP03.modulos.recursos.infrastructure.outbound.persistence.BloqueoRecursoMapper;
import com.service_reservation.EAP03.modulos.recursos.infrastructure.outbound.persistence.entity.BloqueoRecursoJpaEntity;
import com.service_reservation.EAP03.modulos.recursos.infrastructure.outbound.persistence.repository.BloqueoRecursoSpringDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BloqueoRecursoRepositoryAdapterTest {

    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 11, 1, 8, 0);
    private static final LocalDateTime FIN = INICIO.plusHours(2);
    private static final LocalDateTime CREADO = INICIO.minusDays(1);

    @Mock private BloqueoRecursoSpringDataRepository repositorio;

    private BloqueoRecursoRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new BloqueoRecursoRepositoryAdapter(repositorio, new BloqueoRecursoMapper());
    }

    private static BloqueoRecursoJpaEntity entity(Integer id) {
        BloqueoRecursoJpaEntity entidad = new BloqueoRecursoJpaEntity();
        entidad.setId(id);
        entidad.setIdRecurso(1);
        entidad.setFechaInicio(INICIO);
        entidad.setFechaFin(FIN);
        entidad.setMotivo("Mantenimiento");
        entidad.setCreatedAt(CREADO);
        return entidad;
    }

    @Test
    @DisplayName("Guardar convierte el bloqueo a entidad y devuelve el bloqueo guardado")
    void savesBlock() {
        // Arrange
        when(repositorio.save(any())).thenReturn(entity(8));

        // Act
        BloqueoRecurso guardado = adapter.guardar(BloqueoRecurso.reconstruir(null, 1, INICIO, FIN, "Mantenimiento", CREADO));

        // Assert
        ArgumentCaptor<BloqueoRecursoJpaEntity> captor = ArgumentCaptor.forClass(BloqueoRecursoJpaEntity.class);
        verify(repositorio).save(captor.capture());
        assertNull(captor.getValue().getId());
        assertEquals(1, captor.getValue().getIdRecurso());
        assertEquals(INICIO, captor.getValue().getFechaInicio());
        assertEquals(FIN, captor.getValue().getFechaFin());
        assertEquals("Mantenimiento", captor.getValue().getMotivo());
        assertEquals(CREADO, captor.getValue().getCreatedAt());
        assertEquals(8, guardado.getId());
    }

    @Test
    @DisplayName("Busca un bloqueo por id")
    void findsById() {
        when(repositorio.findById(8)).thenReturn(Optional.of(entity(8)));

        assertEquals("Mantenimiento", adapter.buscarPorId(8).orElseThrow().getMotivo());
    }

    @Test
    @DisplayName("Busca los bloqueos que se solapan con un rango")
    void findsOverlappingBlocks() {
        when(repositorio.buscarQueSeSolapan(1, INICIO, FIN)).thenReturn(List.of(entity(8)));

        assertEquals(List.of(8), adapter.buscarQueSeSolapan(1, INICIO, FIN).stream().map(BloqueoRecurso::getId).toList());
    }

    @Test
    @DisplayName("Busca los bloqueos del recurso paginados")
    void findsBlocksPaged() {
        // Arrange
        when(repositorio.findByIdRecursoOrderByFechaInicioAsc(1, PageRequest.of(0, 20)))
                .thenReturn(new PageImpl<>(List.of(entity(8)), PageRequest.of(0, 20), 1));

        // Act
        ResultadoPaginado<BloqueoRecurso> resultado = adapter.buscarPorRecurso(1, 0, 20);

        // Assert
        assertEquals(1, resultado.getContenido().size());
        assertEquals(1L, resultado.getTotalElementos());
    }

    @Test
    @DisplayName("Eliminar delega en el repositorio")
    void deletesBlock() {
        adapter.eliminar(8);

        verify(repositorio).deleteById(8);
    }
}
