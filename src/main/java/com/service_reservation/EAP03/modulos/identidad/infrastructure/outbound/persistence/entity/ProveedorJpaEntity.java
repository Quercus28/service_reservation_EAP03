package com.service_reservation.EAP03.modulos.identidad.infrastructure.outbound.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "PROVEEDOR")
public class ProveedorJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_usuario", nullable = false)
    private Long idUsuario;

    @Column(name = "razon_social")
    private String razonSocial;

    @Column(name = "nit_rut")
    private String nitRut;

    @Column(name = "telefono")
    private String telefono;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }
    public String getRazonSocial() { return razonSocial; }
    public void setRazonSocial(String razonSocial) { this.razonSocial = razonSocial; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getNitRut() { return nitRut; }
    public void setNitRut(String nitRut) { this.nitRut = nitRut; }
}