package com.service_reservation.EAP03.modulos.agendas_horarios.domain.model;

import com.service_reservation.EAP03.modulos.agendas_horarios.domain.exception.DatosAgendaInvalidosException;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.exception.HorarioSolapadoException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Agenda {
    private final Long id;
    private final Integer proveedorId;
    private final Integer servicioId;
    private final boolean activa;
    private final List<Horario> horarios;

    public Agenda(Long id, Integer proveedorId, Integer servicioId, boolean activa, List<Horario> horarios) {
        this.id = id;
        this.proveedorId = Objects.requireNonNull(proveedorId, "La agenda debe estar asociada a un proveedor");
        this.servicioId = Objects.requireNonNull(servicioId, "El servicio es obligatorio");
        this.activa = activa;
        this.horarios = new ArrayList<>();
        if (horarios != null) {
            horarios.forEach(this::agregarHorario);
        }
    }

    public Agenda(Integer proveedorId, Integer servicioId) {
        this(null, proveedorId, servicioId, true, List.of());
    }

    public void agregarHorario(Horario horario) {
        Objects.requireNonNull(horario, "El horario es obligatorio");
        if (horarios.stream().anyMatch(horario::seSuperponeCon)) {
            throw new HorarioSolapadoException(
                    "El horario se superpone con un horario previamente registrado en la agenda"
            );
        }
        horarios.add(horario);
    }

    public void reemplazarHorario(Long horarioId, Horario nuevo) {
        Objects.requireNonNull(horarioId, "El id del horario es obligatorio");
        Objects.requireNonNull(nuevo, "El nuevo horario es obligatorio");
        int indice = -1;
        for (int i = 0; i < horarios.size(); i++) {
            if (horarioId.equals(horarios.get(i).id())) { indice = i; break; }
        }
        if (indice < 0) {
            throw new DatosAgendaInvalidosException("El horario indicado no existe en la agenda");
        }

        final int indiceFinal = indice;
        if (horarios.stream().filter(h -> !Objects.equals(h.id(), horarioId)).anyMatch(nuevo::seSuperponeCon)) {
            throw new HorarioSolapadoException(
                    "El nuevo horario se superpone con otro horario previamente registrado para el mismo día."
            );
        }
        horarios.set(indiceFinal, new Horario(horarioId, nuevo.diaSemana(), nuevo.horaInicio(), nuevo.horaFin()));
    }

    public Long getId() { return id; }
    public Integer getProveedorId() { return proveedorId; }
    public Integer getServicioId() { return servicioId; }
    public boolean isActiva() { return activa; }
    public List<Horario> getHorarios() { return Collections.unmodifiableList(horarios); }
}
