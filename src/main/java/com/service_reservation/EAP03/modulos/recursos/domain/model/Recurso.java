package com.service_reservation.EAP03.modulos.recursos.domain.model;

import com.service_reservation.EAP03.modulos.recursos.domain.exception.DatosInvalidosException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class Recurso {

    private final Integer id;
    private final Long idProveedor;
    private String nombre;
    private BigDecimal precioUnitario;
    private int stock;
    private final LocalDateTime createdAt;
    private LocalDateTime eliminadoEn;

    private Recurso(Integer id, Long idProveedor, String nombre,
                     BigDecimal precioUnitario, int stock, LocalDateTime createdAt,
                     LocalDateTime eliminadoEn) {
        validarNombre(nombre);
        validarPrecio(precioUnitario);
        validarStock(stock);
        this.id = id;
        this.idProveedor = Objects.requireNonNull(idProveedor, "idProveedor es obligatorio");
        this.nombre = nombre;
        this.precioUnitario = precioUnitario;
        this.stock = stock;
        this.createdAt = createdAt;
        this.eliminadoEn = eliminadoEn;
    }

    // Para crear un recurso NUEVO (todavía sin id, activo por definición)
    public static Recurso nuevo(Long idProveedor, String nombre, BigDecimal precioUnitario, int stock) {
        return new Recurso(null, idProveedor, nombre, precioUnitario, stock, LocalDateTime.now(), null);
    }

    // Para reconstruir un recurso que YA existe en la base de datos
    public static Recurso reconstruir(Integer id, Long idProveedor, String nombre,
                                       BigDecimal precioUnitario, int stock, LocalDateTime createdAt,
                                       LocalDateTime eliminadoEn) {
        return new Recurso(id, idProveedor, nombre, precioUnitario, stock, createdAt, eliminadoEn);
    }

    public void actualizarDatos(String nombre, BigDecimal precioUnitario, int stock) {
        validarNombre(nombre);
        validarPrecio(precioUnitario);
        validarStock(stock);
        this.nombre = nombre;
        this.precioUnitario = precioUnitario;
        this.stock = stock;
    }

    public int unidadesDisponibles(int comprometido) {
        int libres = this.stock - comprometido;
        return Math.max(libres, 0);
    }

    public boolean estaActivo() {
        return this.eliminadoEn == null;
    }

    public void desactivar() {
        if (!estaActivo()) {
            throw new DatosInvalidosException("El recurso ya está desactivado");
        }
        this.eliminadoEn = LocalDateTime.now();
    }

    private static void validarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new DatosInvalidosException("El nombre del recurso es obligatorio");
        }
    }

    private static void validarPrecio(BigDecimal precioUnitario) {
        if (precioUnitario == null || precioUnitario.signum() < 0) {
            throw new DatosInvalidosException("El precio unitario no puede ser negativo");
        }
    }

    private static void validarStock(int stock) {
        if (stock < 0) {
            throw new DatosInvalidosException("El stock no puede ser negativo");
        }
    }

    public Integer getId() { return id; }
    public Long getIdProveedor() { return idProveedor; }
    public String getNombre() { return nombre; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public int getStock() { return stock; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getEliminadoEn() { return eliminadoEn; }
}
