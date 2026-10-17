package com.service_reservation.EAP03.modulos.reservas.domain.ports.in;

import com.service_reservation.EAP03.modulos.reservas.domain.model.IntervaloReservado;

import java.time.LocalDateTime;
import java.util.List;

// Consulta pensada para otros módulos (por ejemplo availability): devuelve los horarios
// de un servicio que ya están ocupados por reservas vigentes entre "desde" y "hasta".
public interface ConsultarReservasActivasUseCase {
    List<IntervaloReservado> ejecutar(Integer idServicio, LocalDateTime desde, LocalDateTime hasta);
}
