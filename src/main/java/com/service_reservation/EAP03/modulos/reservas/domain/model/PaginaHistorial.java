package com.service_reservation.EAP03.modulos.reservas.domain.model;

import java.util.List;

public record PaginaHistorial(List<ReservaHistorial> contenido, int page, int size, long total) {

    public int totalPaginas() {
        return size <= 0 ? 0 : (int) Math.ceil((double) total / size);
    }
}