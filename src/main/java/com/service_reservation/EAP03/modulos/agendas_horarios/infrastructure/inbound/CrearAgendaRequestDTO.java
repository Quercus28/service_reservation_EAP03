package com.service_reservation.EAP03.modulos.agendas_horarios.infrastructure.inbound;

import jakarta.validation.constraints.NotNull;

public record CrearAgendaRequestDTO(
        @NotNull(message = "El servicio es obligatorio")
        Integer servicioId
) {}
