package com.service_reservation.EAP03.modulos.recursos.domain.model;

import java.time.LocalDateTime;

// Rango [inicio, fin) en el que las unidades de un recurso quedan prestadas.
public record PeriodoPrestamo(LocalDateTime inicio, LocalDateTime fin) {
}
