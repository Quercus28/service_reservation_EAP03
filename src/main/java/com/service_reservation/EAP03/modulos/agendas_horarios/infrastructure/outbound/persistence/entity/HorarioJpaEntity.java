package com.service_reservation.EAP03.modulos.agendas_horarios.infrastructure.outbound.persistence.entity;

import com.service_reservation.EAP03.modulos.agendas_horarios.domain.model.DiaSemana;
import jakarta.persistence.*;

import java.time.LocalTime;

@Entity
@Table(name = "horario")
public class HorarioJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_agenda", nullable = false)
    private AgendaJpaEntity agenda;

    @Enumerated(EnumType.STRING)
    @Column(name = "dia_semana", nullable = false, length = 20)
    private DiaSemana diaSemana;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    public Long getId() { return id; }
    public AgendaJpaEntity getAgenda() { return agenda; }
    public DiaSemana getDiaSemana() { return diaSemana; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public LocalTime getHoraFin() { return horaFin; }

    public void setId(Long id) { this.id = id; }
    public void setAgenda(AgendaJpaEntity agenda) { this.agenda = agenda; }
    public void setDiaSemana(DiaSemana diaSemana) { this.diaSemana = diaSemana; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }
}
