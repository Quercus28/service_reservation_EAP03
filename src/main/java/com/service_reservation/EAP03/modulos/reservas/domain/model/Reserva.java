package com.service_reservation.EAP03.modulos.reservas.domain.model;

import com.service_reservation.EAP03.modulos.reservas.domain.exception.CancelacionFueraDeTiempoException;
import com.service_reservation.EAP03.modulos.reservas.domain.exception.DatosReservaInvalidosException;
import com.service_reservation.EAP03.modulos.reservas.domain.exception.FechaEnElPasadoException;
import com.service_reservation.EAP03.modulos.reservas.domain.exception.ReservaNoCancelableException;

import java.time.LocalDateTime;
import java.util.Objects;

public class Reserva {

    public static final int NOTAS_MAX = 500;
    public static final int MOTIVO_CANCELACION_MAX = 255;

    private final Integer id;
    private final Long idCliente;
    private final Integer idServicio;
    private final Integer idProveedor;
    private final LocalDateTime fechaInicio;
    private final LocalDateTime fechaFin;
    private EstadoReserva estado;
    private final String notas;
    private final LocalDateTime createdAt;
    private LocalDateTime canceladaEn;
    private String motivoCancelacion;

    private Reserva(Integer id, Long idCliente, Integer idServicio, Integer idProveedor,
                    LocalDateTime fechaInicio, LocalDateTime fechaFin, EstadoReserva estado,
                    String notas, LocalDateTime createdAt,
                    LocalDateTime canceladaEn, String motivoCancelacion) {
        this.id = id;
        this.idCliente = Objects.requireNonNull(idCliente, "idCliente es obligatorio");
        this.idServicio = Objects.requireNonNull(idServicio, "idServicio es obligatorio");
        this.idProveedor = Objects.requireNonNull(idProveedor, "idProveedor es obligatorio");
        this.fechaInicio = Objects.requireNonNull(fechaInicio, "fechaInicio es obligatoria");
        this.fechaFin = Objects.requireNonNull(fechaFin, "fechaFin es obligatoria");
        this.estado = Objects.requireNonNull(estado, "estado es obligatorio");
        this.notas = notas;
        this.createdAt = createdAt;
        this.canceladaEn = canceladaEn;
        this.motivoCancelacion = motivoCancelacion;
    }

    // Para crear una reserva NUEVA (todavía sin id). La hora actual ("ahora") se recibe como
    // dato en vez de leerla del reloj aquí adentro; así en las pruebas se puede usar
    // cualquier fecha.
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
                EstadoReserva.RESERVADO, limpiarTexto(notas, NOTAS_MAX,
                        "Las notas no pueden superar los " + NOTAS_MAX + " caracteres"), ahora, null, null);
    }

    // Para reconstruir una reserva que YA existe en la base de datos (puede estar en el pasado).
    public static Reserva reconstruir(Integer id, Long idCliente, Integer idServicio, Integer idProveedor,
                                      LocalDateTime fechaInicio, LocalDateTime fechaFin, EstadoReserva estado,
                                      String notas, LocalDateTime createdAt,
                                      LocalDateTime canceladaEn, String motivoCancelacion) {
        return new Reserva(id, idCliente, idServicio, idProveedor, fechaInicio, fechaFin,
                estado, notas, createdAt, canceladaEn, motivoCancelacion);
    }

    // Cancela la reserva. Solo se puede si todavía está en estado RESERVADO y si faltan al
    // menos "anticipacionMinimaHoras" horas para que empiece. Por ejemplo, con 2 horas, una
    // reserva de las 15:00 se puede cancelar hasta las 13:00 en punto.
    // Al quedar CANCELADO, la reserva deja de ocupar el horario y otro cliente puede tomarlo.
    public void cancelar(LocalDateTime ahora, int anticipacionMinimaHoras, String motivo) {
        Objects.requireNonNull(ahora, "ahora es obligatorio");
        if (estado != EstadoReserva.RESERVADO) {
            throw estado == EstadoReserva.CANCELADO
                    ? ReservaNoCancelableException.yaCancelada(id)
                    : ReservaNoCancelableException.noActiva(id, estado);
        }
        if (ahora.plusHours(anticipacionMinimaHoras).isAfter(fechaInicio)) {
            throw new CancelacionFueraDeTiempoException(id, anticipacionMinimaHoras);
        }
        String motivoLimpio = limpiarTexto(motivo, MOTIVO_CANCELACION_MAX,
                "El motivo no puede superar los " + MOTIVO_CANCELACION_MAX + " caracteres");
        this.estado = EstadoReserva.CANCELADO;
        this.canceladaEn = ahora;
        this.motivoCancelacion = motivoLimpio;
    }

    // ¿Esta reserva se cruza con el rango desde-hasta? Si una termina justo cuando la otra
    // empieza (por ejemplo 9:00-10:00 y 10:00-11:00), NO se cruzan. Igual que en recursos.
    public boolean seSolapaCon(LocalDateTime desde, LocalDateTime hasta) {
        return this.fechaInicio.isBefore(hasta) && desde.isBefore(this.fechaFin);
    }

    public boolean estaActiva() {
        return estado.esActiva();
    }

    public boolean esDelCliente(Long idCliente) {
        return this.idCliente.equals(idCliente);
    }

    // Quita espacios sobrantes, guarda vacío como "sin texto" y controla el largo máximo.
    private static String limpiarTexto(String texto, int maximo, String mensajeSiSePasa) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        String limpio = texto.trim();
        if (limpio.length() > maximo) {
            throw new DatosReservaInvalidosException(mensajeSiSePasa);
        }
        return limpio;
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
    public LocalDateTime getCanceladaEn() { return canceladaEn; }
    public String getMotivoCancelacion() { return motivoCancelacion; }
}
