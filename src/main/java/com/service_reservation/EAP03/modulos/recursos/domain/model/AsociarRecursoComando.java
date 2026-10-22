package com.service_reservation.EAP03.modulos.recursos.domain.model;

public record AsociarRecursoComando(
    Integer idServicio,
    Integer idRecurso,
    int cantidad
) {}
