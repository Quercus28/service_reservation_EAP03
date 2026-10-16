package com.service_reservation.EAP03.modulos.reservas.domain.model;

import com.service_reservation.EAP03.modulos.reservas.domain.exception.DatosReservaInvalidosException;
import com.service_reservation.EAP03.modulos.reservas.domain.exception.FechaEnElPasadoException;

import java.time.LocalDateTime;
import java.util.Objects;

public class Reserva {

    public static final int NOTAS_MAX = 500;

    private final Integer id;
    private final Long idCliente;
    private final Integer idServicio;
    private final Integer idProveedor;
    private final LocalDateTime fechaInicio;
    private final LocalDateTime fechaFin;
    private final EstadoReserva estado;
    private final String notas;
    private final LocalDateTime createdAt;

    private Reserva(Integer id, Long idCliente, Integer idServicio, Integer idProveedor,
                    LocalDateTime fechaInicio, LocalDateTime fechaFin, EstadoReserva estado,
                    String notas, LocalDateTime createdAt) {
        this.id = id;
        this.idCliente = Objects.requireNonNull(idCliente, "idCliente es obligatorio");
        this.idServicio = Objects.requireNonNull(idServicio, "idServicio es obligatorio");
        this.idProveedor = Objects.requireNonNull(idProveedor, "idProveedor es obligatorio");
        this.fechaInicio = Objects.requireNonNull(fechaInicio, "fechaInicio es obligatoria");
        this.fechaFin = Objects.requireNonNull(fechaFin, "fechaFin es obligatoria");
        this.estado = Objects.requireNonNull(estado, "estado es obligatorio");
        this.notas = notas;
        this.createdAt = createdAt;
    }

    // Para crear una reserva NUEVA (todavía sin id). "ahora" llega desde fuera para que el
    // dominio no dependa del reloj del sistema.
    public static Reserva nueva(Long idCliente, Integer idServicio, Integer idProveedor,
                                LocalDateTime fechaInicio, LocalDateTime fechaFin,
                                String notas, LocalDateTime ahora) {
        Objects.requireNonNull(ahora, "ahora es obligatorio");
        if (fechaInicio == null || fechaFin == null) {
            throw new DatosReservaInvalidosException("fechaInicio y fechaFin son obligatorias");
        }
        if (fechaInicio.isBefore(ahora)) {
            throw new FechaEnElPasadoException(fechaInicio);
        }
        if (!fechaFin.isAfter(fechaInicio)) {
            throw new DatosReservaInvalidosException("fechaFin debe ser posterior a fechaInicio");
        }
        return new Reserva(null, idCliente, idServicio, idProveedor, fechaInicio, fechaFin,
                EstadoReserva.RESERVADO, normalizarNotas(notas), ahora);
    }

    // Para reconstruir una reserva que YA existe en la base de datos (puede estar en el pasado).
    public static Reserva reconstruir(Integer id, Long idCliente, Integer idServicio, Integer idProveedor,
                                      LocalDateTime fechaInicio, LocalDateTime fechaFin, EstadoReserva estado,
                                      String notas, LocalDateTime createdAt) {
        return new Reserva(id, idCliente, idServicio, idProveedor, fechaInicio, fechaFin,
                estado, notas, createdAt);
    }

    // Mismo criterio que RecursoPrestado: rangos [inicio, fin) que solo se tocan en el
    // borde no cuentan como solapamiento.
    public boolean seSolapaCon(LocalDateTime desde, LocalDateTime hasta) {
        return this.fechaInicio.isBefore(hasta) && desde.isBefore(this.fechaFin);
    }

    public boolean estaActiva() {
        return estado.esActiva();
    }

    private static String normalizarNotas(String notas) {
        if (notas == null || notas.isBlank()) {
            return null;
        }
        String limpias = notas.trim();
        if (limpias.length() > NOTAS_MAX) {
            throw new DatosReservaInvalidosException("Las notas no pueden superar los " + NOTAS_MAX + " caracteres");
        }
        return limpias;
    }

    public Integer getId() { return id; }
    public Long getIdCliente() { return idCliente; }
    public Integer getIdServicio() { return idServicio; }
    public Integer getIdProveedor() { return idProveedor; }
    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public LocalDateTime getFechaFin() { return fechaFin; }
    public EstadoReserva getEstado() { return estado; }
    public String getNotas() { return notas; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
