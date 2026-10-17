package com.service_reservation.EAP03.modulos.reservas.domain.model;

import java.time.LocalDateTime;

public record CrearReservaComando(
    Long idUsuario,
    Integer idServicio,
    LocalDateTime fechaInicio,
    String notas
) {}
