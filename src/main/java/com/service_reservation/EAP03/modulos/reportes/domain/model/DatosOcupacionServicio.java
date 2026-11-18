package com.service_reservation.EAP03.modulos.reportes.domain.model;

import java.util.Objects;

/**
 * Value Object que el adaptador de salida entrega al Domain Service
 * con los datos crudos de un servicio en el período:
 * - minutos totales reservados (excluyendo CANCELADO)
 * - número de citas efectivas
 * - número de citas canceladas
 * - minutos de capacidad disponible del servicio en el período
 * - metadatos básicos del servicio (nombre, duración unitaria)
 *
 * Dominio puro: sin anotaciones de Spring ni JPA.
 * Implementa el patrón Builder para evitar métodos con más de 5 parámetros.
 */
public class DatosOcupacionServicio {

    private final Integer idServicio;
    private final String nombreServicio;
    private final int duracionMinutosServicio;
    private final int totalCitas;
    private final int totalCanceladas;
    private final long minutosOcupados;
    private final long minutosDisponibles;

    private DatosOcupacionServicio(Builder builder) {
        this.idServicio              = Objects.requireNonNull(builder.idServicio, "idServicio es obligatorio");
        this.nombreServicio          = Objects.requireNonNull(builder.nombreServicio, "nombreServicio es obligatorio");
        this.duracionMinutosServicio = builder.duracionMinutosServicio;
        this.totalCitas              = builder.totalCitas;
        this.totalCanceladas         = builder.totalCanceladas;
        this.minutosOcupados         = builder.minutosOcupados;
        this.minutosDisponibles      = builder.minutosDisponibles;
    }

    public Integer getIdServicio()              { return idServicio; }
    public String getNombreServicio()           { return nombreServicio; }
    public int getDuracionMinutosServicio()     { return duracionMinutosServicio; }
    public int getTotalCitas()                  { return totalCitas; }
    public int getTotalCanceladas()             { return totalCanceladas; }
    public long getMinutosOcupados()            { return minutosOcupados; }
    public long getMinutosDisponibles()         { return minutosDisponibles; }

    public static Builder builder(Integer idServicio, String nombreServicio) {
        return new Builder(idServicio, nombreServicio);
    }

    public static final class Builder {
        private final Integer idServicio;
        private final String nombreServicio;
        private int duracionMinutosServicio;
        private int totalCitas;
        private int totalCanceladas;
        private long minutosOcupados;
        private long minutosDisponibles;

        private Builder(Integer idServicio, String nombreServicio) {
            this.idServicio     = idServicio;
            this.nombreServicio = nombreServicio;
        }

        public Builder duracionMinutosServicio(int val) { this.duracionMinutosServicio = val; return this; }
        public Builder totalCitas(int val)              { this.totalCitas = val;              return this; }
        public Builder totalCanceladas(int val)         { this.totalCanceladas = val;         return this; }
        public Builder minutosOcupados(long val)        { this.minutosOcupados = val;         return this; }
        public Builder minutosDisponibles(long val)     { this.minutosDisponibles = val;      return this; }

        public DatosOcupacionServicio build() {
            return new DatosOcupacionServicio(this);
        }
    }
}
