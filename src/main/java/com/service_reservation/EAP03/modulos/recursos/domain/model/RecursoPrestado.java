package com.service_reservation.EAP03.modulos.recursos.domain.model;

import com.service_reservation.EAP03.modulos.recursos.domain.exception.DatosInvalidosException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class RecursoPrestado {

    private final Integer id;
    private final Integer idRecurso;
    private final Integer idServicioPrestado;
    private final int cantidad;
    private final LocalDateTime fechaInicio;
    private final LocalDateTime fechaFin;
    private final BigDecimal precioTotal;
    private LocalDateTime liberadoEn;

    private RecursoPrestado(Integer id, Integer idRecurso, Integer idServicioPrestado,
                             int cantidad, PeriodoPrestamo periodo,
                             BigDecimal precioTotal, LocalDateTime liberadoEn) {
        validarCantidad(cantidad);
        validarPeriodo(periodo);
        validarPrecio(precioTotal);
        this.id = id;
        this.idRecurso = Objects.requireNonNull(idRecurso, "idRecurso es obligatorio");
        this.idServicioPrestado = Objects.requireNonNull(idServicioPrestado, "idServicioPrestado es obligatorio");
        this.cantidad = cantidad;
        this.fechaInicio = periodo.inicio();
        this.fechaFin = periodo.fin();
        this.precioTotal = precioTotal;
        this.liberadoEn = liberadoEn;
    }

    // Para crear un préstamo NUEVO (todavía sin id y activo)
    public static RecursoPrestado nuevo(Integer idRecurso, Integer idServicioPrestado, int cantidad,
                                         LocalDateTime fechaInicio, LocalDateTime fechaFin, BigDecimal precioTotal) {
        return new RecursoPrestado(null, idRecurso, idServicioPrestado, cantidad,
                new PeriodoPrestamo(fechaInicio, fechaFin), precioTotal, null);
    }

    // Para reconstruir un préstamo que YA existe en la base de datos
    public static RecursoPrestado reconstruir(Integer id, Integer idRecurso, Integer idServicioPrestado, int cantidad,
                                               PeriodoPrestamo periodo, BigDecimal precioTotal,
                                               LocalDateTime liberadoEn) {
        return new RecursoPrestado(id, idRecurso, idServicioPrestado, cantidad, periodo, precioTotal, liberadoEn);
    }

    // Devuelve las unidades: el préstamo deja de contar como ocupado, pero no se borra.
    public void liberar(LocalDateTime ahora) {
        if (estaLiberado()) {
            return;
        }
        this.liberadoEn = Objects.requireNonNull(ahora, "ahora es obligatorio");
    }

    public boolean estaLiberado() {
        return this.liberadoEn != null;
    }

    // ¿Este préstamo se solapa con el rango [inicio, fin) propuesto?
    // Dos rangos que solo se tocan en el borde no cuentan como solapamiento.
    public boolean seSolapaCon(LocalDateTime inicio, LocalDateTime fin) {
        return this.fechaInicio.isBefore(fin) && inicio.isBefore(this.fechaFin);
    }

    private static void validarCantidad(int cantidad) {
        if (cantidad <= 0) {
            throw new DatosInvalidosException("La cantidad debe ser mayor que cero");
        }
    }

    private static void validarPeriodo(PeriodoPrestamo periodo) {
        if (periodo == null || periodo.inicio() == null || periodo.fin() == null) {
            throw new DatosInvalidosException("fechaInicio y fechaFin son obligatorias");
        }
        if (!periodo.fin().isAfter(periodo.inicio())) {
            throw new DatosInvalidosException("fechaFin debe ser posterior a fechaInicio");
        }
    }

    private static void validarPrecio(BigDecimal precioTotal) {
        if (precioTotal == null || precioTotal.signum() < 0) {
            throw new DatosInvalidosException("El precio total no puede ser negativo");
        }
    }

    public Integer getId() { return id; }
    public Integer getIdRecurso() { return idRecurso; }
    public Integer getIdServicioPrestado() { return idServicioPrestado; }
    public int getCantidad() { return cantidad; }
    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public LocalDateTime getFechaFin() { return fechaFin; }
    public BigDecimal getPrecioTotal() { return precioTotal; }
    public LocalDateTime getLiberadoEn() { return liberadoEn; }
}
