package com.service_reservation.EAP03.modulos.recursos.infrastructure.outbound.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "recurso_prestado")
public class RecursoPrestadoJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "id_recurso", nullable = false)
    private Integer idRecurso;

    @Column(name = "id_servicio_prestado", nullable = false)
    private Integer idServicioPrestado;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDateTime fechaFin;

    @Column(name = "precio_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioTotal;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getIdRecurso() { return idRecurso; }
    public void setIdRecurso(Integer idRecurso) { this.idRecurso = idRecurso; }
    public Integer getIdServicioPrestado() { return idServicioPrestado; }
    public void setIdServicioPrestado(Integer idServicioPrestado) { this.idServicioPrestado = idServicioPrestado; }
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDateTime fechaInicio) { this.fechaInicio = fechaInicio; }
    public LocalDateTime getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDateTime fechaFin) { this.fechaFin = fechaFin; }
    public BigDecimal getPrecioTotal() { return precioTotal; }
    public void setPrecioTotal(BigDecimal precioTotal) { this.precioTotal = precioTotal; }
}
