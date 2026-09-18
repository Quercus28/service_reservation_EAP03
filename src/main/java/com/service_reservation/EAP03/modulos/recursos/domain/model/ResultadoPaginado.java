package com.service_reservation.EAP03.modulos.recursos.domain.model;

import java.util.List;

public class ResultadoPaginado<T> {

    private final List<T> contenido;
    private final int pagina;
    private final int tamano;
    private final long totalElementos;

    public ResultadoPaginado(List<T> contenido, int pagina, int tamano, long totalElementos) {
        this.contenido = contenido;
        this.pagina = pagina;
        this.tamano = tamano;
        this.totalElementos = totalElementos;
    }

    public List<T> getContenido() { return contenido; }
    public int getPagina() { return pagina; }
    public int getTamano() { return tamano; }
    public long getTotalElementos() { return totalElementos; }

    public int getTotalPaginas() {
        if (tamano <= 0) return 0;
        return (int) Math.ceil((double) totalElementos / tamano);
    }
}
