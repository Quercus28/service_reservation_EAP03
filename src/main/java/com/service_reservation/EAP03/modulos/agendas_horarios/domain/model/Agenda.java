package com.service_reservation.EAP03.modulos.agendas_horarios.domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Agenda {
    private final Long id;
    private final Long proveedorId;
    private final List<Horario> horarios;

    public Agenda(Long id, Long proveedorId, List<Horario> horarios) {
        this.id = id;
        this.proveedorId = Objects.requireNonNull(proveedorId, "La agenda debe estar asociada a un proveedor");
        this.horarios = new ArrayList<>();
        if (horarios != null) {
            horarios.forEach(this::agregarHorario);
        }
    }

    public Agenda(Long proveedorId) {
        this(null, proveedorId, List.of());
    }

    public void agregarHorario(Horario horario) {
        Objects.requireNonNull(horario, "El horario es obligatorio");
        if (horarios.stream().anyMatch(horario::seSuperponeCon)) {
            throw new IllegalArgumentException("El horario se superpone con un horario previamente registrado en la agenda");
        }
        horarios.add(horario);
    }

    public void reemplazarHorario(Horario anterior, Horario nuevo) {
        Objects.requireNonNull(anterior, "El horario a modificar es obligatorio");
        Objects.requireNonNull(nuevo, "El nuevo horario es obligatorio");
        int indice = horarios.indexOf(anterior);
        if (indice < 0) {
            throw new IllegalArgumentException("El horario indicado no existe en la agenda");
        }
        if (horarios.stream().filter(h -> h != anterior).anyMatch(nuevo::seSuperponeCon)) {
            throw new IllegalArgumentException("El horario se superpone con un horario previamente registrado en la agenda");
        }
        horarios.set(indice, nuevo);
    }

    public Long getId() { return id; }
    public Long getProveedorId() { return proveedorId; }
    public List<Horario> getHorarios() { return Collections.unmodifiableList(horarios); }
}
