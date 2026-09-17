package com.service_reservation.EAP03.modulos.recursos.domain.model;

import java.math.BigDecimal;

public record ActualizarRecursoComando(
    Integer id,
    String nombre,
    BigDecimal precioUnitario,
    int stock
) {}
