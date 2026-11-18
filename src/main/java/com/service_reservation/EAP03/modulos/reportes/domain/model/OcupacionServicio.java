package com.service_reservation.EAP03.modulos.reportes.domain.model;

import java.util.Objects;

/**
 * Value Object que representa las métricas de ocupación y cancelación
 * de un único servicio dentro del período consultado.
 *
 * Dominio puro: sin anotaciones de Spring ni JPA.
 * Utiliza Builder para no exceder 5 parámetros en constructores/métodos.
 */
public class OcupacionServicio {

    private final Integer idServicio;
    private final String nombreServicio;
    private final int totalCitas;
    private final double horasOcupadas;
    private final double horasDisponibles;
    private final double porcentajeOcupacion;
    private final int citasCanceladas;
    private final double porcentajeCancelacion;

    private OcupacionServicio(Builder builder) {
        this.idServicio            = Objects.requireNonNull(builder.idServicio, "idServicio es obligatorio");
        this.nombreServicio        = Objects.requireNonNull(builder.nombreServicio, "nombreServicio es obligatorio");
        this.totalCitas            = builder.totalCitas;
        this.horasOcupadas         = builder.horasOcupadas;
        this.horasDisponibles      = builder.horasDisponibles;
        this.porcentajeOcupacion   = builder.porcentajeOcupacion;
        this.citasCanceladas       = builder.citasCanceladas;
        this.porcentajeCancelacion = builder.porcentajeCancelacion;
    }

    public Integer getIdServicio()            { return idServicio; }
    public String getNombreServicio()         { return nombreServicio; }
    public int getTotalCitas()                { return totalCitas; }
    public double getHorasOcupadas()          { return horasOcupadas; }
    public double getHorasDisponibles()       { return horasDisponibles; }
    public double getPorcentajeOcupacion()    { return porcentajeOcupacion; }
    public int getCitasCanceladas()           { return citasCanceladas; }
    public double getPorcentajeCancelacion()  { return porcentajeCancelacion; }

    public static Builder builder(Integer idServicio, String nombreServicio) {
        return new Builder(idServicio, nombreServicio);
    }

    public static final class Builder {
        private final Integer idServicio;
        private final String nombreServicio;
        private int totalCitas;
        private double horasOcupadas;
        private double horasDisponibles;
        private double porcentajeOcupacion;
        private int citasCanceladas;
        private double porcentajeCancelacion;

        private Builder(Integer idServicio, String nombreServicio) {
            this.idServicio     = idServicio;
            this.nombreServicio = nombreServicio;
        }

        public Builder totalCitas(int val)               { this.totalCitas = val;            return this; }
        public Builder horasOcupadas(double val)          { this.horasOcupadas = val;         return this; }
        public Builder horasDisponibles(double val)       { this.horasDisponibles = val;      return this; }
        public Builder porcentajeOcupacion(double val)    { this.porcentajeOcupacion = val;   return this; }
        public Builder citasCanceladas(int val)          { this.citasCanceladas = val;       return this; }
        public Builder porcentajeCancelacion(double val) { this.porcentajeCancelacion = val; return this; }

        public OcupacionServicio build() {
            return new OcupacionServicio(this);
        }
    }
}
