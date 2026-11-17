package com.service_reservation.EAP03.modulos.reservas.domain.model;

import java.util.List;

public record PaginaReservas(List<Reserva> contenido, int page, int size, long total) {
}