package com.service_reservation.EAP03.modulos.servicios_catalogo.application.dto;

import java.util.List;

public record PaginaResponse<T>(
        List<T> contenido,
        int pagina,
        int tamano,
        long totalElementos,
        int totalPaginas,
        boolean esPrimera,
        boolean esUltima
) {
    public static <T> PaginaResponse<T> de(
            List<T> contenido,
            int pagina,
            int tamano,
            long totalElementos,
            int totalPaginas
    ) {
        List<T> elementosSeguros = (contenido != null) ? List.copyOf(contenido) : List.of();
        boolean esPrimera = pagina == 0;
        boolean esUltima = totalPaginas == 0 || pagina >= totalPaginas - 1;
        return new PaginaResponse<>(elementosSeguros, pagina, tamano, totalElementos, totalPaginas, esPrimera, esUltima);
    }
}
