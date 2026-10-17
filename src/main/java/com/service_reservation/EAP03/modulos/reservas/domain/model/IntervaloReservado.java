package com.service_reservation.EAP03.modulos.reservas.domain.model;

import java.time.LocalDateTime;

public record IntervaloReservado(
    LocalDateTime inicio,
    LocalDateTime fin
) {}
