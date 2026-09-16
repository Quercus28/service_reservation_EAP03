package com.service_reservation.EAP03.modulos.servicios_catalogo.domain.model;

import java.util.List;

public record ResultadoPaginado<T>(
        List<T> elementos,
        int pagina,
        int tamano,
        long totalElementos,
        int totalPaginas
) {
    public ResultadoPaginado {
        elementos = (elementos != null) ? List.copyOf(elementos) : List.of();
    }
}
