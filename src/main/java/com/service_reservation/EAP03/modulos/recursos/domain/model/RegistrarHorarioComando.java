package com.service_reservation.EAP03.modulos.recursos.domain.model;

import java.time.LocalTime;

public record RegistrarHorarioComando(
    Integer idRecurso,
    DiaSemana diaSemana,
    LocalTime horaInicio,
    LocalTime horaFin
) {}
