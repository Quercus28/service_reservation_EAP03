package com.service_reservation.EAP03.modulos.recursos.infrastructure.outbound.adapter;

import com.service_reservation.EAP03.modulos.recursos.domain.exception.RecursoYaAsociadoException;
import com.service_reservation.EAP03.modulos.recursos.domain.model.ServicioRecurso;
import com.service_reservation.EAP03.modulos.recursos.infrastructure.outbound.persistence.ServicioRecursoMapper;
import com.service_reservation.EAP03.modulos.recursos.infrastructure.outbound.persistence.entity.ServicioRecursoJpaEntity;
import com.service_reservation.EAP03.modulos.recursos.infrastructure.outbound.persistence.repository.ServicioRecursoSpringDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicioRecursoRepositoryAdapterTest {

    @Mock private ServicioRecursoSpringDataRepository repositorio;

    private ServicioRecursoRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new ServicioRecursoRepositoryAdapter(repositorio, new ServicioRecursoMapper());
    }

    private static ServicioRecursoJpaEntity entity(Integer id) {
        ServicioRecursoJpaEntity entidad = new ServicioRecursoJpaEntity();
        entidad.setId(id);
        entidad.setIdServicio(10);
        entidad.setIdRecurso(3);
        entidad.setCantidad(2);
        return entidad;
    }

    @Test
    @DisplayName("Guardar convierte la asociación a entidad y devuelve la asociación guardada")
    void savesLink() {
        // Arrange
        when(repositorio.saveAndFlush(any())).thenReturn(entity(4));

        // Act
        ServicioRecurso guardada = adapter.guardar(ServicioRecurso.nuevo(10, 3, 2));

        // Assert
        ArgumentCaptor<ServicioRecursoJpaEntity> captor = ArgumentCaptor.forClass(ServicioRecursoJpaEntity.class);
        verify(repositorio).saveAndFlush(captor.capture());
        assertNull(captor.getValue().getId());
        assertEquals(10, captor.getValue().getIdServicio());
        assertEquals(3, captor.getValue().getIdRecurso());
        assertEquals(2, captor.getValue().getCantidad());
        assertEquals(4, guardada.getId());
    }

    @Test
    @DisplayName("Si la base de datos rechaza la asociación duplicada responde que ya estaba asociado")
    void duplicatedLinkIsAlreadyLinked() {
        // Arrange
        when(repositorio.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("duplicado"));
        ServicioRecurso asociacion = ServicioRecurso.nuevo(10, 3, 2);

        // Act
        RecursoYaAsociadoException ex = assertThrows(RecursoYaAsociadoException.class, () -> adapter.guardar(asociacion));

        // Assert
        assertEquals(new RecursoYaAsociadoException(3, 10).getMessage(), ex.getMessage());
    }

    @Test
    @DisplayName("Busca la asociación de un servicio y un recurso")
    void findsLink() {
        when(repositorio.findByIdServicioAndIdRecurso(10, 3)).thenReturn(Optional.of(entity(4)));

        assertEquals(4, adapter.buscarPorServicioYRecurso(10, 3).orElseThrow().getId());
    }

    @Test
    @DisplayName("Delega la consulta de existencia en el repositorio")
    void delegatesExistence() {
        when(repositorio.existsByIdServicioAndIdRecurso(10, 3)).thenReturn(true);

        assertTrue(adapter.existePorServicioYRecurso(10, 3));
    }

    @Test
    @DisplayName("Busca las asociaciones de un servicio")
    void findsLinksOfService() {
        when(repositorio.findByIdServicioOrderByIdAsc(10)).thenReturn(List.of(entity(4), entity(5)));

        assertEquals(List.of(4, 5), adapter.buscarPorServicio(10).stream().map(ServicioRecurso::getId).toList());
    }

    @Test
    @DisplayName("Eliminar delega en el repositorio")
    void deletesLink() {
        adapter.eliminar(4);

        verify(repositorio).deleteById(4);
    }
}
