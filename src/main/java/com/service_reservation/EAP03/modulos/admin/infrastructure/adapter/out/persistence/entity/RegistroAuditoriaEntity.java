package com.service_reservation.EAP03.modulos.admin.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "admin_auditoria")
public class RegistroAuditoriaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    @Column(name = "id_actor")
    private Long idActor;

    @Column(name = "id_usuario_afectado")
    private Long idUsuarioAfectado;

    @Column(name = "accion", nullable = false)
    private String accion;

    @Column(name = "rol")
    private String rol;

    @Column(name = "resultado", nullable = false)
    private String resultado;

    @Column(name = "endpoint")
    private String endpoint;

    @Column(name = "detalle", length = 1000)
    private String detalle;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public Long getIdActor() { return idActor; }
    public void setIdActor(Long idActor) { this.idActor = idActor; }

    public Long getIdUsuarioAfectado() { return idUsuarioAfectado; }
    public void setIdUsuarioAfectado(Long idUsuarioAfectado) { this.idUsuarioAfectado = idUsuarioAfectado; }

    public String getAccion() { return accion; }
    public void setAccion(String accion) { this.accion = accion; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public String getResultado() { return resultado; }
    public void setResultado(String resultado) { this.resultado = resultado; }

    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }

    public String getDetalle() { return detalle; }
    public void setDetalle(String detalle) { this.detalle = detalle; }
}
