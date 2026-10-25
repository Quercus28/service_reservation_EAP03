package com.service_reservation.EAP03.modulos.servicios_catalogo.application.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PaginaResponseTest {

    @ParameterizedTest(name = "pagina={0}, totalPaginas={1} -> primera={2}, ultima={3}")
    @CsvSource({
            "0, 0, true, true",
            "0, 1, true, true",
            "0, 3, true, false",
            "1, 3, false, false",
            "2, 3, false, true",
            "5, 3, false, true"
    })
    @DisplayName("Debe calcular si la página es la primera y/o la última")
    void debeCalcularPrimeraYUltima(int pagina, int totalPaginas, boolean esPrimera, boolean esUltima) {
        // Act
        PaginaResponse<String> response = PaginaResponse.de(List.of(), pagina, 10, 0L, totalPaginas);

        // Assert
        assertEquals(esPrimera, response.esPrimera());
        assertEquals(esUltima, response.esUltima());
    }

    @Test
    @DisplayName("Debe conservar los datos de paginación recibidos")
    void debeConservarDatos() {
        // Act
        PaginaResponse<String> response = PaginaResponse.de(List.of("a", "b"), 1, 2, 6L, 3);

        // Assert
        assertEquals(List.of("a", "b"), response.contenido());
        assertEquals(1, response.pagina());
        assertEquals(2, response.tamano());
        assertEquals(6L, response.totalElementos());
        assertEquals(3, response.totalPaginas());
    }

    @Test
    @DisplayName("Debe convertir un contenido nulo en una lista vacía")
    void debeConvertirContenidoNuloEnListaVacia() {
        // Act
        PaginaResponse<String> response = PaginaResponse.de(null, 0, 10, 0L, 0);

        // Assert
        assertNotNull(response.contenido());
        assertTrue(response.contenido().isEmpty());
    }

    @Test
    @DisplayName("Debe hacer una copia defensiva e inmutable del contenido")
    void debeCopiarContenidoDeFormaDefensiva() {
        // Arrange
        List<String> original = new ArrayList<>(List.of("a"));
        PaginaResponse<String> response = PaginaResponse.de(original, 0, 10, 1L, 1);

        // Act
        original.add("b");

        // Assert
        assertEquals(List.of("a"), response.contenido());
        List<String> contenido = response.contenido();
        assertThrows(UnsupportedOperationException.class, () -> contenido.add("c"));
    }
}
