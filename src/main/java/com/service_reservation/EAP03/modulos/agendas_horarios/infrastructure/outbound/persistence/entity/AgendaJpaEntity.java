package com.service_reservation.EAP03.modulos.agendas_horarios.infrastructure.outbound.persistence.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "agenda",
        uniqueConstraints = @UniqueConstraint(name = "uk_agenda_servicio_activa", columnNames = {"id_servicio", "activa"}))
public class AgendaJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_proveedor", nullable = false)
    private Integer idProveedor;

    @Column(name = "id_servicio", nullable = false)
    private Integer idServicio;

    @Column(name = "activa", nullable = false)
    private boolean activa = true;

    @OneToMany(mappedBy = "agenda", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("id ASC")
    private List<HorarioJpaEntity> horarios = new ArrayList<>();

    public Long getId() { return id; }
    public Integer getIdProveedor() { return idProveedor; }
    public Integer getIdServicio() { return idServicio; }
    public boolean isActiva() { return activa; }
    public List<HorarioJpaEntity> getHorarios() { return horarios; }

    public void setId(Long id) { this.id = id; }
    public void setIdProveedor(Integer idProveedor) { this.idProveedor = idProveedor; }
    public void setIdServicio(Integer idServicio) { this.idServicio = idServicio; }
    public void setActiva(boolean activa) { this.activa = activa; }
    public void setHorarios(List<HorarioJpaEntity> horarios) { this.horarios = horarios; }
}
