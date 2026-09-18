package com.service_reservation.EAP03.modulos.recursos.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RegistrarPrestamoComando(
    Integer idRecurso,
    Integer idServicioPrestado,
    int cantidad,
    LocalDateTime fechaInicio,
    LocalDateTime fechaFin,
    BigDecimal precioTotal
) {}
