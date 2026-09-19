package com.service_reservation.EAP03.modulos.recursos.domain.model;

import java.time.LocalDateTime;

public record DisponibilidadRecurso(
    Integer idRecurso,
    LocalDateTime desde,
    LocalDateTime hasta,
    int stockTotal,
    int comprometido,
    int disponible
) {}
