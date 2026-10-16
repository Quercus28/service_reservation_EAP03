package com.service_reservation.EAP03.modulos.reservas.domain.ports.in;

import com.service_reservation.EAP03.modulos.reservas.domain.model.IntervaloReservado;

import java.time.LocalDateTime;
import java.util.List;

// Puerto público para otros módulos (p. ej. availability): qué intervalos de un servicio
// están ocupados por reservas activas dentro de [desde, hasta).
public interface ConsultarReservasActivasUseCase {
    List<IntervaloReservado> ejecutar(Integer idServicio, LocalDateTime desde, LocalDateTime hasta);
}
