package com.service_reservation.EAP03.modulos.servicios_catalogo.infrastructure.adapter.out.persistence;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicioSpecificationTest {

    @Mock
    private Root<ServicioJpaEntity> root;

    @Mock
    private CriteriaQuery<?> query;

    @Mock
    private CriteriaBuilder cb;

    @Mock
    private Path<Object> pathProveedor;

    @Mock
    private Path<Object> pathNombre;

    @Mock
    private Expression<String> nombreEnMinusculas;

    @Mock
    private Predicate predicadoProveedor;

    @Mock
    private Predicate predicadoNombre;

    @Mock
    private Predicate predicadoFinal;

    @Test
    @DisplayName("Sin filtros debe combinar una lista vacía de predicados")
    void sinFiltros() {
        // Arrange
        when(cb.and(any(Predicate[].class))).thenReturn(predicadoFinal);

        // Act
        Predicate resultado = ServicioSpecification.conFiltros(null, null).toPredicate(root, query, cb);

        // Assert
        assertSame(predicadoFinal, resultado);
        verify(cb).and();
        verifyNoInteractions(root);
    }

    @Test
    @DisplayName("Con proveedor debe filtrar por igualdad de idProveedor")
    void conFiltroProveedor() {
        // Arrange
        doReturn(pathProveedor).when(root).get("idProveedor");
        when(cb.equal(pathProveedor, 5)).thenReturn(predicadoProveedor);
        when(cb.and(any(Predicate[].class))).thenReturn(predicadoFinal);

        // Act
        ServicioSpecification.conFiltros(5, null).toPredicate(root, query, cb);

        // Assert
        verify(cb).and(predicadoProveedor);
    }

    @Test
    @DisplayName("Con nombre debe filtrar con LIKE sin distinguir mayúsculas y recortando espacios")
    void conFiltroNombre() {
        // Arrange
        doReturn(pathNombre).when(root).get("nombre");
        doReturn(nombreEnMinusculas).when(cb).lower(any());
        when(cb.like(nombreEnMinusculas, "%corte%")).thenReturn(predicadoNombre);
        when(cb.and(any(Predicate[].class))).thenReturn(predicadoFinal);

        // Act
        ServicioSpecification.conFiltros(null, "  CoRtE ").toPredicate(root, query, cb);

        // Assert
        verify(cb).lower(any());
        verify(cb).and(predicadoNombre);
    }

    @Test
    @DisplayName("Con proveedor y nombre debe combinar ambos predicados")
    void conAmbosFiltros() {
        // Arrange
        doReturn(pathProveedor).when(root).get("idProveedor");
        doReturn(pathNombre).when(root).get("nombre");
        when(cb.equal(pathProveedor, 5)).thenReturn(predicadoProveedor);
        doReturn(nombreEnMinusculas).when(cb).lower(any());
        when(cb.like(nombreEnMinusculas, "%corte%")).thenReturn(predicadoNombre);
        when(cb.and(any(Predicate[].class))).thenReturn(predicadoFinal);

        // Act
        ServicioSpecification.conFiltros(5, "corte").toPredicate(root, query, cb);

        // Assert
        verify(cb).and(new Predicate[]{predicadoProveedor, predicadoNombre});
    }

    @ParameterizedTest(name = "nombre = [{0}]")
    @ValueSource(strings = {"", "   "})
    @DisplayName("Un nombre en blanco no debe agregar el filtro por nombre")
    void nombreEnBlancoSeIgnora(String nombre) {
        // Arrange
        when(cb.and(any(Predicate[].class))).thenReturn(predicadoFinal);

        // Act
        ServicioSpecification.conFiltros(null, nombre).toPredicate(root, query, cb);

        // Assert
        verify(cb).and();
        verify(cb, never()).like(any(), anyString());
    }
}
