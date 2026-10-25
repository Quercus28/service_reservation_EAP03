package com.service_reservation.EAP03.modulos.recursos.domain.model;

import com.service_reservation.EAP03.modulos.recursos.domain.exception.DatosInvalidosException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;

public class BloqueoRecurso {

    public static final int MOTIVO_MAX = 255;

    private final Integer id;
    private final Integer idRecurso;
    private final LocalDateTime fechaInicio;
    private final LocalDateTime fechaFin;
    private final String motivo;
    private final LocalDateTime createdAt;

    private BloqueoRecurso(Integer id, Integer idRecurso, LocalDateTime fechaInicio,
                           LocalDateTime fechaFin, String motivo, LocalDateTime createdAt) {
        validarFechas(fechaInicio, fechaFin);
        this.id = id;
        this.idRecurso = Objects.requireNonNull(idRecurso, "idRecurso es obligatorio");
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.motivo = normalizarMotivo(motivo);
        this.createdAt = createdAt;
    }

    // Para crear un bloqueo NUEVO (todavía sin id). Solo aquí se exige que no termine en el
    // pasado: un bloqueo ya vencido que se lee de la base de datos sigue siendo válido.
    public static BloqueoRecurso nuevo(Integer idRecurso, LocalDateTime fechaInicio,
                                       LocalDateTime fechaFin, String motivo) {
        LocalDateTime ahora = LocalDateTime.now(ZoneId.systemDefault());
        BloqueoRecurso bloqueo = new BloqueoRecurso(null, idRecurso, fechaInicio, fechaFin, motivo, ahora);
        if (!fechaFin.isAfter(ahora)) {
            throw new DatosInvalidosException("fechaFin no puede estar en el pasado");
        }
        return bloqueo;
    }

    // Para reconstruir un bloqueo que YA existe en la base de datos
    public static BloqueoRecurso reconstruir(Integer id, Integer idRecurso, LocalDateTime fechaInicio,
                                             LocalDateTime fechaFin, String motivo, LocalDateTime createdAt) {
        return new BloqueoRecurso(id, idRecurso, fechaInicio, fechaFin, motivo, createdAt);
    }

    // Mismo criterio que RecursoPrestado: rangos [inicio, fin) que solo se tocan en el
    // borde no cuentan como solapamiento.
    public boolean seSolapaCon(LocalDateTime inicio, LocalDateTime fin) {
        return this.fechaInicio.isBefore(fin) && inicio.isBefore(this.fechaFin);
    }

    private static void validarFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        if (fechaInicio == null || fechaFin == null) {
            throw new DatosInvalidosException("fechaInicio y fechaFin son obligatorias");
        }
        if (!fechaFin.isAfter(fechaInicio)) {
            throw new DatosInvalidosException("fechaFin debe ser posterior a fechaInicio");
        }
    }

    private static String normalizarMotivo(String motivo) {
        if (motivo == null || motivo.isBlank()) {
            return null;
        }
        String limpio = motivo.trim();
        if (limpio.length() > MOTIVO_MAX) {
            throw new DatosInvalidosException("El motivo no puede superar los " + MOTIVO_MAX + " caracteres");
        }
        return limpio;
    }

    public Integer getId() { return id; }
    public Integer getIdRecurso() { return idRecurso; }
    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public LocalDateTime getFechaFin() { return fechaFin; }
    public String getMotivo() { return motivo; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
