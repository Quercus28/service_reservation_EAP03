package com.service_reservation.EAP03.modulos.recursos.domain.model;

import java.math.BigDecimal;

public record CrearRecursoComando(
    Integer idProveedor,
    String nombre,
    BigDecimal precioUnitario,
    int stock
) {}
