package com.service_reservation.EAP03.modulos.servicios_catalogo.domain.model;

import com.service_reservation.EAP03.modulos.servicios_catalogo.domain.exception.ServicioInvalidoException;

import java.time.OffsetDateTime;
import java.util.Objects;

public class Servicio {

    private final Integer id;
    private final Integer idProveedor;
    private String nombre;
    private Integer duracionMinutos;
    private final OffsetDateTime createdAt;

    private Servicio(Integer id, Integer idProveedor, String nombre, Integer duracionMinutos, OffsetDateTime createdAt) {
        validarInvariantes(idProveedor, nombre, duracionMinutos);
        this.id = id;
        this.idProveedor = idProveedor;
        this.nombre = nombre.trim();
        this.duracionMinutos = duracionMinutos;
        this.createdAt = createdAt;
    }

    public static Servicio crearNuevo(Integer idProveedor, String nombre, Integer duracionMinutos) {
        return new Servicio(null, idProveedor, nombre, duracionMinutos, null);
    }

    public static Servicio reconstituir(Integer id, Integer idProveedor, String nombre, Integer duracionMinutos, OffsetDateTime createdAt) {
        if (id == null) {
            throw new ServicioInvalidoException("El id del servicio es obligatorio para reconstituir la entidad.");
        }
        return new Servicio(id, idProveedor, nombre, duracionMinutos, createdAt);
    }

    private void validarInvariantes(Integer idProveedor, String nombre, Integer duracionMinutos) {
        if (idProveedor == null) {
            throw new ServicioInvalidoException("El id del proveedor es obligatorio.");
        }
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new ServicioInvalidoException("El nombre del servicio no puede estar vacío.");
        }
        if (nombre.trim().length() > 150) {
            throw new ServicioInvalidoException("El nombre del servicio no puede exceder los 150 caracteres.");
        }
        if (duracionMinutos == null || duracionMinutos <= 0) {
            throw new ServicioInvalidoException("La duración del servicio debe ser mayor a 0 minutos.");
        }
    }

    public void actualizar(String nuevoNombre, Integer nuevaDuracionMinutos) {
        validarInvariantes(this.idProveedor, nuevoNombre, nuevaDuracionMinutos);
        this.nombre = nuevoNombre.trim();
        this.duracionMinutos = nuevaDuracionMinutos;
    }

    public Integer getId() {
        return id;
    }

    public Integer getIdProveedor() {
        return idProveedor;
    }

    public String getNombre() {
        return nombre;
    }

    public Integer getDuracionMinutos() {
        return duracionMinutos;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Servicio servicio = (Servicio) o;
        return Objects.equals(id, servicio.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Servicio{" +
                "id=" + id +
                ", idProveedor=" + idProveedor +
                ", nombre='" + nombre + '\'' +
                ", duracionMinutos=" + duracionMinutos +
                ", createdAt=" + createdAt +
                '}';
    }
}
