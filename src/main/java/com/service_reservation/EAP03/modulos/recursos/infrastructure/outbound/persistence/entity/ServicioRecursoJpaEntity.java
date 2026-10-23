package com.service_reservation.EAP03.modulos.recursos.infrastructure.outbound.persistence.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "servicio_recurso")
public class ServicioRecursoJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "id_servicio", nullable = false)
    private Integer idServicio;

    @Column(name = "id_recurso", nullable = false)
    private Integer idRecurso;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getIdServicio() { return idServicio; }
    public void setIdServicio(Integer idServicio) { this.idServicio = idServicio; }
    public Integer getIdRecurso() { return idRecurso; }
    public void setIdRecurso(Integer idRecurso) { this.idRecurso = idRecurso; }
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
}
