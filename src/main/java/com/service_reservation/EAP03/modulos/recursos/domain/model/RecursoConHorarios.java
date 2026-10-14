package com.service_reservation.EAP03.modulos.recursos.domain.model;

import java.util.List;

public record RecursoConHorarios(
    Recurso recurso,
    List<HorarioRecurso> horarios
) {}
