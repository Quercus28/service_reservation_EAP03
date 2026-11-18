package com.service_reservation.EAP03.modulos.reportes.domain.model;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Aggregate raíz del módulo de reportes.
 * Contiene las cifras consolidadas del proveedor (ocupación y cancelaciones)
 * y el desglose individual por cada servicio para el período consultado.
 *
 * Dominio puro: sin anotaciones de Spring ni JPA.
 */
public class ReporteOcupacion {

    private final Integer idProveedor;
    private final PeriodoReporte periodo;
    private final int totalCitasGlobal;
    private final double horasOcupadasGlobal;
    private final double horasDisponiblesGlobal;
    private final double porcentajeOcupacionGlobal;
    private final int totalCanceladasGlobal;
    private final double porcentajeCancelacionGlobal;
    private final List<OcupacionServicio> detalleServicios;

    private ReporteOcupacion(Builder builder) {
        this.idProveedor                 = Objects.requireNonNull(builder.idProveedor, "El id del proveedor es obligatorio");
        this.periodo                     = Objects.requireNonNull(builder.periodo,     "El período es obligatorio");
        this.totalCitasGlobal            = builder.totalCitasGlobal;
        this.horasOcupadasGlobal         = builder.horasOcupadasGlobal;
        this.horasDisponiblesGlobal      = builder.horasDisponiblesGlobal;
        this.porcentajeOcupacionGlobal   = builder.porcentajeOcupacionGlobal;
        this.totalCanceladasGlobal       = builder.totalCanceladasGlobal;
        this.porcentajeCancelacionGlobal = builder.porcentajeCancelacionGlobal;
        this.detalleServicios            = Collections.unmodifiableList(builder.detalleServicios);
    }

    // ── Getters ─────────────────────────────────────────────────────────────

    public Integer getIdProveedor()                 { return idProveedor; }
    public PeriodoReporte getPeriodo()              { return periodo; }
    public int getTotalCitasGlobal()                { return totalCitasGlobal; }
    public double getHorasOcupadasGlobal()          { return horasOcupadasGlobal; }
    public double getHorasDisponiblesGlobal()       { return horasDisponiblesGlobal; }
    public double getPorcentajeOcupacionGlobal()    { return porcentajeOcupacionGlobal; }
    public int getTotalCanceladasGlobal()          { return totalCanceladasGlobal; }
    public double getPorcentajeCancelacionGlobal() { return porcentajeCancelacionGlobal; }
    public List<OcupacionServicio> getDetalleServicios() { return detalleServicios; }

    // ── Builder ──────────────────────────────────────────────────────────────

    public static Builder builder(Integer idProveedor, PeriodoReporte periodo) {
        return new Builder(idProveedor, periodo);
    }

    public static final class Builder {
        private final Integer idProveedor;
        private final PeriodoReporte periodo;
        private int totalCitasGlobal;
        private double horasOcupadasGlobal;
        private double horasDisponiblesGlobal;
        private double porcentajeOcupacionGlobal;
        private int totalCanceladasGlobal;
        private double porcentajeCancelacionGlobal;
        private List<OcupacionServicio> detalleServicios = List.of();

        private Builder(Integer idProveedor, PeriodoReporte periodo) {
            this.idProveedor = idProveedor;
            this.periodo     = periodo;
        }

        public Builder totalCitasGlobal(int value)                { this.totalCitasGlobal = value;            return this; }
        public Builder horasOcupadasGlobal(double value)          { this.horasOcupadasGlobal = value;         return this; }
        public Builder horasDisponiblesGlobal(double value)       { this.horasDisponiblesGlobal = value;      return this; }
        public Builder porcentajeOcupacionGlobal(double value)    { this.porcentajeOcupacionGlobal = value;   return this; }
        public Builder totalCanceladasGlobal(int value)           { this.totalCanceladasGlobal = value;       return this; }
        public Builder porcentajeCancelacionGlobal(double value)  { this.porcentajeCancelacionGlobal = value; return this; }
        public Builder detalleServicios(List<OcupacionServicio> value) {
            this.detalleServicios = value != null ? value : List.of();
            return this;
        }

        public ReporteOcupacion build() { return new ReporteOcupacion(this); }
    }
}
