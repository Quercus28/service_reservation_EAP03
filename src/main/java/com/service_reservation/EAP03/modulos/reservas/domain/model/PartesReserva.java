package com.service_reservation.EAP03.modulos.reservas.domain.model;

// Quién reserva, qué servicio y con qué proveedor.
public record PartesReserva(Long idCliente, Integer idServicio, Integer idProveedor) {
}
