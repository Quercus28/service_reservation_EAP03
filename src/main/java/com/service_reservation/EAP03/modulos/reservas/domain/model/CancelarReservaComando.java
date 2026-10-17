package com.service_reservation.EAP03.modulos.reservas.domain.model;

public record CancelarReservaComando(
    Long idUsuario,
    Integer idReserva,
    String motivo
) {}
