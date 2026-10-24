package com.service_reservation.EAP03.modulos.servicios_catalogo.infrastructure.adapter.out.persistence;

import com.service_reservation.EAP03.modulos.servicios_catalogo.domain.model.ResultadoPaginado;
import com.service_reservation.EAP03.modulos.servicios_catalogo.domain.model.Servicio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias del adaptador con el repositorio de Spring Data simulado.
 * La prueba contra base de datos está en {@link ServicioPersistenceAdapterTest}.
 */
@ExtendWith(MockitoExtension.class)
class ServicioPersistenceAdapterUnitTest {

    @Mock
    private SpringDataServicioRepository springDataServicioRepository;

    private ServicioPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new ServicioPersistenceAdapter(springDataServicioRepository, new ServicioMapper());
    }

    private static ServicioJpaEntity entidad(Integer id, Integer idProveedor, String nombre) {
        return new ServicioJpaEntity(id, idProveedor, 30, nombre, OffsetDateTime.now());
    }

    @Test
    @DisplayName("El constructor debe rechazar dependencias nulas")
    void constructorRechazaDependenciasNulas() {
        ServicioMapper mapper = new ServicioMapper();
        assertThrows(NullPointerException.class, () -> new ServicioPersistenceAdapter(null, mapper));
        assertThrows(NullPointerException.class, () -> new ServicioPersistenceAdapter(springDataServicioRepository, null));
    }

    @Test
    @DisplayName("Guardar debe persistir la entidad mapeada y devolver el dominio guardado")
    void guardarPersisteYDevuelveDominio() {
        // Arrange
        when(springDataServicioRepository.save(any(ServicioJpaEntity.class))).thenReturn(entidad(9, 3, "Masaje"));

        // Act
        Servicio guardado = adapter.guardar(Servicio.crearNuevo(3, "Masaje", 30));

        // Assert
        assertEquals(9, guardado.getId());
        ArgumentCaptor<ServicioJpaEntity> captor = ArgumentCaptor.forClass(ServicioJpaEntity.class);
        verify(springDataServicioRepository).save(captor.capture());
        assertNull(captor.getValue().getId());
        assertEquals("Masaje", captor.getValue().getNombre());
    }

    @Test
    @DisplayName("Guardar debe rechazar un servicio nulo")
    void guardarRechazaNulo() {
        assertThrows(NullPointerException.class, () -> adapter.guardar(null));
        verifyNoInteractions(springDataServicioRepository);
    }

    @Test
    @DisplayName("Buscar por id debe devolver el servicio cuando existe")
    void buscarPorIdExistente() {
        // Arrange
        when(springDataServicioRepository.findById(9)).thenReturn(Optional.of(entidad(9, 3, "Masaje")));

        // Act
        Optional<Servicio> resultado = adapter.buscarPorId(9);

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals("Masaje", resultado.get().getNombre());
    }

    @Test
    @DisplayName("Buscar por id debe devolver vacío cuando no existe")
    void buscarPorIdInexistente() {
        when(springDataServicioRepository.findById(9)).thenReturn(Optional.empty());

        assertTrue(adapter.buscarPorId(9).isEmpty());
    }

    @Test
    @DisplayName("Buscar por id nulo debe devolver vacío sin consultar la base de datos")
    void buscarPorIdNulo() {
        assertTrue(adapter.buscarPorId(null).isEmpty());
        verifyNoInteractions(springDataServicioRepository);
    }

    @Test
    @DisplayName("Buscar por proveedor debe mapear todas las entidades encontradas")
    void buscarPorProveedor() {
        // Arrange
        when(springDataServicioRepository.findByIdProveedor(3))
                .thenReturn(List.of(entidad(1, 3, "Corte"), entidad(2, 3, "Tinte")));

        // Act
        List<Servicio> servicios = adapter.buscarPorProveedor(3);

        // Assert
        assertEquals(List.of("Corte", "Tinte"), servicios.stream().map(Servicio::getNombre).toList());
    }

    @Test
    @DisplayName("Buscar por proveedor nulo debe devolver lista vacía sin consultar la base de datos")
    void buscarPorProveedorNulo() {
        assertTrue(adapter.buscarPorProveedor(null).isEmpty());
        verifyNoInteractions(springDataServicioRepository);
    }

    @Test
    @DisplayName("Buscar paginado debe ordenar por fecha de creación descendente y mapear la página")
    void buscarPaginadoMapeaPagina() {
        // Arrange
        Pageable pageable = PageRequest.of(1, 2, Sort.by(Sort.Direction.DESC, "createdAt"));
        when(springDataServicioRepository.findAll(ArgumentMatchers.<Specification<ServicioJpaEntity>>any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entidad(3, 1, "Corte"), entidad(4, 1, "Tinte")), pageable, 5));

        // Act
        ResultadoPaginado<Servicio> resultado = adapter.buscarPaginado(1, "  cor  ", 1, 2);

        // Assert
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(springDataServicioRepository).findAll(ArgumentMatchers.<Specification<ServicioJpaEntity>>any(), pageableCaptor.capture());
        assertEquals(1, pageableCaptor.getValue().getPageNumber());
        assertEquals(2, pageableCaptor.getValue().getPageSize());
        assertEquals(Sort.Direction.DESC, pageableCaptor.getValue().getSort().getOrderFor("createdAt").getDirection());

        assertEquals(List.of(3, 4), resultado.elementos().stream().map(Servicio::getId).toList());
        assertEquals(1, resultado.pagina());
        assertEquals(2, resultado.tamano());
        assertEquals(5L, resultado.totalElementos());
        assertEquals(3, resultado.totalPaginas());
    }

    @ParameterizedTest(name = "nombre = [{0}]")
    @NullSource
    @ValueSource(strings = {"", "   "})
    @DisplayName("Buscar paginado debe tolerar un filtro de nombre nulo o vacío")
    void buscarPaginadoSinFiltroDeNombre(String nombre) {
        // Arrange
        when(springDataServicioRepository.findAll(ArgumentMatchers.<Specification<ServicioJpaEntity>>any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 10), 0));

        // Act
        ResultadoPaginado<Servicio> resultado = adapter.buscarPaginado(null, nombre, 0, 10);

        // Assert
        assertTrue(resultado.elementos().isEmpty());
        assertEquals(0L, resultado.totalElementos());
    }

    @Test
    @DisplayName("Existe por id debe delegar en el repositorio")
    void existePorIdDelega() {
        when(springDataServicioRepository.existsById(9)).thenReturn(true);
        when(springDataServicioRepository.existsById(10)).thenReturn(false);

        assertTrue(adapter.existePorId(9));
        assertFalse(adapter.existePorId(10));
    }

    @Test
    @DisplayName("Existe por id nulo debe devolver false sin consultar la base de datos")
    void existePorIdNulo() {
        assertFalse(adapter.existePorId(null));
        verifyNoInteractions(springDataServicioRepository);
    }

    @Test
    @DisplayName("Eliminar por id debe delegar en el repositorio")
    void eliminarPorIdDelega() {
        adapter.eliminarPorId(9);

        verify(springDataServicioRepository).deleteById(9);
    }

    @Test
    @DisplayName("Eliminar por id nulo no debe hacer nada")
    void eliminarPorIdNulo() {
        adapter.eliminarPorId(null);

        verifyNoInteractions(springDataServicioRepository);
    }
}
