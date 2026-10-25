package com.service_reservation.EAP03.modulos.recursos.infrastructure.outbound.adapter;

import com.service_reservation.EAP03.modulos.recursos.domain.model.Recurso;
import com.service_reservation.EAP03.modulos.recursos.domain.model.ResultadoPaginado;
import com.service_reservation.EAP03.modulos.recursos.infrastructure.outbound.persistence.RecursoMapper;
import com.service_reservation.EAP03.modulos.recursos.infrastructure.outbound.persistence.entity.RecursoJpaEntity;
import com.service_reservation.EAP03.modulos.recursos.infrastructure.outbound.persistence.repository.RecursoSpringDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecursoRepositoryAdapterTest {

    private static final LocalDateTime CREADO = LocalDateTime.of(2026, 9, 1, 8, 0);
    private static final LocalDateTime ELIMINADO = LocalDateTime.of(2026, 9, 2, 8, 0);

    @Mock private RecursoSpringDataRepository repositorio;

    private RecursoRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new RecursoRepositoryAdapter(repositorio, new RecursoMapper());
    }

    private static RecursoJpaEntity entity(Integer id) {
        RecursoJpaEntity entidad = new RecursoJpaEntity();
        entidad.setId(id);
        entidad.setIdProveedor(7);
        entidad.setNombre("Camilla");
        entidad.setPrecioUnitario(new BigDecimal("50000.00"));
        entidad.setStock(3);
        entidad.setCreatedAt(CREADO);
        entidad.setEliminadoEn(ELIMINADO);
        return entidad;
    }

    @Test
    @DisplayName("Guardar convierte el recurso a entidad y devuelve el recurso guardado")
    void savesResource() {
        // Arrange
        Recurso recurso = Recurso.reconstruir(null, 7, "Camilla", new BigDecimal("50000.00"), 3, CREADO, ELIMINADO);
        when(repositorio.save(any())).thenReturn(entity(1));

        // Act
        Recurso guardado = adapter.guardar(recurso);

        // Assert
        ArgumentCaptor<RecursoJpaEntity> captor = ArgumentCaptor.forClass(RecursoJpaEntity.class);
        verify(repositorio).save(captor.capture());
        RecursoJpaEntity enviada = captor.getValue();
        assertNull(enviada.getId());
        assertEquals(7, enviada.getIdProveedor());
        assertEquals("Camilla", enviada.getNombre());
        assertEquals(new BigDecimal("50000.00"), enviada.getPrecioUnitario());
        assertEquals(3, enviada.getStock());
        assertEquals(CREADO, enviada.getCreatedAt());
        assertEquals(ELIMINADO, enviada.getEliminadoEn());
        assertEquals(1, guardado.getId());
        assertEquals(ELIMINADO, guardado.getEliminadoEn());
    }

    @Test
    @DisplayName("Busca por id con y sin bloqueo de fila")
    void findsById() {
        when(repositorio.findById(1)).thenReturn(Optional.of(entity(1)));
        when(repositorio.buscarPorIdConBloqueo(1)).thenReturn(Optional.of(entity(1)));
        when(repositorio.findById(2)).thenReturn(Optional.empty());

        assertEquals("Camilla", adapter.buscarPorId(1).orElseThrow().getNombre());
        assertEquals(1, adapter.buscarPorIdConBloqueo(1).orElseThrow().getId());
        assertTrue(adapter.buscarPorId(2).isEmpty());
    }

    @Test
    @DisplayName("Busca los recursos activos del proveedor paginados")
    void findsActiveResourcesPaged() {
        // Arrange
        when(repositorio.findByIdProveedorAndEliminadoEnIsNull(7, PageRequest.of(1, 2)))
                .thenReturn(new PageImpl<>(List.of(entity(3)), PageRequest.of(1, 2), 5));

        // Act
        ResultadoPaginado<Recurso> resultado = adapter.buscarActivosPorProveedor(7, 1, 2);

        // Assert
        assertEquals(List.of(3), resultado.getContenido().stream().map(Recurso::getId).toList());
        assertEquals(1, resultado.getPagina());
        assertEquals(2, resultado.getTamano());
        assertEquals(5L, resultado.getTotalElementos());
    }

    @Test
    @DisplayName("Delega las consultas de existencia en el repositorio")
    void delegatesExistenceQueries() {
        when(repositorio.existsById(1)).thenReturn(true);
        when(repositorio.existeNombreActivo(7, "Camilla", 1)).thenReturn(false);

        assertTrue(adapter.existePorId(1));
        assertFalse(adapter.existeNombreActivo(7, "Camilla", 1));
    }
}
