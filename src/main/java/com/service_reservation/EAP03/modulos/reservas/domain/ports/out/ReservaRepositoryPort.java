package com.service_reservation.EAP03.modulos.reservas.domain.ports.out;

import com.service_reservation.EAP03.modulos.reservas.domain.model.Reserva;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservaRepositoryPort {
    Reserva guardar(Reserva reserva);
    Optional<Reserva> buscarPorId(Integer id);
    List<Reserva> buscarActivasQueSeSolapan(Integer idServicio, LocalDateTime desde, LocalDateTime hasta);
}
