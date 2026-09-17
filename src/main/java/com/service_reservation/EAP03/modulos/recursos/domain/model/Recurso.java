package com.service_reservation.EAP03.modulos.recursos.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class Recurso {

    private final Integer id;
    private final Integer idProveedor;
    private String nombre;
    private BigDecimal precioUnitario;
    private int stock;
    private final LocalDateTime createdAt;

    private Recurso(Integer id, Integer idProveedor, String nombre,
                     BigDecimal precioUnitario, int stock, LocalDateTime createdAt) {
        validarNombre(nombre);
        validarPrecio(precioUnitario);
        validarStock(stock);
        this.id = id;
        this.idProveedor = Objects.requireNonNull(idProveedor, "idProveedor es obligatorio");
        this.nombre = nombre;
        this.precioUnitario = precioUnitario;
        this.stock = stock;
        this.createdAt = createdAt;
    }

    // Para crear un recurso NUEVO (todavía sin id, lo asigna la base de datos)
    public static Recurso nuevo(Integer idProveedor, String nombre, BigDecimal precioUnitario, int stock) {
        return new Recurso(null, idProveedor, nombre, precioUnitario, stock, LocalDateTime.now());
    }

    // Para reconstruir un recurso que YA existe en la base de datos
    public static Recurso reconstruir(Integer id, Integer idProveedor, String nombre,
                                       BigDecimal precioUnitario, int stock, LocalDateTime createdAt) {
        return new Recurso(id, idProveedor, nombre, precioUnitario, stock, createdAt);
    }

    public void actualizarDatos(String nombre, BigDecimal precioUnitario, int stock) {
        validarNombre(nombre);
        validarPrecio(precioUnitario);
        validarStock(stock);
        this.nombre = nombre;
        this.precioUnitario = precioUnitario;
        this.stock = stock;
    }

    public boolean puedeCubrir(int cantidadSolicitada) {
        return cantidadSolicitada > 0 && cantidadSolicitada <= this.stock;
    }

    private static void validarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del recurso es obligatorio");
        }
    }

    private static void validarPrecio(BigDecimal precioUnitario) {
        if (precioUnitario == null || precioUnitario.signum() < 0) {
            throw new IllegalArgumentException("El precio unitario no puede ser negativo");
        }
    }

    private static void validarStock(int stock) {
        if (stock < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }
    }

    public Integer getId() { return id; }
    public Integer getIdProveedor() { return idProveedor; }
    public String getNombre() { return nombre; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public int getStock() { return stock; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
