package com.service_reservation.EAP03.modulos.reservas.domain.ports.out;

import com.service_reservation.EAP03.modulos.reservas.domain.model.EstadoReserva;
import com.service_reservation.EAP03.modulos.reservas.domain.model.PaginaReservas;
import com.service_reservation.EAP03.modulos.reservas.domain.model.Reserva;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservaRepositoryPort {
    Reserva guardar(Reserva reserva);
    Optional<Reserva> buscarPorId(Integer id);
    List<Reserva> buscarActivasQueSeSolapan(Integer idServicio, LocalDateTime desde, LocalDateTime hasta);
    List<Reserva> buscarActivasDeCliente(Long idCliente);
    List<Reserva> buscarActivasDeProveedor(Integer idProveedor);
    // Historial: todas las reservas del cliente (cualquier estado), la más reciente primero.
    PaginaReservas buscarHistorialDeCliente(Long idCliente, EstadoReserva estado, int page, int size);
    int contarActivasDeCliente(Long idCliente);
    int contarActivasDeProveedor(Integer idProveedor);
}
