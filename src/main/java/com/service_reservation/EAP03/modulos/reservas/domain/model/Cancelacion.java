package com.service_reservation.EAP03.modulos.reservas.domain.model;

import java.time.LocalDateTime;

// Cuándo y por qué se canceló una reserva.
public record Cancelacion(LocalDateTime canceladaEn, String motivo) {
}
