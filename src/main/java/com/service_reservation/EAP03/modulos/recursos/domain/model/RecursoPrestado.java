package com.service_reservation.EAP03.modulos.recursos.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class RecursoPrestado {

    private final Integer id;
    private final Integer idRecurso;
    private final Integer idServicioPrestado;
    private int cantidad;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private BigDecimal precioTotal;

    private RecursoPrestado(Integer id, Integer idRecurso, Integer idServicioPrestado,
                             int cantidad, LocalDateTime fechaInicio, LocalDateTime fechaFin,
                             BigDecimal precioTotal) {
        validarCantidad(cantidad);
        validarFechas(fechaInicio, fechaFin);
        validarPrecio(precioTotal);
        this.id = id;
        this.idRecurso = Objects.requireNonNull(idRecurso, "idRecurso es obligatorio");
        this.idServicioPrestado = Objects.requireNonNull(idServicioPrestado, "idServicioPrestado es obligatorio");
        this.cantidad = cantidad;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.precioTotal = precioTotal;
    }

    // Para crear un préstamo NUEVO (todavía sin id)
    public static RecursoPrestado nuevo(Integer idRecurso, Integer idServicioPrestado, int cantidad,
                                         LocalDateTime fechaInicio, LocalDateTime fechaFin, BigDecimal precioTotal) {
        return new RecursoPrestado(null, idRecurso, idServicioPrestado, cantidad, fechaInicio, fechaFin, precioTotal);
    }

    // Para reconstruir un préstamo que YA existe en la base de datos
    public static RecursoPrestado reconstruir(Integer id, Integer idRecurso, Integer idServicioPrestado, int cantidad,
                                               LocalDateTime fechaInicio, LocalDateTime fechaFin, BigDecimal precioTotal) {
        return new RecursoPrestado(id, idRecurso, idServicioPrestado, cantidad, fechaInicio, fechaFin, precioTotal);
    }

    // ¿Este préstamo se solapa con el rango [inicio, fin) propuesto?
    // Dos rangos que solo se tocan en el borde no cuentan como solapamiento.
    public boolean seSolapaCon(LocalDateTime inicio, LocalDateTime fin) {
        return this.fechaInicio.isBefore(fin) && inicio.isBefore(this.fechaFin);
    }

    private static void validarCantidad(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
        }
    }

    private static void validarFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        if (fechaInicio == null || fechaFin == null) {
            throw new IllegalArgumentException("fechaInicio y fechaFin son obligatorias");
        }
        if (!fechaFin.isAfter(fechaInicio)) {
            throw new IllegalArgumentException("fechaFin debe ser posterior a fechaInicio");
        }
    }

    private static void validarPrecio(BigDecimal precioTotal) {
        if (precioTotal == null || precioTotal.signum() < 0) {
            throw new IllegalArgumentException("El precio total no puede ser negativo");
        }
    }

    public Integer getId() { return id; }
    public Integer getIdRecurso() { return idRecurso; }
    public Integer getIdServicioPrestado() { return idServicioPrestado; }
    public int getCantidad() { return cantidad; }
    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public LocalDateTime getFechaFin() { return fechaFin; }
    public BigDecimal getPrecioTotal() { return precioTotal; }
}
