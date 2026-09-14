package com.service_reservation.EAP03.modulos.identidad.infrastructure.outbound.persistence.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "ROLES")
public class RolJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String nombre;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}