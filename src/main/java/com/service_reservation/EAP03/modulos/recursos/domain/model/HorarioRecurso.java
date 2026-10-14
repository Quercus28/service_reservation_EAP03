package com.service_reservation.EAP03.modulos.recursos.domain.model;

import com.service_reservation.EAP03.modulos.recursos.domain.exception.DatosInvalidosException;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

public class HorarioRecurso {

    private final Integer id;
    private final Integer idRecurso;
    private final DiaSemana diaSemana;
    private final LocalTime horaInicio;
    private final LocalTime horaFin;

    private HorarioRecurso(Integer id, Integer idRecurso, DiaSemana diaSemana,
                           LocalTime horaInicio, LocalTime horaFin) {
        validarHoras(horaInicio, horaFin);
        this.id = id;
        this.idRecurso = Objects.requireNonNull(idRecurso, "idRecurso es obligatorio");
        this.diaSemana = Objects.requireNonNull(diaSemana, "diaSemana es obligatorio");
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
    }

    // Para crear un horario NUEVO (todavía sin id)
    public static HorarioRecurso nuevo(Integer idRecurso, DiaSemana diaSemana,
                                       LocalTime horaInicio, LocalTime horaFin) {
        return new HorarioRecurso(null, idRecurso, diaSemana, horaInicio, horaFin);
    }

    // Para reconstruir un horario que YA existe en la base de datos
    public static HorarioRecurso reconstruir(Integer id, Integer idRecurso, DiaSemana diaSemana,
                                             LocalTime horaInicio, LocalTime horaFin) {
        return new HorarioRecurso(id, idRecurso, diaSemana, horaInicio, horaFin);
    }

    // Mismo día y franjas que se cruzan. Dos franjas que solo se tocan en el borde
    // (08:00-12:00 y 12:00-16:00) no cuentan como solapamiento.
    public boolean seSolapaCon(HorarioRecurso otro) {
        return this.diaSemana == otro.diaSemana
                && this.horaInicio.isBefore(otro.horaFin)
                && otro.horaInicio.isBefore(this.horaFin);
    }

    // ¿El rango [desde, hasta) cae completo dentro de esta franja? Exige que ambos extremos
    // estén en el mismo día calendario y que ese día corresponda al diaSemana de la franja.
    public boolean contiene(LocalDateTime desde, LocalDateTime hasta) {
        if (desde == null || hasta == null || !hasta.isAfter(desde)) {
            return false;
        }
        if (!desde.toLocalDate().equals(hasta.toLocalDate())) {
            return false;
        }
        if (DiaSemana.desde(desde.getDayOfWeek()) != this.diaSemana) {
            return false;
        }
        return !desde.toLocalTime().isBefore(this.horaInicio)
                && !hasta.toLocalTime().isAfter(this.horaFin);
    }

    private static void validarHoras(LocalTime horaInicio, LocalTime horaFin) {
        if (horaInicio == null || horaFin == null) {
            throw new DatosInvalidosException("horaInicio y horaFin son obligatorias");
        }
        if (!horaFin.isAfter(horaInicio)) {
            throw new DatosInvalidosException("horaFin debe ser posterior a horaInicio");
        }
    }

    public Integer getId() { return id; }
    public Integer getIdRecurso() { return idRecurso; }
    public DiaSemana getDiaSemana() { return diaSemana; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public LocalTime getHoraFin() { return horaFin; }
}
