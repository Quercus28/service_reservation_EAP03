package com.service_reservation.EAP03.modulos.reservas.infrastructure.outbound.persistence.entity;

import com.service_reservation.EAP03.modulos.reservas.domain.model.EstadoReserva;
import jakarta.persistence.*;
import java.time.LocalDateTime;

// Representa la tabla servicio_prestado tal como la crea la migración
// V20261014112723__crear_esquema_reservas.sql. Si no coincide exactamente, la aplicación
// no arranca, porque al iniciar se compara contra la base.
@Entity
@Table(name = "servicio_prestado")
public class ReservaJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "id_cliente", nullable = false)
    private Long idCliente;

    @Column(name = "id_servicio", nullable = false)
    private Integer idServicio;

    @Column(name = "id_proveedor", nullable = false)
    private Integer idProveedor;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDateTime fechaFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoReserva estado;

    @Column(length = 500)
    private String notas;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "cancelada_en")
    private LocalDateTime canceladaEn;

    @Column(name = "motivo_cancelacion", length = 255)
    private String motivoCancelacion;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Long getIdCliente() { return idCliente; }
    public void setIdCliente(Long idCliente) { this.idCliente = idCliente; }
    public Integer getIdServicio() { return idServicio; }
    public void setIdServicio(Integer idServicio) { this.idServicio = idServicio; }
    public Integer getIdProveedor() { return idProveedor; }
    public void setIdProveedor(Integer idProveedor) { this.idProveedor = idProveedor; }
    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDateTime fechaInicio) { this.fechaInicio = fechaInicio; }
    public LocalDateTime getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDateTime fechaFin) { this.fechaFin = fechaFin; }
    public EstadoReserva getEstado() { return estado; }
    public void setEstado(EstadoReserva estado) { this.estado = estado; }
    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getCanceladaEn() { return canceladaEn; }
    public void setCanceladaEn(LocalDateTime canceladaEn) { this.canceladaEn = canceladaEn; }
    public String getMotivoCancelacion() { return motivoCancelacion; }
    public void setMotivoCancelacion(String motivoCancelacion) { this.motivoCancelacion = motivoCancelacion; }
}
