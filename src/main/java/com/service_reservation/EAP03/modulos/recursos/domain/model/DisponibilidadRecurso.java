package com.service_reservation.EAP03.modulos.recursos.domain.model;

import java.time.LocalDateTime;

// operativo = false cuando el rango cae fuera del horario del recurso o dentro de un
// bloqueo; en ese caso disponible es 0 aunque haya stock libre.
public record DisponibilidadRecurso(
    Integer idRecurso,
    LocalDateTime desde,
    LocalDateTime hasta,
    int stockTotal,
    int comprometido,
    int disponible,
    boolean operativo
) {
    // Constructor previo a la HU06: un resultado sin información de operatividad se
    // considera operativo.
    public DisponibilidadRecurso(Integer idRecurso, LocalDateTime desde, LocalDateTime hasta,
                                 int stockTotal, int comprometido, int disponible) {
        this(idRecurso, desde, hasta, stockTotal, comprometido, disponible, true);
    }
}
