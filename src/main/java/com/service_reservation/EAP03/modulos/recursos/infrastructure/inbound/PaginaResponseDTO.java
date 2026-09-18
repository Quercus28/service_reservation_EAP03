package com.service_reservation.EAP03.modulos.recursos.infrastructure.inbound;

import java.util.List;

public record PaginaResponseDTO<T>(
        List<T> contenido,
        int pagina,
        int tamano,
        long totalElementos,
        int totalPaginas
) {}
