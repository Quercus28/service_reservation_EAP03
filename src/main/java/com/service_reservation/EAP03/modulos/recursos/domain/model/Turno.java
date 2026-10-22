package com.service_reservation.EAP03.modulos.recursos.domain.model;

import java.time.LocalDateTime;

// Un turno que se quiere ofrecer: de inicio a fin.
public record Turno(LocalDateTime inicio, LocalDateTime fin) {}
