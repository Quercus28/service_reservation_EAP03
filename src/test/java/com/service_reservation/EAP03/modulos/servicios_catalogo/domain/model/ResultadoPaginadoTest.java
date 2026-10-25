package com.service_reservation.EAP03.modulos.servicios_catalogo.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ResultadoPaginadoTest {

    @Test
    @DisplayName("Debe conservar los datos de paginación recibidos")
    void debeConservarDatosDePaginacion() {
        // Act
        ResultadoPaginado<String> resultado = new ResultadoPaginado<>(List.of("a", "b"), 2, 5, 12L, 3);

        // Assert
        assertEquals(List.of("a", "b"), resultado.elementos());
        assertEquals(2, resultado.pagina());
        assertEquals(5, resultado.tamano());
        assertEquals(12L, resultado.totalElementos());
        assertEquals(3, resultado.totalPaginas());
    }

    @Test
    @DisplayName("Debe convertir una lista de elementos nula en una lista vacía")
    void debeConvertirElementosNulosEnListaVacia() {
        // Act
        ResultadoPaginado<String> resultado = new ResultadoPaginado<>(null, 0, 10, 0L, 0);

        // Assert
        assertNotNull(resultado.elementos());
        assertTrue(resultado.elementos().isEmpty());
    }

    @Test
    @DisplayName("Debe hacer una copia defensiva e inmutable de los elementos")
    void debeCopiarElementosDeFormaDefensiva() {
        // Arrange
        List<String> original = new ArrayList<>(List.of("a"));
        ResultadoPaginado<String> resultado = new ResultadoPaginado<>(original, 0, 10, 1L, 1);

        // Act
        original.add("b");

        // Assert
        assertEquals(List.of("a"), resultado.elementos());
        List<String> elementos = resultado.elementos();
        assertThrows(UnsupportedOperationException.class, () -> elementos.add("c"));
    }
}
