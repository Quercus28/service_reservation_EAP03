package com.service_reservation.EAP03.modulos.recursos.domain.model;

import java.time.LocalDateTime;

public record RegistrarBloqueoComando(
    Integer idRecurso,
    LocalDateTime fechaInicio,
    LocalDateTime fechaFin,
    String motivo
) {}
