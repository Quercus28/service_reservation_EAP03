package com.service_reservation.EAP03.modulos.recursos.infrastructure.outbound.persistence.entity;

import com.service_reservation.EAP03.modulos.recursos.domain.model.DiaSemana;
import jakarta.persistence.*;
import java.time.LocalTime;

@Entity
@Table(name = "horario_recurso")
public class HorarioRecursoJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "id_recurso", nullable = false)
    private Integer idRecurso;

    @Enumerated(EnumType.STRING)
    @Column(name = "dia_semana", nullable = false, length = 10)
    private DiaSemana diaSemana;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getIdRecurso() { return idRecurso; }
    public void setIdRecurso(Integer idRecurso) { this.idRecurso = idRecurso; }
    public DiaSemana getDiaSemana() { return diaSemana; }
    public void setDiaSemana(DiaSemana diaSemana) { this.diaSemana = diaSemana; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }
    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }
}
