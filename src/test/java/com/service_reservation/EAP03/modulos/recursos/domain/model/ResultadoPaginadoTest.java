package com.service_reservation.EAP03.modulos.recursos.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ResultadoPaginadoTest {

    @Test
    @DisplayName("Conserva el contenido y los datos de paginación")
    void keepsPagingData() {
        ResultadoPaginado<String> resultado = new ResultadoPaginado<>(List.of("a", "b"), 1, 2, 5L);

        assertEquals(List.of("a", "b"), resultado.getContenido());
        assertEquals(1, resultado.getPagina());
        assertEquals(2, resultado.getTamano());
        assertEquals(5L, resultado.getTotalElementos());
    }

    @Test
    @DisplayName("Calcula el total de páginas redondeando hacia arriba")
    void roundsTotalPagesUp() {
        assertEquals(3, new ResultadoPaginado<>(List.of(), 0, 2, 5L).getTotalPaginas());
        assertEquals(2, new ResultadoPaginado<>(List.of(), 0, 2, 4L).getTotalPaginas());
        assertEquals(0, new ResultadoPaginado<>(List.of(), 0, 2, 0L).getTotalPaginas());
    }

    @Test
    @DisplayName("Con tamaño de página cero o negativo no hay páginas")
    void noPagesWithoutSize() {
        assertEquals(0, new ResultadoPaginado<>(List.of(), 0, 0, 5L).getTotalPaginas());
        assertEquals(0, new ResultadoPaginado<>(List.of(), 0, -1, 5L).getTotalPaginas());
    }
}
