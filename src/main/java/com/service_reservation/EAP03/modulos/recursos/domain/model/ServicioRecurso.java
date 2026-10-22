package com.service_reservation.EAP03.modulos.recursos.domain.model;

import com.service_reservation.EAP03.modulos.recursos.domain.exception.DatosInvalidosException;

import java.util.Objects;

public class ServicioRecurso {

    private final Integer id;
    private final Integer idServicio;
    private final Integer idRecurso;
    private final int cantidad;
    private final boolean obligatorio;

    private ServicioRecurso(Integer id, Integer idServicio, Integer idRecurso,
                            int cantidad, boolean obligatorio) {
        validarCantidad(cantidad);
        this.id = id;
        this.idServicio = Objects.requireNonNull(idServicio, "idServicio es obligatorio");
        this.idRecurso = Objects.requireNonNull(idRecurso, "idRecurso es obligatorio");
        this.cantidad = cantidad;
        this.obligatorio = obligatorio;
    }

    // Para crear una asociación NUEVA (todavía sin id)
    public static ServicioRecurso nuevo(Integer idServicio, Integer idRecurso,
                                        int cantidad, boolean obligatorio) {
        return new ServicioRecurso(null, idServicio, idRecurso, cantidad, obligatorio);
    }

    // Para reconstruir una asociación que YA existe en la base de datos
    public static ServicioRecurso reconstruir(Integer id, Integer idServicio, Integer idRecurso,
                                              int cantidad, boolean obligatorio) {
        return new ServicioRecurso(id, idServicio, idRecurso, cantidad, obligatorio);
    }

    private static void validarCantidad(int cantidad) {
        if (cantidad <= 0) {
            throw new DatosInvalidosException("La cantidad debe ser mayor que cero");
        }
    }

    public Integer getId() { return id; }
    public Integer getIdServicio() { return idServicio; }
    public Integer getIdRecurso() { return idRecurso; }
    public int getCantidad() { return cantidad; }
    public boolean isObligatorio() { return obligatorio; }
}
