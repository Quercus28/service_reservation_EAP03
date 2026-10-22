package com.service_reservation.EAP03.modulos.reservas.domain.model;

import java.time.LocalDateTime;
import java.util.List;

// recursosOpcionales: ids de los recursos extra que eligió el cliente. Puede venir vacía.
public record CrearReservaComando(
    Long idUsuario,
    Integer idServicio,
    LocalDateTime fechaInicio,
    String notas,
    List<Integer> recursosOpcionales
) {}
